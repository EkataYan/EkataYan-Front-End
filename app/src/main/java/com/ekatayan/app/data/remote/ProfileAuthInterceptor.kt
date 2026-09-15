package com.ekatayan.app.data.remote

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthenticationRequiredException : IOException("Authentication required")

class ProfileAuthInterceptor private constructor(
    private val tokenProvider: () -> String?,
) : Interceptor {
    @Inject constructor(refresh: SessionRefreshCoordinator) : this(refresh::tokenForRequest)
    internal constructor(session: UserSessionProvider) : this(session::currentAccessToken)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider() ?: throw AuthenticationRequiredException()
        return chain.proceed(chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build())
    }
}
