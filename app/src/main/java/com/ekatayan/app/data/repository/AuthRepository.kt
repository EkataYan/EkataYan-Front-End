package com.ekatayan.app.data.repository


import com.ekatayan.app.data.remote.SupabaseConfigurationException
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.SupabaseAuthApiService
import com.ekatayan.app.data.remote.api.GoogleIdTokenRequest
import com.ekatayan.app.data.remote.api.GoogleUserMetadataRequest
import com.ekatayan.app.data.remote.dto.PasswordSignInRequest
import com.ekatayan.app.data.remote.dto.PasswordSignUpMetadata
import com.ekatayan.app.data.remote.dto.PasswordSignUpRequest
import com.ekatayan.app.data.remote.dto.SupabaseSessionDto
import dagger.Lazy
import java.io.IOException
import java.util.logging.Logger
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import retrofit2.HttpException

enum class AuthenticationFailure {
    INVALID_CREDENTIALS, NETWORK, SERVER, CONFIGURATION, INVALID_RESPONSE,
    GOOGLE_CANCELED, GOOGLE_NO_CREDENTIAL, GOOGLE_INVALID_TOKEN,
}
class AuthenticationException(val failure: AuthenticationFailure) : Exception()

enum class SignUpResult { AUTHENTICATED, EMAIL_CONFIRMATION_REQUIRED }

interface AuthRepository {
    suspend fun signIn(email: String, password: String)
    suspend fun signInWithGoogle(
        idToken: String,
        nonce: String? = null,
        displayName: String? = null,
        avatarUrl: String? = null,
    ) {
        throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
    }
    suspend fun signUp(name: String, email: String, phone: String, password: String): SignUpResult
    suspend fun restoreSession(): Boolean
    suspend fun refreshSession(): Boolean
    fun currentUserEmail(): String? = null
    fun currentUserName(): String? = null
    fun clearSession()
}

class SupabaseAuthRepository @Inject constructor(
    private val api: Lazy<SupabaseAuthApiService>,
    private val session: UserSessionProvider,
) : AuthRepository {
    override suspend fun signIn(email: String, password: String) {
        try {
            store(api.get().signInWithPassword(request = PasswordSignInRequest(email, password)), authenticatedEmail = email)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: HttpException) {
            throw AuthenticationException(if (e.code() in 400..401) AuthenticationFailure.INVALID_CREDENTIALS else AuthenticationFailure.SERVER)
        } catch (e: SupabaseConfigurationException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: IOException) {
            throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationException(AuthenticationFailure.SERVER)
        }
    }

    override suspend fun signInWithGoogle(
        idToken: String,
        nonce: String?,
        displayName: String?,
        avatarUrl: String?,
    ) {
        if (idToken.isBlank()) throw AuthenticationException(AuthenticationFailure.INVALID_RESPONSE)
        try {
            store(
                api.get().signInWithIdToken(
                    request = GoogleIdTokenRequest(
                        idToken = idToken,
                        nonce = nonce,
                        data = GoogleUserMetadataRequest(displayName, avatarUrl),
                    ),
                ),
                authenticatedName = displayName,
            )
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: HttpException) {
            throw AuthenticationException(
                if (e.code() in 400..401) AuthenticationFailure.GOOGLE_INVALID_TOKEN else AuthenticationFailure.SERVER,
            )
        } catch (e: SupabaseConfigurationException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: IOException) {
            throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw AuthenticationException(AuthenticationFailure.SERVER)
        }
    }

    override suspend fun signUp(name: String, email: String, phone: String, password: String): SignUpResult {
        try {
            val response = api.get().signUpWithPassword(
                PasswordSignUpRequest(email, password, PasswordSignUpMetadata(fullName = name, phone = phone)),
            )
            val result = if (response.hasSession()) {
                store(response, authenticatedEmail = email, authenticatedName = name)
                SignUpResult.AUTHENTICATED
            } else {
                SignUpResult.EMAIL_CONFIRMATION_REQUIRED
            }
            logger.info("Supabase signup completed userUuid=${response.user?.id ?: "pending-confirmation"} result=$result")
            return result
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: HttpException) {
            throw AuthenticationException(if (e.code() in 400..401) AuthenticationFailure.INVALID_CREDENTIALS else AuthenticationFailure.SERVER)
        } catch (e: SupabaseConfigurationException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: IOException) {
            throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationException(AuthenticationFailure.SERVER)
        }
    }

    override suspend fun restoreSession(): Boolean {
        if (session.currentAccessToken() != null) {
            if (session.currentUserEmail() == null || session.currentUserName() == null) refreshSession()
            return session.currentAccessToken() != null
        }
        return refreshSession()
    }

    override suspend fun refreshSession(): Boolean {
        val refreshToken = session.refreshToken() ?: return false
        return try {
            store(api.get().refreshSession(request = com.ekatayan.app.data.remote.api.RefreshTokenRequest(refreshToken)))
            true
        } catch (e: CancellationException) {
            throw e
        } catch (_: AuthenticationException) {
            clearSession()
            false
        } catch (_: HttpException) {
            clearSession()
            false
        } catch (_: IOException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    override fun clearSession() = session.clearSession()

    override fun currentUserEmail(): String? = session.currentUserEmail()

    override fun currentUserName(): String? = session.currentUserName()

    private fun store(
        value: SupabaseSessionDto,
        authenticatedEmail: String? = session.currentUserEmail(),
        authenticatedName: String? = session.currentUserName(),
    ) {
        val access = value.accessToken
        val refresh = value.refreshToken
        val expiresAt = value.expiresAtSeconds?.times(1_000)
            ?: value.expiresInSeconds?.times(1_000)?.plus(System.currentTimeMillis())
        if (access.isNullOrBlank() || refresh.isNullOrBlank() || expiresAt == null || expiresAt <= System.currentTimeMillis()) {
            throw AuthenticationException(AuthenticationFailure.INVALID_RESPONSE)
        }
        session.setSession(
            accessToken = access,
            refreshToken = refresh,
            expiresAtMillis = expiresAt,
            email = value.user?.email ?: authenticatedEmail,
            name = value.user?.userMetadata?.fullName
                ?: value.user?.userMetadata?.name
                ?: authenticatedName,
        )
    }

    private fun SupabaseSessionDto.hasSession() =
        !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()

    private companion object { val logger: Logger = Logger.getLogger("EkataYanAuth") }
}
