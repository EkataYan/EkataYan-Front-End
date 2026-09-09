package com.ekatayan.app.ui.signup

import com.ekatayan.app.viewmodel.SignUpViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SignUpRoute(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel.uiState.signUpSucceeded) {
        if (viewModel.uiState.signUpSucceeded) {
            viewModel.consumeSignUpSuccess()
            onSignUpClick()
        }
    }
    SignUpScreen(
        uiState = viewModel.uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onPasswordVisibilityClick = viewModel::onPasswordVisibilityClick,
        onConfirmPasswordVisibilityClick = viewModel::onConfirmPasswordVisibilityClick,
        onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
        onSignUpClick = viewModel::signUp,
        onGoogleClick = viewModel::onGoogleClick,
        onAppleClick = viewModel::onAppleClick,
        onLoginClick = onLoginClick,
    )
}
