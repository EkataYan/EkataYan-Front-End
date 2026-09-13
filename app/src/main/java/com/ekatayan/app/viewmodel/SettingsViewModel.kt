package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.SettingsRepository
import com.ekatayan.app.data.repository.AuthRepository

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val uiState = repository.preferences
    fun setPushNotificationsEnabled(enabled: Boolean) = repository.setPushNotificationsEnabled(enabled)
    fun setThemeMode(mode: String) = repository.setThemeMode(mode)
    fun setLanguage(code: String) = repository.setLanguage(code)
    fun markLocationPermissionPromptShown() = repository.markLocationPermissionPromptShown()
    fun logout() = authRepository.clearSession()
}
