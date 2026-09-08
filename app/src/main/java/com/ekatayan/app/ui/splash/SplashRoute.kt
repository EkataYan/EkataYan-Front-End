package com.ekatayan.app.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

private const val SPLASH_DISPLAY_DURATION_MILLIS = 1_500L

@Composable
fun SplashRoute(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DISPLAY_DURATION_MILLIS)
        onSplashFinished()
    }

    SplashScreen()
}
