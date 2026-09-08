package com.ekatayan.app.ui.welcome

import androidx.compose.runtime.Composable

@Composable
fun WelcomeRoute(onGetStarted: () -> Unit) {
    WelcomeScreen(onGetStarted = onGetStarted)
}
