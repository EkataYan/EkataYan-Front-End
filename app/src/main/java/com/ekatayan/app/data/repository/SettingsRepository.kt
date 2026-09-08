package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.SettingsPreferences

import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Local settings demo; preserves the existing ViewModel lifetime. */
class SettingsRepository @Inject constructor() {
    private val mutableState = MutableStateFlow(SettingsPreferences())
    val preferences = mutableState.asStateFlow()
    fun setPushNotificationsEnabled(enabled: Boolean) {
        mutableState.update { it.copy(pushNotificationsEnabled = enabled) }
    }
    fun setDarkModeEnabled(enabled: Boolean) {
        mutableState.update { it.copy(darkModeEnabled = enabled) }
    }
}
