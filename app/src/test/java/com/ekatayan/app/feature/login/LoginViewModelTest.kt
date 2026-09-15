package com.ekatayan.app.feature.login

import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.AuthenticationException
import com.ekatayan.app.data.repository.AuthenticationFailure
import com.ekatayan.app.viewmodel.LoginViewModel
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
class LoginViewModelTest {
    @Test fun credentialsUpdateUiState() = withMainDispatcher {
        val viewModel = LoginViewModel(FakeAuthRepository())
        viewModel.onEmailChange("traveler@example.com")
        viewModel.onPasswordChange("secret")
        assertEquals("traveler@example.com", viewModel.uiState.email)
        assertEquals("secret", viewModel.uiState.password)
    }

    @Test fun passwordVisibilityToggles() = withMainDispatcher {
        val viewModel = LoginViewModel(FakeAuthRepository())
        assertFalse(viewModel.uiState.isPasswordVisible)
        viewModel.onPasswordVisibilityClick()
        assertTrue(viewModel.uiState.isPasswordVisible)
    }

    @Test fun successfulLoginEmitsNavigationOnlyAfterRepositorySucceeds() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val signInGate = CompletableDeferred<Unit>()
            val repository = FakeAuthRepository(signInGate = signInGate)
            val viewModel = LoginViewModel(repository)
            advanceUntilIdle()
            viewModel.onEmailChange("traveler@example.com")
            viewModel.onPasswordChange("password")
            viewModel.signIn()
            runCurrent()
            assertTrue(viewModel.uiState.isLoading)
            assertFalse(viewModel.uiState.loginSucceeded)
            signInGate.complete(Unit)
            advanceUntilIdle()
            assertEquals(1, repository.signInCalls)
            assertTrue(viewModel.uiState.loginSucceeded)
            assertFalse(viewModel.uiState.isLoading)
            viewModel.consumeLoginSuccess()
            assertFalse(viewModel.uiState.loginSucceeded)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun failedLoginDoesNotEmitNavigationAndDuplicateRequestsAreBlocked() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeAuthRepository(AuthenticationFailure.INVALID_CREDENTIALS)
            val viewModel = LoginViewModel(repository)
            advanceUntilIdle()
            viewModel.onEmailChange("traveler@example.com")
            viewModel.onPasswordChange("wrong")
            viewModel.signIn(); viewModel.signIn()
            advanceUntilIdle()
            assertEquals(1, repository.signInCalls)
            assertEquals(AuthenticationFailure.INVALID_CREDENTIALS, viewModel.uiState.error)
            assertFalse(viewModel.uiState.loginSucceeded)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun blankCredentialsDoNotStartAuthentication() = withMainDispatcher {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository)
        viewModel.signIn()
        assertEquals(0, repository.signInCalls)
        assertEquals(AuthenticationFailure.INVALID_CREDENTIALS, viewModel.uiState.error)
    }

    @Test fun passwordRecoveryRequiresAValidEmailAndReportsSuccess() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeAuthRepository()
            val viewModel = LoginViewModel(repository)
            viewModel.onForgotPasswordClick()
            assertEquals(0, repository.passwordRecoveryCalls)
            viewModel.onEmailChange("traveler@example.com")
            viewModel.onForgotPasswordClick()
            advanceUntilIdle()
            assertEquals(1, repository.passwordRecoveryCalls)
            assertTrue(viewModel.uiState.passwordResetSent)
            assertFalse(viewModel.uiState.isPasswordResetLoading)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun loginDoesNotRestoreSessionOrFlashAuthenticatedNavigation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeAuthRepository(restored = true)
            val viewModel = LoginViewModel(repository)
            advanceUntilIdle()
            assertEquals(0, repository.restoreCalls)
            assertFalse(viewModel.uiState.loginSucceeded)
        } finally { Dispatchers.resetMain() }
    }
}

private class FakeAuthRepository(
    private val failure: AuthenticationFailure? = null,
    private val restored: Boolean = false,
    private val signInGate: CompletableDeferred<Unit>? = null,
) : AuthRepository {
    var signInCalls = 0
    var passwordRecoveryCalls = 0
    var restoreCalls = 0
    override suspend fun signIn(email: String, password: String) {
        signInCalls++
        failure?.let { throw AuthenticationException(it) }
        signInGate?.await()
    }
    override suspend fun requestPasswordRecovery(email: String) { passwordRecoveryCalls++ }
    override suspend fun signUp(name: String, email: String, phone: String, password: String) = error("Not used by LoginViewModel")
    override suspend fun restoreSession(): Boolean { restoreCalls++; return restored }
    override suspend fun refreshSession() = false
    override fun clearSession() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun withMainDispatcher(block: () -> Unit) {
    Dispatchers.setMain(StandardTestDispatcher())
    try { block() } finally { Dispatchers.resetMain() }
}
