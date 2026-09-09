package com.ekatayan.app.data.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
    val email: String? = null,
)

interface SessionStore {
    fun read(): StoredSession?
    fun save(session: StoredSession)
    fun clear()
}

@Singleton
class EncryptedSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) : SessionStore {
    private val preferences = EncryptedSharedPreferences.create(
        context,
        "ekatayan_auth_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun read(): StoredSession? {
        val access = preferences.getString(ACCESS_TOKEN, null)
        val refresh = preferences.getString(REFRESH_TOKEN, null)
        val expiry = preferences.getLong(EXPIRES_AT, 0)
        val email = preferences.getString(EMAIL, null)
        return if (access.isNullOrBlank() || refresh.isNullOrBlank() || expiry <= 0) null
        else StoredSession(access, refresh, expiry, email)
    }

    override fun save(session: StoredSession) {
        preferences.edit().putString(ACCESS_TOKEN, session.accessToken)
            .putString(REFRESH_TOKEN, session.refreshToken).putLong(EXPIRES_AT, session.expiresAtMillis)
            .putString(EMAIL, session.email).apply()
    }

    override fun clear() { preferences.edit().clear().apply() }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val EXPIRES_AT = "expires_at"
        const val EMAIL = "email"
    }
}
