package com.ekatayan.app.data.remote

import com.ekatayan.app.BuildConfig
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import okhttp3.Interceptor
import okhttp3.Response

/** Debug-only auth telemetry. Request bodies, emails, passwords, headers, and tokens are never logged. */
class AuthDiagnosticsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val operation = when {
            request.url.encodedPath.endsWith("/signup") -> "email_signup"
            request.url.encodedPath.endsWith("/user") -> "user_update"
            request.url.queryParameter("grant_type") == "password" -> "email_login"
            request.url.queryParameter("grant_type") == "id_token" -> "google_token_exchange"
            request.url.queryParameter("grant_type") == "refresh_token" -> "session_refresh"
            else -> "auth_request"
        }
        val startedAt = System.nanoTime()
        debug("$operation request_started host=${request.url.host}")
        return try {
            chain.proceed(request).also { response ->
                val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
                debug("$operation response status=${response.code} duration_ms=$elapsedMs")
            }
        } catch (error: Exception) {
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
            if (BuildConfig.DEBUG) {
                logger.log(Level.WARNING, "$operation transport_failure duration_ms=$elapsedMs type=${error.javaClass.simpleName}")
            }
            throw error
        }
    }

    private fun debug(message: String) {
        if (BuildConfig.DEBUG) logger.info(message)
    }

    private companion object {
        val logger: Logger = Logger.getLogger("EkataYanAuth")
    }
}
