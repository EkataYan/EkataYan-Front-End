package com.ekatayan.app.feature.signup

import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.AuthenticationException
import com.ekatayan.app.data.repository.AuthenticationFailure
import com.ekatayan.app.data.repository.SignUpResult
import com.ekatayan.app.viewmodel.SignUpValidationError
import com.ekatayan.app.viewmodel.SignUpViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {
    @Test fun validSignUpNavigatesOnlyAfterAnAuthenticatedResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeSignUpRepository(gate = gate)
            val viewModel = SignUpViewModel(repository).also { it.populateValidForm() }

            viewModel.signUp()
            runCurrent()
            assertTrue(viewModel.uiState.isLoading)
            assertFalse(viewModel.uiState.signUpSucceeded)
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, repository.signUpCalls)
            assertEquals("New Traveler", repository.submittedName)
            assertTrue(viewModel.uiState.signUpSucceeded)
            assertFalse(viewModel.uiState.emailConfirmationRequired)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun mismatchedPasswordsDoNotCallSupabase() = withMainDispatcher {
        val repository = FakeSignUpRepository()
        val viewModel = SignUpViewModel(repository).also { it.populateValidForm() }
        viewModel.onConfirmPasswordChange("different")

        viewModel.signUp()

        assertEquals(SignUpValidationError.PASSWORD_MISMATCH, viewModel.uiState.validationError)
        assertEquals(0, repository.signUpCalls)
    }

    @Test fun blankNameDoesNotCallSupabase() = withMainDispatcher {
        val repository = FakeSignUpRepository()
        val viewModel = SignUpViewModel(repository).also { it.populateValidForm() }
        viewModel.onNameChange("   ")

        viewModel.signUp()

        assertEquals(SignUpValidationError.REQUIRED_FIELDS, viewModel.uiState.validationError)
        assertEquals(0, repository.signUpCalls)
    }

    @Test fun failedSignUpDoesNotEmitHomeNavigation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = SignUpViewModel(FakeSignUpRepository(failure = AuthenticationFailure.INVALID_CREDENTIALS)).also { it.populateValidForm() }
            viewModel.signUp()
            advanceUntilIdle()

            assertEquals(AuthenticationFailure.INVALID_CREDENTIALS, viewModel.uiState.authenticationError)
            assertFalse(viewModel.uiState.signUpSucceeded)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun emailConfirmationDoesNotEmitAuthenticatedNavigation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = SignUpViewModel(FakeSignUpRepository(result = SignUpResult.EMAIL_CONFIRMATION_REQUIRED)).also { it.populateValidForm() }
            viewModel.signUp()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.emailConfirmationRequired)
            assertFalse(viewModel.uiState.signUpSucceeded)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun duplicateSignUpRequestsAreBlockedWhileLoading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeSignUpRepository(gate = gate)
            val viewModel = SignUpViewModel(repository).also { it.populateValidForm() }
            viewModel.signUp(); viewModel.signUp()
            runCurrent()
            assertEquals(1, repository.signUpCalls)
            gate.complete(Unit)
            advanceUntilIdle()
        } finally { Dispatchers.resetMain() }
    }
}

private fun SignUpViewModel.populateValidForm() {
    onNameChange("New Traveler")
    onEmailChange("new@example.com")
    onPhoneNumberChange("+94771234567")
    onPasswordChange("password")
    onConfirmPasswordChange("password")
    onTermsAcceptedChange(true)
}

private class FakeSignUpRepository(
    private val result: SignUpResult = SignUpResult.AUTHENTICATED,
    private val failure: AuthenticationFailure? = null,
    private val gate: CompletableDeferred<Unit>? = null,
) : AuthRepository {
    var signUpCalls = 0
    var submittedName: String? = null
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun signUp(name: String, email: String, phone: String, password: String): SignUpResult {
        signUpCalls++
        submittedName = name
        failure?.let { throw AuthenticationException(it) }
        gate?.await()
        return result
    }
    override suspend fun restoreSession() = false
    override suspend fun refreshSession() = false
    override fun clearSession() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun withMainDispatcher(block: () -> Unit) {
    Dispatchers.setMain(StandardTestDispatcher())
    try { block() } finally { Dispatchers.resetMain() }
}
