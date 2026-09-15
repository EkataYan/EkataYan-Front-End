package com.ekatayan.app.data.remote

import com.google.gson.JsonParser
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class ApiCallException(message: String, cause: Throwable? = null) : IOException(message, cause)

suspend fun <T> apiCall(fallback: String, request: suspend () -> T): T = try {
    request()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (error: HttpException) {
    val providerMessage = runCatching {
        error.response()?.errorBody()?.string()?.let(JsonParser::parseString)?.asJsonObject
            ?.getAsJsonObject("error")?.get("message")?.asString
            ?.trim()?.takeIf(String::isNotEmpty)?.take(300)
    }.getOrNull()
    val message = providerMessage ?: when (error.code()) {
        400, 422 -> "Some information was invalid. Check it and try again."
        401 -> "Your session has expired. Please sign in again."
        403 -> "You do not have permission to do that."
        404 -> "That item no longer exists. Refresh and try again."
        409 -> "That change conflicts with existing data. Refresh and try again."
        429 -> "Too many requests. Wait a moment and try again."
        500, 502, 503 -> "The service is temporarily unavailable. Try again shortly."
        else -> fallback
    }
    throw ApiCallException(message, error)
} catch (error: AuthenticationRequiredException) {
    throw ApiCallException("Your session has expired. Please sign in again.", error)
} catch (error: IOException) {
    throw ApiCallException("Unable to connect. Check your internet connection and try again.", error)
} catch (error: Exception) {
    throw ApiCallException(fallback, error)
}
