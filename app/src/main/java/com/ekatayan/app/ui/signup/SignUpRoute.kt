package com.ekatayan.app.ui.signup

import com.ekatayan.app.viewmodel.SignUpViewModel
import com.ekatayan.app.viewmodel.AppLanguageViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SignUpRoute(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
    languageViewModel: AppLanguageViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val languageState by languageViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel.uiState.signUpSucceeded) {
        if (viewModel.uiState.signUpSucceeded) {
            viewModel.consumeSignUpSuccess()
            onSignUpClick()
        }
    }
    SignUpScreen(
        uiState = viewModel.uiState,
        selectedLanguage = languageState.selectedLanguage,
        onLanguageSelected = languageViewModel::setLanguage,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneNumberChange = viewModel::onPhoneNumberChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onPasswordVisibilityClick = viewModel::onPasswordVisibilityClick,
        onConfirmPasswordVisibilityClick = viewModel::onConfirmPasswordVisibilityClick,
        onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
        onSignUpClick = viewModel::signUp,
        onGoogleClick = { viewModel.onGoogleClick(context) },
        onLoginClick = onLoginClick,
    )
}
