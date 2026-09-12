package com.ekatayan.app.ui.welcome

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ekatayan.app.data.local.preferences.frontendPreferencesDataStore
import kotlinx.coroutines.flow.first

class WelcomePreferences(context: Context) {
    private val appContext = context.applicationContext

    suspend fun hasCompletedWelcome(): Boolean =
        appContext.frontendPreferencesDataStore.data.first()[WELCOME_COMPLETED] ?: false

    suspend fun markWelcomeCompleted() {
        appContext.frontendPreferencesDataStore.edit { it[WELCOME_COMPLETED] = true }
    }

    private companion object {
        val WELCOME_COMPLETED = booleanPreferencesKey("welcome_completed")
    }
}
