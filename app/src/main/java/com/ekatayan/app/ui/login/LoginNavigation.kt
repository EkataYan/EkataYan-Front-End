package com.ekatayan.app.ui.login

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val LOGIN_ROUTE = "login"

fun NavGraphBuilder.loginScreen(
    onLogInClick: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    composable(LOGIN_ROUTE) {
        LoginRoute(
            onLogInClick = onLogInClick,
            onSignUpClick = onSignUpClick,
        )
    }
}
