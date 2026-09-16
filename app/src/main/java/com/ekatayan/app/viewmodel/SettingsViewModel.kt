package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.SettingsRepository
import com.ekatayan.app.data.repository.AuthRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.local.AccountDataIsolation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val accountDataIsolation: AccountDataIsolation,
) : ViewModel() {
    val uiState = repository.preferences
    fun setPushNotificationsEnabled(enabled: Boolean) = repository.setPushNotificationsEnabled(enabled)
    fun setThemeMode(mode: String) = repository.setThemeMode(mode)
    fun setLanguage(code: String) = repository.setLanguage(code)
    fun markLocationPermissionPromptShown() = repository.markLocationPermissionPromptShown()
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Local collections are not cloud-backed/account-scoped yet. Clear them before
            // another account can sign in; authenticated trips reload from the backend.
            withContext(Dispatchers.IO) { accountDataIsolation.clearNow() }
            authRepository.clearSession()
            onComplete()
        }
    }
}
