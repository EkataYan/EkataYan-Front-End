package com.ekatayan.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) = update { copy(email = value) }
    fun onPasswordChange(value: String) = update { copy(password = value) }
    fun onPasswordVisibilityClick() = update {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun onForgotPasswordClick() = Unit
    fun onGoogleClick() = Unit
    fun onAppleClick() = Unit

    private inline fun update(transform: LoginUiState.() -> LoginUiState) {
        uiState = uiState.transform()
    }
}
