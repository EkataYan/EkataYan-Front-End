package com.ekatayan.app.data.local

import android.content.Context
import android.util.Base64
import com.ekatayan.app.data.local.database.EkataYanDatabase
import com.ekatayan.app.data.remote.UserSessionProvider
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Prevents account-owned Room rows from crossing an authentication boundary. */
@Singleton
class AccountDataIsolation @Inject constructor(
    @ApplicationContext context: Context,
    private val session: UserSessionProvider,
    private val database: EkataYanDatabase,
) {
    private val preferences = context.getSharedPreferences("ekatayan_local_account", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            session.accessToken.collectLatest { token ->
                val currentUser = token?.subjectUuid()
                val cachedUser = preferences.getString(ACCOUNT_ID, null)
                if (currentUser == null || cachedUser != currentUser) {
                    clearAccountTables()
                }
                preferences.edit().apply {
                    if (currentUser == null) remove(ACCOUNT_ID) else putString(ACCOUNT_ID, currentUser)
                }.apply()
            }
        }
    }

    /** Forces Hilt to construct this process-wide guard at application startup. */
    fun start() = Unit

    suspend fun clearNow() {
        clearAccountTables()
        preferences.edit().remove(ACCOUNT_ID).apply()
    }

    private suspend fun clearAccountTables() {
        database.wishlistDao().clearAccountData()
        database.tripsDao().clearAccountData()
        database.groupHubDao().clearAccountData()
    }

    private fun String.subjectUuid(): String? = runCatching {
        val encoded = split('.')[1]
        val decoded = String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        JsonParser.parseString(decoded).asJsonObject.get("sub").asString.also(UUID::fromString)
    }.getOrNull()

    private companion object { const val ACCOUNT_ID = "account_id" }
}
