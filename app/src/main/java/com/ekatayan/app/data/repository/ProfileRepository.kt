package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.CachedProfile
import com.ekatayan.app.data.local.MemoryProfileCacheStore
import com.ekatayan.app.data.local.InvalidProfileImageException
import com.ekatayan.app.data.local.ProfileCacheStore
import com.ekatayan.app.data.local.ProfileImageStore
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

enum class ProfileFailure { AUTHENTICATION, NETWORK, NOT_FOUND, FORBIDDEN, USERNAME_TAKEN, SERVER, INVALID_RESPONSE, INVALID_IMAGE, CONFIGURATION }
class ProfileLoadException(val failure: ProfileFailure) : Exception()

@Singleton
class ProfileRepository @Inject constructor(
    private val api: Lazy<ProfileApiService>,
    private val session: UserSessionProvider,
    private val cache: ProfileCacheStore = MemoryProfileCacheStore(),
    private val imageStore: ProfileImageStore,
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

    fun hasPendingAvatar(): Boolean {
        activateCurrentUser()
        return AVATAR in cachedEntry?.pendingFields.orEmpty()
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
            return cacheCloudAvatar(mergeCloudProfile(dto))
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
        } catch (e: InvalidProfileImageException) {
            throw ProfileLoadException(ProfileFailure.INVALID_IMAGE)
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
            return syncPending(profile)
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
        } catch (e: InvalidProfileImageException) {
            throw ProfileLoadException(ProfileFailure.INVALID_IMAGE)
        } catch (e: IOException) {
            throw ProfileLoadException(ProfileFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw ProfileLoadException(ProfileFailure.CONFIGURATION)
        }
    }

    /** Saves a normalized account-scoped local image first, then uploads it and persists avatar_path. */
    suspend fun updateAvatar(uri: String): ProfileDetails {
        activateCurrentUser()
        val identity = activeIdentity ?: throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        val store = imageStore
        val previous = mutableProfile.value ?: authProfile() ?: throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        try {
            val localPath = withContext(Dispatchers.IO) { store.importFromPicker(uri, identity) }
            val local = previous.copy(avatarLocalPath = localPath)
            saveLocal(local, cachedEntry?.pendingFields.orEmpty() + AVATAR)
            return syncPending(local)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ProfileLoadException) {
            throw e
        } catch (e: InvalidProfileImageException) {
            throw ProfileLoadException(ProfileFailure.INVALID_IMAGE)
        } catch (e: AuthenticationRequiredException) {
            throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        } catch (e: HttpException) {
            throw ProfileLoadException(e.toProfileFailure(update = true))
        } catch (e: IOException) {
            throw ProfileLoadException(ProfileFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw ProfileLoadException(ProfileFailure.CONFIGURATION)
        }
    }

    private suspend fun syncPending(profile: ProfileDetails): ProfileDetails {
        var synced = profile
        var pending = cachedEntry?.pendingFields.orEmpty()

        if (AVATAR in pending) {
            val store = imageStore
            val localPath = synced.avatarLocalPath ?: throw ProfileLoadException(ProfileFailure.INVALID_IMAGE)
            val response = api.get().uploadProfilePicture(store.multipart(localPath))
            val upload = response?.data
            val avatarPath = upload?.profile?.avatarPath ?: upload?.upload?.path
            if (response?.success != true || upload?.profile?.id.isNullOrBlank() || avatarPath.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            synced = synced.copy(avatarPath = avatarPath)
            pending = pending - AVATAR
            saveLocal(synced, pending)
        }

        val patchFields = pending.intersect(PROFILE_FIELDS)
        if (patchFields.isNotEmpty()) {
            val response = api.get().updateProfile(synced.toPatch(patchFields))
            val dto = response?.data
            if (response?.success != true || dto?.id.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            synced = dto!!.toDetails(synced)
            pending = pending - patchFields
            saveLocal(synced, pending)
        }
        return synced
    }

    private suspend fun cacheCloudAvatar(profile: ProfileDetails): ProfileDetails {
        val store = imageStore
        val identity = activeIdentity ?: return profile
        if (profile.avatarPath.isNullOrBlank()) {
            return profile.copy(avatarLocalPath = null).also {
                saveLocal(it, cachedEntry?.pendingFields.orEmpty())
            }
        }
        if (store.exists(profile.avatarLocalPath)) return profile

        return try {
            val response = api.get().getProfilePicture()
            val body = response.body()
            if (!response.isSuccessful || body == null) return profile
            val localPath = body.use { responseBody ->
                withContext(Dispatchers.IO) { store.saveDownloaded(responseBody.byteStream(), identity) }
            }
            profile.copy(avatarLocalPath = localPath).also {
                saveLocal(it, cachedEntry?.pendingFields.orEmpty())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            profile
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
        if (before.username != after.username) add(USERNAME)
        if (before.phone != after.phone) add(PHONE)
        if (before.bio != after.bio) add(BIO)
        if (before.location != after.location) add(HOME_CITY)
        if (before.language != after.language) add(LANGUAGE)
        if (before.interests != after.interests) add(INTERESTS)
        if (before.isDiscoverable != after.isDiscoverable) add(DISCOVERABLE)
    }

    private fun ProfileDetails.toPatch(fields: Set<String>) = UpdateProfileRequest(
        username = username.takeIf { USERNAME in fields },
        displayName = name.takeIf { DISPLAY_NAME in fields },
        bio = bio.takeIf { BIO in fields },
        homeCity = location.takeIf { HOME_CITY in fields },
        language = language.takeIf { LANGUAGE in fields },
        interests = interests.takeIf { INTERESTS in fields },
        phone = phone.takeIf { PHONE in fields },
        isDiscoverable = isDiscoverable.takeIf { DISCOVERABLE in fields },
    )

    private fun ProfileDto.toDetails(fallback: ProfileDetails?): ProfileDetails {
        val resolvedAvatarPath = avatarPath ?: fallback?.avatarPath
        return ProfileDetails(
            name = displayName ?: fallback?.name.orEmpty(),
            location = homeCity ?: fallback?.location.orEmpty(),
            username = username ?: fallback?.username.orEmpty(),
            isDiscoverable = isDiscoverable ?: fallback?.isDiscoverable ?: true,
            email = email ?: fallback?.email ?: session.currentUserEmail().orEmpty(),
            phone = phone ?: fallback?.phone.orEmpty(),
            bio = bio ?: fallback?.bio.orEmpty(),
            language = language ?: fallback?.language.orEmpty().ifBlank { "en" },
            interests = interests ?: fallback?.interests.orEmpty(),
            avatarPath = resolvedAvatarPath,
            avatarLocalPath = fallback?.avatarLocalPath?.takeIf { fallback.avatarPath == resolvedAvatarPath },
        )
    }

    private fun mergeCloudProfile(dto: ProfileDto): ProfileDetails {
        val local = mutableProfile.value
        val pending = cachedEntry?.pendingFields.orEmpty()
        val cloud = dto.toDetails(local)
        val merged = if (local == null || pending.isEmpty()) cloud else cloud.copy(
            name = local.name.takeIf { DISPLAY_NAME in pending } ?: cloud.name,
            username = local.username.takeIf { USERNAME in pending } ?: cloud.username,
            phone = local.phone.takeIf { PHONE in pending } ?: cloud.phone,
            bio = local.bio.takeIf { BIO in pending } ?: cloud.bio,
            location = local.location.takeIf { HOME_CITY in pending } ?: cloud.location,
            language = local.language.takeIf { LANGUAGE in pending } ?: cloud.language,
            interests = local.interests.takeIf { INTERESTS in pending } ?: cloud.interests,
            isDiscoverable = local.isDiscoverable.takeIf { DISCOVERABLE in pending } ?: cloud.isDiscoverable,
            avatarPath = local.avatarPath.takeIf { AVATAR in pending } ?: cloud.avatarPath,
            avatarLocalPath = local.avatarLocalPath.takeIf { AVATAR in pending } ?: cloud.avatarLocalPath,
        )
        saveLocal(merged, pending)
        return merged
    }

    private fun HttpException.toProfileFailure(update: Boolean = false) = when (code()) {
        400, 422 -> if (update) ProfileFailure.INVALID_RESPONSE else ProfileFailure.SERVER
        401 -> ProfileFailure.AUTHENTICATION
        403 -> ProfileFailure.FORBIDDEN
        404 -> ProfileFailure.NOT_FOUND
        409 -> ProfileFailure.USERNAME_TAKEN
        else -> ProfileFailure.SERVER
    }

    private companion object {
        const val DISPLAY_NAME = "display_name"
        const val USERNAME = "username"
        const val PHONE = "phone"
        const val BIO = "bio"
        const val HOME_CITY = "home_city"
        const val LANGUAGE = "language"
        const val INTERESTS = "interests"
        const val AVATAR = "avatar"
        const val DISCOVERABLE = "is_discoverable"
        val PROFILE_FIELDS = setOf(DISPLAY_NAME, USERNAME, PHONE, BIO, HOME_CITY, LANGUAGE, INTERESTS, DISCOVERABLE)
        val logger: Logger = Logger.getLogger("EkataYanProfile")
    }
}
