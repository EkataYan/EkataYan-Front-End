package com.ekatayan.app.data.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class SettingsPreferences(
    val loaded: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    @param:DrawableRes val profileImageResId: Int? = null,
    val selectedLanguage: String = "en",
    val selectedCurrency: String = "LKR - Sri Lankan Rupees",
    val pushNotificationsEnabled: Boolean = true,
    val themeMode: String = "system",
    val locationPermissionPromptShown: Boolean = false,
    val appVersion: String = "App Version 1.0.0",
)

