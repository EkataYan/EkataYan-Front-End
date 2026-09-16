package com.ekatayan.app.ui.login

import com.ekatayan.app.viewmodel.LoginViewModel
import com.ekatayan.app.viewmodel.AppLanguageViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginRoute(
    onLogInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    languageViewModel: AppLanguageViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val languageState by languageViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel.uiState.loginSucceeded) {
        if (viewModel.uiState.loginSucceeded) {
            viewModel.consumeLoginSuccess()
            onLogInClick()
        }
    }
    LoginScreen(
        uiState = viewModel.uiState,
        selectedLanguage = languageState.selectedLanguage,
        onLanguageSelected = languageViewModel::setLanguage,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityClick = viewModel::onPasswordVisibilityClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onLogInClick = viewModel::signIn,
        onGoogleClick = { viewModel.onGoogleClick(context) },
        onSignUpClick = onSignUpClick,
    )
}
