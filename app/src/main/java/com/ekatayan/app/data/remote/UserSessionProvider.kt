package com.ekatayan.app.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/** Keeps the current session in memory and restores encrypted tokens after process restart. */
@Singleton
class UserSessionProvider @Inject constructor(private val store: SessionStore) {
    @Volatile private var session: StoredSession? = store.read()

    fun currentAccessToken(): String? = session?.takeIf {
        it.expiresAtMillis > System.currentTimeMillis()
    }?.accessToken

    fun setSession(accessToken: String, refreshToken: String, expiresAtMillis: Long, email: String? = null) {
        require(accessToken.isNotBlank() && refreshToken.isNotBlank())
        StoredSession(accessToken, refreshToken, expiresAtMillis, email?.trim()?.takeIf(String::isNotEmpty)).also {
            session = it
            store.save(it)
        }
    }

    fun refreshToken(): String? = session?.refreshToken

    fun currentUserEmail(): String? = session?.email

    fun clearSession() {
        session = null
        store.clear()
    }
}
