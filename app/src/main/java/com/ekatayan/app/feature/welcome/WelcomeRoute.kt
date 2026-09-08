package com.ekatayan.app.feature.welcome

import androidx.compose.runtime.Composable

@Composable
fun WelcomeRoute(onGetStarted: () -> Unit) {
    WelcomeScreen(onGetStarted = onGetStarted)
}
