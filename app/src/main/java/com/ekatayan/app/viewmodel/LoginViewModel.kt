package com.ekatayan.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.AuthenticationException
import com.ekatayan.app.data.repository.AuthenticationFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: AuthenticationFailure? = null,
    val loginSucceeded: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private var signInJob: Job? = null

    init {
        restoreSession()
    }

    fun onEmailChange(value: String) = update { copy(email = value, error = null) }
    fun onPasswordChange(value: String) = update { copy(password = value, error = null) }
    fun onPasswordVisibilityClick() = update {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun onForgotPasswordClick() = Unit
    fun onGoogleClick() = Unit
    fun onAppleClick() = Unit

    fun signIn() {
        if (uiState.isLoading || signInJob?.isActive == true) return
        val email = uiState.email.trim()
        val password = uiState.password
        val error = when {
            email.isBlank() -> AuthenticationFailure.INVALID_CREDENTIALS
            password.isBlank() -> AuthenticationFailure.INVALID_CREDENTIALS
            else -> null
        }
        if (error != null) {
            uiState = uiState.copy(error = error)
            return
        }
        signInJob = viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, loginSucceeded = false)
            try {
                authRepository.signIn(email, password)
                uiState = uiState.copy(password = "", isLoading = false, loginSucceeded = true)
            } catch (e: AuthenticationException) {
                uiState = uiState.copy(isLoading = false, error = e.failure)
            }
        }
    }

    fun consumeLoginSuccess() = update { copy(loginSucceeded = false) }

    private fun restoreSession() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            if (authRepository.restoreSession()) {
                uiState = uiState.copy(isLoading = false, loginSucceeded = true)
            } else {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    private inline fun update(transform: LoginUiState.() -> LoginUiState) {
        uiState = uiState.transform()
    }
}
