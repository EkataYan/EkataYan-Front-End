package com.ekatayan.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ekatayan.app.data.local.preferences.frontendPreferencesDataStore
import com.ekatayan.app.data.model.SettingsPreferences
import com.ekatayan.app.core.localization.AppLocaleManager
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
    private val mutableState = MutableStateFlow(
        SettingsPreferences(
            selectedLanguage = AppLocaleManager.currentLanguage() ?: AppLocaleManager.ENGLISH,
        ),
    )
    val preferences = mutableState.asStateFlow()

    init {
        if (!testMode && context != null) scope.launch {
            context.frontendPreferencesDataStore.data
                .map { values -> SettingsPreferences(
                    loaded = true,
                    pushNotificationsEnabled = values[PUSH_NOTIFICATIONS] ?: true,
                    themeMode = values[THEME_MODE] ?: if (values[DARK_MODE] == true) "dark" else "system",
                    selectedLanguage = AppLocaleManager.sanitize(values[LANGUAGE] ?: AppLocaleManager.ENGLISH),
                    locationPermissionPromptShown = values[LOCATION_PROMPT_SHOWN] ?: false,
                ) }
                .catch {
                    emit(
                        SettingsPreferences(
                            loaded = true,
                            selectedLanguage = AppLocaleManager.currentLanguage() ?: AppLocaleManager.ENGLISH,
                        ),
                    )
                }
                .collect { mutableState.value = it }
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(pushNotificationsEnabled = enabled)
        context?.let { scope.launch { it.frontendPreferencesDataStore.edit { values -> values[PUSH_NOTIFICATIONS] = enabled } } }
    }

    fun setThemeMode(mode: String) {
        val safe = mode.takeIf { it in setOf("system", "light", "dark") } ?: "system"
        mutableState.value = mutableState.value.copy(themeMode = safe)
        context?.let { scope.launch { it.frontendPreferencesDataStore.edit { values -> values[THEME_MODE] = safe } } }
    }

    fun setLanguage(code: String) {
        val safe = AppLocaleManager.sanitize(code)
        mutableState.value = mutableState.value.copy(selectedLanguage = safe)
        context?.let { scope.launch { it.frontendPreferencesDataStore.edit { values -> values[LANGUAGE] = safe } } }
        AppLocaleManager.applyLanguage(safe)
    }

    fun markLocationPermissionPromptShown() {
        mutableState.value = mutableState.value.copy(locationPermissionPromptShown = true)
        context?.let { scope.launch { it.frontendPreferencesDataStore.edit { values -> values[LOCATION_PROMPT_SHOWN] = true } } }
    }

    private companion object {
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language_code")
        val LOCATION_PROMPT_SHOWN = booleanPreferencesKey("location_permission_prompt_shown")
    }
}
