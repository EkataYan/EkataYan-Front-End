package com.ekatayan.app.ui.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashScreen(onSplashFinished: suspend () -> Unit) {
    composable(route = SPLASH_ROUTE) {
        SplashRoute(onSplashFinished = onSplashFinished)
    }
}
