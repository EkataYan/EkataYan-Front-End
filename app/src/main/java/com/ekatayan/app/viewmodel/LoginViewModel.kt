package com.ekatayan.app.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.AuthenticationException
import com.ekatayan.app.data.repository.AuthenticationFailure
import com.ekatayan.app.data.auth.GoogleCredentialException
import com.ekatayan.app.data.auth.GoogleCredentialProvider
import com.ekatayan.app.data.auth.UnavailableGoogleCredentialProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleCredentials: GoogleCredentialProvider = UnavailableGoogleCredentialProvider(),
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private var signInJob: Job? = null

    fun onEmailChange(value: String) = update { copy(email = value, error = null) }
    fun onPasswordChange(value: String) = update { copy(password = value, error = null) }
    fun onPasswordVisibilityClick() = update {
        copy(isPasswordVisible = !isPasswordVisible)
    }

    fun onForgotPasswordClick() = Unit
    fun onGoogleClick(context: Context) {
        if (uiState.isLoading || signInJob?.isActive == true) return
        signInJob = viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, loginSucceeded = false)
            try {
                val credential = googleCredentials.getCredential(context)
                authRepository.signInWithGoogle(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                    displayName = credential.displayName,
                    avatarUrl = credential.profilePictureUrl,
                )
                uiState = uiState.copy(isLoading = false, loginSucceeded = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: GoogleCredentialException) {
                uiState = uiState.copy(isLoading = false, error = e.reason.toAuthenticationFailure())
            } catch (e: AuthenticationException) {
                uiState = uiState.copy(isLoading = false, error = e.failure)
            }
        }
    }

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

    private inline fun update(transform: LoginUiState.() -> LoginUiState) {
        uiState = uiState.transform()
    }

    private fun GoogleCredentialException.Reason.toAuthenticationFailure() = when (this) {
        GoogleCredentialException.Reason.CANCELED -> AuthenticationFailure.GOOGLE_CANCELED
        GoogleCredentialException.Reason.NO_CREDENTIAL -> AuthenticationFailure.GOOGLE_NO_CREDENTIAL
        GoogleCredentialException.Reason.INVALID_TOKEN -> AuthenticationFailure.GOOGLE_INVALID_TOKEN
        GoogleCredentialException.Reason.CONFIGURATION -> AuthenticationFailure.CONFIGURATION
        GoogleCredentialException.Reason.UNKNOWN -> AuthenticationFailure.SERVER
    }
}
