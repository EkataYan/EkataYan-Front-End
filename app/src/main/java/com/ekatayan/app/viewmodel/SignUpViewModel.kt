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
import com.ekatayan.app.data.repository.SignUpResult
import com.ekatayan.app.data.auth.GoogleCredentialException
import com.ekatayan.app.data.auth.GoogleCredentialProvider
import com.ekatayan.app.data.auth.UnavailableGoogleCredentialProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class SignUpValidationError { REQUIRED_FIELDS, INVALID_EMAIL, WEAK_PASSWORD, PASSWORD_MISMATCH, TERMS_REQUIRED }

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val hasAcceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val validationError: SignUpValidationError? = null,
    val authenticationError: AuthenticationFailure? = null,
    val signUpSucceeded: Boolean = false,
    val emailConfirmationRequired: Boolean = false,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleCredentials: GoogleCredentialProvider = UnavailableGoogleCredentialProvider(),
) : ViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    private var signUpJob: Job? = null

    fun onNameChange(value: String) = update { copy(name = value, validationError = null, authenticationError = null) }
    fun onEmailChange(value: String) = update { copy(email = value, validationError = null, authenticationError = null) }
    fun onPhoneNumberChange(value: String) = update { copy(phoneNumber = value, validationError = null, authenticationError = null) }
    fun onPasswordChange(value: String) = update { copy(password = value, validationError = null, authenticationError = null) }
    fun onConfirmPasswordChange(value: String) = update { copy(confirmPassword = value, validationError = null, authenticationError = null) }
    fun onPasswordVisibilityClick() = update { copy(isPasswordVisible = !isPasswordVisible) }
    fun onConfirmPasswordVisibilityClick() = update {
        copy(isConfirmPasswordVisible = !isConfirmPasswordVisible)
    }
    fun onTermsAcceptedChange(value: Boolean) = update { copy(hasAcceptedTerms = value, validationError = null) }

    fun onGoogleClick(context: Context) {
        if (uiState.isLoading || signUpJob?.isActive == true) return
        signUpJob = viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, validationError = null, authenticationError = null, signUpSucceeded = false)
            try {
                val credential = googleCredentials.getCredential(context)
                authRepository.signInWithGoogle(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                    displayName = credential.displayName,
                    avatarUrl = credential.profilePictureUrl,
                )
                uiState = uiState.copy(isLoading = false, signUpSucceeded = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: GoogleCredentialException) {
                uiState = uiState.copy(isLoading = false, authenticationError = e.reason.toAuthenticationFailure())
            } catch (e: AuthenticationException) {
                uiState = uiState.copy(isLoading = false, authenticationError = e.failure)
            }
        }
    }

    fun signUp() {
        if (uiState.isLoading || signUpJob?.isActive == true) return
        val name = uiState.name.trim()
        val email = uiState.email.trim()
        val phone = uiState.phoneNumber.trim()
        val validation = when {
            name.isBlank() || email.isBlank() || phone.isBlank() || uiState.password.isBlank() || uiState.confirmPassword.isBlank() -> SignUpValidationError.REQUIRED_FIELDS
            !EMAIL_PATTERN.matches(email) -> SignUpValidationError.INVALID_EMAIL
            uiState.password.length < MINIMUM_PASSWORD_LENGTH -> SignUpValidationError.WEAK_PASSWORD
            uiState.password != uiState.confirmPassword -> SignUpValidationError.PASSWORD_MISMATCH
            !uiState.hasAcceptedTerms -> SignUpValidationError.TERMS_REQUIRED
            else -> null
        }
        if (validation != null) {
            uiState = uiState.copy(validationError = validation, authenticationError = null)
            return
        }
        val password = uiState.password
        signUpJob = viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, validationError = null, authenticationError = null, signUpSucceeded = false, emailConfirmationRequired = false)
            try {
                when (authRepository.signUp(name, email, phone, password)) {
                    SignUpResult.AUTHENTICATED -> uiState = uiState.copy(password = "", confirmPassword = "", isLoading = false, signUpSucceeded = true)
                    SignUpResult.EMAIL_CONFIRMATION_REQUIRED -> uiState = uiState.copy(password = "", confirmPassword = "", isLoading = false, emailConfirmationRequired = true)
                }
            } catch (e: AuthenticationException) {
                uiState = uiState.copy(isLoading = false, authenticationError = e.failure)
            }
        }
    }

    fun consumeSignUpSuccess() = update { copy(signUpSucceeded = false) }

    private inline fun update(transform: SignUpUiState.() -> SignUpUiState) {
        uiState = uiState.transform()
    }

    private fun GoogleCredentialException.Reason.toAuthenticationFailure() = when (this) {
        GoogleCredentialException.Reason.CANCELED -> AuthenticationFailure.GOOGLE_CANCELED
        GoogleCredentialException.Reason.NO_CREDENTIAL -> AuthenticationFailure.GOOGLE_NO_CREDENTIAL
        GoogleCredentialException.Reason.INVALID_TOKEN -> AuthenticationFailure.GOOGLE_INVALID_TOKEN
        GoogleCredentialException.Reason.CONFIGURATION -> AuthenticationFailure.CONFIGURATION
        GoogleCredentialException.Reason.UNKNOWN -> AuthenticationFailure.SERVER
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        const val MINIMUM_PASSWORD_LENGTH = 6
    }
}
