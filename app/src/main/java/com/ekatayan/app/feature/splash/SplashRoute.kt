package com.ekatayan.app.feature.splash
import androidx.compose.runtime.Composable
 
@Composable
fun SplashRoute(onSplashFinished: () -> Unit) {
    SplashScreen(onGetStartedClick = onSplashFinished)
}
