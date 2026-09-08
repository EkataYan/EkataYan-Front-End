package com.ekatayan.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val hasAcceptedTerms: Boolean = false,
)

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun onNameChange(value: String) = update { copy(name = value) }
    fun onEmailChange(value: String) = update { copy(email = value) }
    fun onPhoneNumberChange(value: String) = update { copy(phoneNumber = value) }
    fun onPasswordChange(value: String) = update { copy(password = value) }
    fun onConfirmPasswordChange(value: String) = update { copy(confirmPassword = value) }
    fun onPasswordVisibilityClick() = update { copy(isPasswordVisible = !isPasswordVisible) }
    fun onConfirmPasswordVisibilityClick() = update {
        copy(isConfirmPasswordVisible = !isConfirmPasswordVisible)
    }
    fun onTermsAcceptedChange(value: Boolean) = update { copy(hasAcceptedTerms = value) }

    fun onGoogleClick() = Unit
    fun onAppleClick() = Unit

    private inline fun update(transform: SignUpUiState.() -> SignUpUiState) {
        uiState = uiState.transform()
    }
}
