package com.ekatayan.app.data.repository


import com.ekatayan.app.data.remote.SupabaseConfigurationException
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.SupabaseAuthApiService
import com.ekatayan.app.data.remote.api.PasswordUpdateRequest
import com.ekatayan.app.data.remote.api.PasswordRecoveryRequest
import com.ekatayan.app.data.remote.api.GoogleIdTokenRequest
import com.ekatayan.app.data.remote.dto.PasswordSignInRequest
import com.ekatayan.app.data.remote.dto.PasswordSignUpMetadata
import com.ekatayan.app.data.remote.dto.PasswordSignUpRequest
import com.ekatayan.app.data.remote.dto.SupabaseSessionDto
import com.google.gson.JsonParser
import dagger.Lazy
import java.io.IOException
import java.util.logging.Logger
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import retrofit2.HttpException

enum class AuthenticationFailure {
    INVALID_CREDENTIALS, NETWORK, SERVER, CONFIGURATION, INVALID_RESPONSE,
    EMAIL_NOT_CONFIRMED, RATE_LIMITED,
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
    suspend fun updatePassword(password: String) {
        throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
    }
    suspend fun requestPasswordRecovery(email: String) {
        throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
    }
    fun currentUserEmail(): String? = null
    fun currentUserName(): String? = null
    fun clearSession()
}

class SupabaseAuthRepository @Inject constructor(
    private val api: Lazy<SupabaseAuthApiService>,
    private val session: UserSessionProvider,
) : AuthRepository {
    override suspend fun requestPasswordRecovery(email: String) {
        logger.info("Password recovery request started")
        try {
            api.get().requestPasswordRecovery(PasswordRecoveryRequest(email.trim()))
            logger.info("Password recovery request accepted")
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            val failure = e.toAuthenticationFailure(defaultClientFailure = AuthenticationFailure.INVALID_CREDENTIALS)
            logger.warning("Password recovery response failure status=${e.code()} type=$failure")
            throw AuthenticationException(failure)
        } catch (e: SupabaseConfigurationException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: IOException) {
            throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: Exception) {
            throw AuthenticationException(AuthenticationFailure.SERVER)
        }
    }

    override suspend fun updatePassword(password: String) {
        val token = session.currentAccessToken() ?: throw AuthenticationException(AuthenticationFailure.INVALID_CREDENTIALS)
        try {
            api.get().updatePassword("Bearer $token", PasswordUpdateRequest(password))
        } catch (e: CancellationException) { throw e
        } catch (e: IOException) { throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: Exception) { throw AuthenticationException(AuthenticationFailure.SERVER) }
    }
    override suspend fun signIn(email: String, password: String) {
        logger.info("Email auth request started")
        try {
            store(api.get().signInWithPassword(request = PasswordSignInRequest(email, password)), authenticatedEmail = email)
            logger.info("Email auth succeeded; Supabase session persisted")
        } catch (e: AuthenticationException) {
            logger.warning("Email auth failed type=${e.failure}")
            throw e
        } catch (e: HttpException) {
            val failure = e.toAuthenticationFailure(defaultClientFailure = AuthenticationFailure.INVALID_CREDENTIALS)
            logger.warning("Email auth response failure status=${e.code()} type=$failure")
            throw AuthenticationException(failure)
        } catch (e: SupabaseConfigurationException) {
            logger.warning("Email auth configuration failure")
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: IOException) {
            logger.warning("Email auth network failure type=${e.javaClass.simpleName}")
            throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            logger.warning("Email auth invalid client configuration")
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warning("Email auth unexpected failure type=${e.javaClass.simpleName}")
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
        logger.info("Google ID-token exchange started")
        try {
            store(
                api.get().signInWithIdToken(
                    request = GoogleIdTokenRequest(
                        idToken = idToken,
                        nonce = nonce,
                    ),
                ),
                authenticatedName = displayName,
                preferAuthenticatedName = true,
            )
            logger.info("Google sign-in completed through Supabase Auth")
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: HttpException) {
            val failure = e.toAuthenticationFailure(defaultClientFailure = AuthenticationFailure.GOOGLE_INVALID_TOKEN)
            logger.warning("Google ID-token exchange response failure status=${e.code()} type=$failure")
            throw AuthenticationException(failure)
        } catch (e: SupabaseConfigurationException) {
            logger.warning("Google auth configuration failure")
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: IOException) {
            logger.warning("Google auth network failure type=${e.javaClass.simpleName}")
            throw AuthenticationException(AuthenticationFailure.NETWORK)
        } catch (e: IllegalArgumentException) {
            logger.warning("Google auth invalid client configuration")
            throw AuthenticationException(AuthenticationFailure.CONFIGURATION)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.warning("Google auth unexpected failure type=${e.javaClass.simpleName}")
            throw AuthenticationException(AuthenticationFailure.SERVER)
        }
    }

    override suspend fun signUp(name: String, email: String, phone: String, password: String): SignUpResult {
        logger.info("Email signup request started")
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
            val failure = e.toAuthenticationFailure(defaultClientFailure = AuthenticationFailure.INVALID_CREDENTIALS)
            logger.warning("Email signup response failure status=${e.code()} type=$failure")
            throw AuthenticationException(failure)
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
        preferAuthenticatedName: Boolean = false,
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
            name = if (preferAuthenticatedName) {
                authenticatedName
                    ?: value.user?.userMetadata?.fullName
                    ?: value.user?.userMetadata?.name
            } else {
                value.user?.userMetadata?.fullName
                    ?: value.user?.userMetadata?.name
                    ?: authenticatedName
            },
        )
    }

    private fun SupabaseSessionDto.hasSession() =
        !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()

    private fun HttpException.toAuthenticationFailure(
        defaultClientFailure: AuthenticationFailure,
    ): AuthenticationFailure {
        if (code() == 429) return AuthenticationFailure.RATE_LIMITED
        val errorCode = runCatching {
            response()?.errorBody()?.string()?.let(JsonParser::parseString)?.asJsonObject
                ?.get("error_code")?.asString
        }.getOrNull()
        return when (errorCode) {
            "email_not_confirmed" -> AuthenticationFailure.EMAIL_NOT_CONFIRMED
            "over_request_rate_limit", "over_email_send_rate_limit", "email_rate_limit_exceeded" ->
                AuthenticationFailure.RATE_LIMITED
            else -> if (code() in 400..499) defaultClientFailure else AuthenticationFailure.SERVER
        }
    }

    private companion object { val logger: Logger = Logger.getLogger("EkataYanAuth") }
}
