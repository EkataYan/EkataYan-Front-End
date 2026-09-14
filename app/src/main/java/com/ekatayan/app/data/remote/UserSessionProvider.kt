package com.ekatayan.app.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps the current session in memory and restores encrypted tokens after process restart. */
@Singleton
class UserSessionProvider @Inject constructor(private val store: SessionStore) {
    @Volatile private var session: StoredSession? = store.read()
    private val mutableUserName = MutableStateFlow(session?.name)
    val userName = mutableUserName.asStateFlow()
    private val mutableAccessToken = MutableStateFlow(currentAccessToken())
    val accessToken = mutableAccessToken.asStateFlow()

    fun currentAccessToken(): String? = session?.takeIf {
        it.expiresAtMillis > System.currentTimeMillis()
    }?.accessToken

    fun setSession(
        accessToken: String,
        refreshToken: String,
        expiresAtMillis: Long,
        email: String? = null,
        name: String? = null,
    ) {
        require(accessToken.isNotBlank() && refreshToken.isNotBlank())
        StoredSession(
            accessToken,
            refreshToken,
            expiresAtMillis,
            email?.trim()?.takeIf(String::isNotEmpty),
            name?.trim()?.takeIf(String::isNotEmpty),
        ).also {
            session = it
            mutableUserName.value = it.name
            mutableAccessToken.value = it.accessToken
            store.save(it)
        }
    }

    fun refreshToken(): String? = session?.refreshToken

    fun currentUserEmail(): String? = session?.email

    fun currentUserName(): String? = session?.name

    fun updateUserName(name: String) {
        val current = session ?: return
        current.copy(name = name.trim().takeIf(String::isNotEmpty)).also {
            session = it
            mutableUserName.value = it.name
            store.save(it)
        }
    }

    fun clearSession() {
        session = null
        mutableUserName.value = null
        mutableAccessToken.value = null
        store.clear()
    }
}
