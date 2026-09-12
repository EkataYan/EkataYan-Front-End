package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.CachedProfile
import com.ekatayan.app.data.local.MemoryProfileCacheStore
import com.ekatayan.app.data.local.ProfileCacheStore
import com.ekatayan.app.data.model.ProfileDetails
import com.ekatayan.app.data.remote.AuthenticationRequiredException
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.ekatayan.app.data.remote.dto.ProfileDto
import com.ekatayan.app.data.remote.dto.UpdateProfileRequest
import com.google.gson.JsonParseException
import dagger.Lazy
import java.io.IOException
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException

enum class ProfileFailure { AUTHENTICATION, NETWORK, NOT_FOUND, FORBIDDEN, SERVER, INVALID_RESPONSE, CONFIGURATION }
class ProfileLoadException(val failure: ProfileFailure) : Exception()

@Singleton
class ProfileRepository @Inject constructor(
    private val api: Lazy<ProfileApiService>,
    private val session: UserSessionProvider,
    private val cache: ProfileCacheStore = MemoryProfileCacheStore(),
) {
    private var activeIdentity = cacheIdentity()
    private var cachedEntry = activeIdentity?.let(cache::read)
    private val mutableProfile = MutableStateFlow(cachedEntry?.profile ?: authProfile())
    val profile = mutableProfile.asStateFlow()

    /** Synchronous UI source. It never performs network I/O. */
    fun currentProfile(): ProfileDetails? {
        activateCurrentUser()
        return mutableProfile.value
    }

    fun hasPendingChanges(): Boolean {
        activateCurrentUser()
        return cachedEntry?.pendingFields?.isNotEmpty() == true
    }

    fun activateCurrentUser() {
        val identity = cacheIdentity()
        if (identity == activeIdentity) return
        activeIdentity = identity
        if (identity == null) {
            cachedEntry = null
            mutableProfile.value = null
            return
        }
        cache.clearMemoryForOtherUser(identity)
        cachedEntry = cache.read(identity)
        mutableProfile.value = cachedEntry?.profile ?: authProfile()
    }

    /** Refreshes the canonical cloud profile and replaces the synchronized local snapshot. */
    suspend fun getProfile(): ProfileDetails {
        activateCurrentUser()
        try {
            val response = api.get().getProfile()
            val dto = response?.data
            if (response?.success != true || dto?.id.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            logger.info("Profile fetch succeeded userUuid=${dto!!.id}")
            return mergeCloudProfile(dto)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ProfileLoadException) {
            throw e
        } catch (e: AuthenticationRequiredException) {
            throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        } catch (e: HttpException) {
            throw ProfileLoadException(e.toProfileFailure())
        } catch (e: JsonParseException) {
            throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
        } catch (e: IOException) {
            throw ProfileLoadException(ProfileFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw ProfileLoadException(ProfileFailure.CONFIGURATION)
        }
    }

    /** Writes locally before PATCH; failures retain the local data and pending fields for retry. */
    suspend fun updateProfile(profile: ProfileDetails): ProfileDetails {
        activateCurrentUser()
        val previous = mutableProfile.value ?: authProfile() ?: profile
        val pending = cachedEntry?.pendingFields.orEmpty() + changedFields(previous, profile)
        saveLocal(profile, pending)
        if (pending.isEmpty()) return profile

        try {
            val response = api.get().updateProfile(profile.toPatch(pending))
            val dto = response?.data
            if (response?.success != true || dto?.id.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            return dto!!.toDetails(profile).also { saveLocal(it, emptySet()) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ProfileLoadException) {
            throw e
        } catch (e: AuthenticationRequiredException) {
            throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        } catch (e: HttpException) {
            throw ProfileLoadException(e.toProfileFailure(update = true))
        } catch (e: JsonParseException) {
            throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
        } catch (e: IOException) {
            throw ProfileLoadException(ProfileFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw ProfileLoadException(ProfileFailure.CONFIGURATION)
        }
    }

    private fun saveLocal(profile: ProfileDetails, pendingFields: Set<String>) {
        val value = CachedProfile(profile, pendingFields)
        cachedEntry = value
        (activeIdentity ?: cacheIdentity())?.let { cache.save(it, value) }
        if (profile.name.isNotBlank()) session.updateUserName(profile.name)
        mutableProfile.value = profile
    }

    private fun cacheIdentity() = session.currentUserEmail()?.trim()?.lowercase()?.takeIf(String::isNotEmpty)

    private fun authProfile(): ProfileDetails? {
        val email = session.currentUserEmail().orEmpty()
        val name = session.currentUserName().orEmpty()
        return if (email.isBlank() && name.isBlank()) null else ProfileDetails(
            name = name, location = "", email = email, language = "en",
        )
    }

    private fun changedFields(before: ProfileDetails, after: ProfileDetails) = buildSet {
        if (before.name != after.name) add(DISPLAY_NAME)
        if (before.phone != after.phone) add(PHONE)
        if (before.bio != after.bio) add(BIO)
        if (before.location != after.location) add(HOME_CITY)
        if (before.language != after.language) add(LANGUAGE)
        if (before.interests != after.interests) add(INTERESTS)
    }

    private fun ProfileDetails.toPatch(fields: Set<String>) = UpdateProfileRequest(
        displayName = name.takeIf { DISPLAY_NAME in fields },
        bio = bio.takeIf { BIO in fields },
        homeCity = location.takeIf { HOME_CITY in fields },
        language = language.takeIf { LANGUAGE in fields },
        interests = interests.takeIf { INTERESTS in fields },
        phone = phone.takeIf { PHONE in fields },
    )

    private fun ProfileDto.toDetails(fallback: ProfileDetails?) = ProfileDetails(
        name = displayName ?: fallback?.name.orEmpty(),
        location = homeCity ?: fallback?.location.orEmpty(),
        email = email ?: fallback?.email ?: session.currentUserEmail().orEmpty(),
        phone = phone ?: fallback?.phone.orEmpty(),
        bio = bio ?: fallback?.bio.orEmpty(),
        language = language ?: fallback?.language.orEmpty().ifBlank { "en" },
        interests = interests ?: fallback?.interests.orEmpty(),
        avatarPath = avatarPath ?: fallback?.avatarPath,
    )

    private fun mergeCloudProfile(dto: ProfileDto): ProfileDetails {
        val local = mutableProfile.value
        val pending = cachedEntry?.pendingFields.orEmpty()
        val cloud = dto.toDetails(local)
        val merged = if (local == null || pending.isEmpty()) cloud else cloud.copy(
            name = local.name.takeIf { DISPLAY_NAME in pending } ?: cloud.name,
            phone = local.phone.takeIf { PHONE in pending } ?: cloud.phone,
            bio = local.bio.takeIf { BIO in pending } ?: cloud.bio,
            location = local.location.takeIf { HOME_CITY in pending } ?: cloud.location,
            language = local.language.takeIf { LANGUAGE in pending } ?: cloud.language,
            interests = local.interests.takeIf { INTERESTS in pending } ?: cloud.interests,
        )
        saveLocal(merged, pending)
        return merged
    }

    private fun HttpException.toProfileFailure(update: Boolean = false) = when (code()) {
        400, 422 -> if (update) ProfileFailure.INVALID_RESPONSE else ProfileFailure.SERVER
        401 -> ProfileFailure.AUTHENTICATION
        403 -> ProfileFailure.FORBIDDEN
        404 -> ProfileFailure.NOT_FOUND
        else -> ProfileFailure.SERVER
    }

    private companion object {
        const val DISPLAY_NAME = "display_name"
        const val PHONE = "phone"
        const val BIO = "bio"
        const val HOME_CITY = "home_city"
        const val LANGUAGE = "language"
        const val INTERESTS = "interests"
        val logger: Logger = Logger.getLogger("EkataYanProfile")
    }
}
