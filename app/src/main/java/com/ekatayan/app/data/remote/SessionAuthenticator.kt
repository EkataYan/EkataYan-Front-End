package com.ekatayan.app.data.remote

import javax.inject.Inject
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class SessionAuthenticator @Inject constructor(
    private val refresh: SessionRefreshCoordinator,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.retryCount() >= 2) return null
        val rejected = response.request.header("Authorization")?.removePrefix("Bearer ") ?: return null
        val replacement = refresh.tokenForRequest(forceRefreshOf = rejected) ?: return null
        if (replacement == rejected) return null
        return response.request.newBuilder().header("Authorization", "Bearer $replacement").build()
    }

    private fun Response.retryCount(): Int {
        var count = 1
        var previous = priorResponse
        while (previous != null) {
            count++
            previous = previous.priorResponse
        }
        return count
    }
}
