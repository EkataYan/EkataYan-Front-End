package com.ekatayan.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ekatayan.app.data.local.preferences.frontendPreferencesDataStore
import com.ekatayan.app.data.model.SettingsPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class SettingsRepository private constructor(private val context: Context?, testMode: Boolean) {
    @Inject constructor(@ApplicationContext context: Context) : this(context, false)
    constructor() : this(null, true)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(SettingsPreferences())
    val preferences = mutableState.asStateFlow()

    init {
        if (!testMode && context != null) scope.launch {
            context.frontendPreferencesDataStore.data
                .map { values -> SettingsPreferences(
                    pushNotificationsEnabled = values[PUSH_NOTIFICATIONS] ?: true,
                    darkModeEnabled = values[DARK_MODE] ?: false,
                ) }
                .catch { emit(SettingsPreferences()) }
                .collect { mutableState.value = it }
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(pushNotificationsEnabled = enabled)
        context?.let { scope.launch { it.frontendPreferencesDataStore.edit { values -> values[PUSH_NOTIFICATIONS] = enabled } } }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(darkModeEnabled = enabled)
        context?.let { scope.launch { it.frontendPreferencesDataStore.edit { values -> values[DARK_MODE] = enabled } } }
    }

    private companion object {
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    }
}
