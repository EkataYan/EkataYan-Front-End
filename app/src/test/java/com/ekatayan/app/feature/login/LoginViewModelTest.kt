package com.ekatayan.app.feature.login

import com.ekatayan.app.viewmodel.LoginViewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {
    @Test
    fun credentialsUpdateUiState() {
        val viewModel = LoginViewModel()

        viewModel.onEmailChange("traveler@example.com")
        viewModel.onPasswordChange("secret")

        assertEquals("traveler@example.com", viewModel.uiState.email)
        assertEquals("secret", viewModel.uiState.password)
    }

    @Test
    fun passwordVisibilityToggles() {
        val viewModel = LoginViewModel()

        assertFalse(viewModel.uiState.isPasswordVisible)
        viewModel.onPasswordVisibilityClick()
        assertTrue(viewModel.uiState.isPasswordVisible)
    }
}
