package com.ekatayan.app.data.repository


import com.ekatayan.app.data.model.ProfileDetails
import com.ekatayan.app.data.remote.AuthenticationRequiredException
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.google.gson.JsonParseException
import dagger.Lazy
import java.io.IOException
import java.util.logging.Logger
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

enum class ProfileFailure { AUTHENTICATION, NETWORK, NOT_FOUND, FORBIDDEN, SERVER, INVALID_RESPONSE, CONFIGURATION }
class ProfileLoadException(val failure: ProfileFailure) : Exception()

class ProfileRepository @Inject constructor(private val api: Lazy<ProfileApiService>) {
    suspend fun getProfile(): ProfileDetails {
        try {
            val response = api.get().getProfile()
            val profile = response?.data
            if (response?.success != true || profile?.id.isNullOrBlank()) {
                throw ProfileLoadException(ProfileFailure.INVALID_RESPONSE)
            }
            logger.info("Profile fetch succeeded userUuid=${profile!!.id}")
            return ProfileDetails(
                name = profile.displayName.orEmpty(), location = profile.homeCity.orEmpty(),
                email = profile.email.orEmpty(), phone = profile.phone.orEmpty(),
                bio = profile.bio.orEmpty(), language = profile.language.orEmpty(),
                interests = profile.interests.orEmpty(), avatarPath = profile.avatarPath,
            )
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

    private companion object { val logger: Logger = Logger.getLogger("EkataYanProfile") }
}
