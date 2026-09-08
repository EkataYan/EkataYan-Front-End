package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.SettingsRepository

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val uiState = repository.preferences
    fun setPushNotificationsEnabled(enabled: Boolean) = repository.setPushNotificationsEnabled(enabled)
    fun setDarkModeEnabled(enabled: Boolean) = repository.setDarkModeEnabled(enabled)
}
