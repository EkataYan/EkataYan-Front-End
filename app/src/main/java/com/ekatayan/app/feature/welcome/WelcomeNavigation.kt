package com.ekatayan.app.feature.welcome

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val WELCOME_ROUTE = "welcome"

fun NavGraphBuilder.welcomeScreen(onGetStarted: () -> Unit) {
    composable(WELCOME_ROUTE) {
        WelcomeRoute(onGetStarted = onGetStarted)
    }
}
