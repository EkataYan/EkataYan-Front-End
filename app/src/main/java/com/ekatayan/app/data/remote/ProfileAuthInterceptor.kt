package com.ekatayan.app.data.remote

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthenticationRequiredException : IOException("Authentication required")

class ProfileAuthInterceptor @Inject constructor(
    private val session: UserSessionProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.currentAccessToken() ?: throw AuthenticationRequiredException()
        return chain.proceed(chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build())
    }
}
