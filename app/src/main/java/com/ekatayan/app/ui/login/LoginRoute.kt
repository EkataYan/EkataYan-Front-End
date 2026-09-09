package com.ekatayan.app.ui.login

import com.ekatayan.app.viewmodel.LoginViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun LoginRoute(
    onLogInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel.uiState.loginSucceeded) {
        if (viewModel.uiState.loginSucceeded) {
            viewModel.consumeLoginSuccess()
            onLogInClick()
        }
    }
    LoginScreen(
        uiState = viewModel.uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityClick = viewModel::onPasswordVisibilityClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onLogInClick = viewModel::signIn,
        onGoogleClick = viewModel::onGoogleClick,
        onAppleClick = viewModel::onAppleClick,
        onSignUpClick = onSignUpClick,
    )
}
