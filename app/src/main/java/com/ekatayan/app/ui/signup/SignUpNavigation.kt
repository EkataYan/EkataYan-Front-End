package com.ekatayan.app.ui.signup

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SIGN_UP_ROUTE = "signup"

fun NavGraphBuilder.signUpScreen(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    composable(SIGN_UP_ROUTE) {
        SignUpRoute(
            onSignUpClick = onSignUpClick,
            onLoginClick = onLoginClick,
        )
    }
}
