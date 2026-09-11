package com.ekatayan.app.data.repository


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
) {
    private val mutableProfile = MutableStateFlow<ProfileDetails?>(null)
    val profile = mutableProfile.asStateFlow()

    suspend fun getProfile(): ProfileDetails {
        try {
            val response = api.get().getProfile()
            val profile = response?.data
            if (response?.success != true || profile?.id.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            logger.info("Profile fetch succeeded userUuid=${profile!!.id}")
            return profile.toDetails().also {
                if (it.name.isNotBlank()) session.updateUserName(it.name)
                mutableProfile.value = it
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthenticationRequiredException) {
            logger.warning("Profile fetch failed: authentication required")
            throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        } catch (e: HttpException) {
            logger.warning("Profile fetch failed: HTTP ${e.code()}")
            throw ProfileLoadException(when (e.code()) {
                401 -> ProfileFailure.AUTHENTICATION
                403 -> ProfileFailure.FORBIDDEN
                404 -> ProfileFailure.NOT_FOUND
                else -> ProfileFailure.SERVER
            })
        } catch (e: JsonParseException) {
            logger.warning("Profile fetch failed: invalid response (${e.javaClass.simpleName})")
            throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
        } catch (e: IOException) {
            logger.warning("Profile fetch failed: network error (${e.javaClass.simpleName})")
            throw ProfileLoadException(ProfileFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            logger.warning("Profile fetch failed: configuration error (${e.javaClass.simpleName})")
            throw ProfileLoadException(ProfileFailure.CONFIGURATION)
        }
    }

    suspend fun updateProfile(profile: ProfileDetails): ProfileDetails {
        try {
            val response = api.get().updateProfile(
                UpdateProfileRequest(
                    displayName = profile.name,
                    bio = profile.bio,
                    homeCity = profile.location,
                    language = profile.language,
                    interests = profile.interests,
                    phone = profile.phone,
                ),
            )
            val updated = response?.data
            if (response?.success != true || updated?.id.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            return updated!!.toDetails().also {
                session.updateUserName(it.name)
                mutableProfile.value = it
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ProfileLoadException) {
            throw e
        } catch (e: AuthenticationRequiredException) {
            throw ProfileLoadException(ProfileFailure.AUTHENTICATION)
        } catch (e: HttpException) {
            throw ProfileLoadException(when (e.code()) {
                400, 422 -> ProfileFailure.INVALID_RESPONSE
                401 -> ProfileFailure.AUTHENTICATION
                403 -> ProfileFailure.FORBIDDEN
                404 -> ProfileFailure.NOT_FOUND
                else -> ProfileFailure.SERVER
            })
        } catch (e: JsonParseException) {
            throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
        } catch (e: IOException) {
            throw ProfileLoadException(ProfileFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw ProfileLoadException(ProfileFailure.CONFIGURATION)
        }
    }

    private fun ProfileDto.toDetails() = ProfileDetails(
        name = displayName.orEmpty(), location = homeCity.orEmpty(),
        email = email.orEmpty(), phone = phone.orEmpty(), bio = bio.orEmpty(),
        language = language.orEmpty(), interests = interests.orEmpty(), avatarPath = avatarPath,
    )

    private companion object { val logger: Logger = Logger.getLogger("EkataYanProfile") }
}
