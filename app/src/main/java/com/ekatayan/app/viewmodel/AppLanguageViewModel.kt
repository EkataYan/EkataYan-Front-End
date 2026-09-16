package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import com.ekatayan.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppLanguageViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = settingsRepository.preferences

    fun setLanguage(languageCode: String) = settingsRepository.setLanguage(languageCode)
}
