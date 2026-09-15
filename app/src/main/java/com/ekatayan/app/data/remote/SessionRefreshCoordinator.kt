package com.ekatayan.app.data.remote

import com.ekatayan.app.data.repository.AuthRepository
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

/** Serializes token refreshes made by concurrent OkHttp calls. */
@Singleton
class SessionRefreshCoordinator @Inject constructor(
    private val session: UserSessionProvider,
    private val authRepository: Lazy<AuthRepository>,
) {
    private val lock = Any()

    fun tokenForRequest(forceRefreshOf: String? = null): String? = synchronized(lock) {
        val current = session.currentAccessToken()
        if (current != null && (forceRefreshOf == null || current != forceRefreshOf)) return@synchronized current
        if (forceRefreshOf == null && current != null) return@synchronized current
        if (session.refreshToken().isNullOrBlank()) return@synchronized null
        if (!runBlocking { authRepository.get().refreshSession() }) return@synchronized null
        session.currentAccessToken()
    }
}
