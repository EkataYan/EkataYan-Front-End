package com.ekatayan.app.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ekatayan.app.viewmodel.SplashViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val SPLASH_DISPLAY_DURATION_MILLIS = 1_500L

@Composable
fun SplashRoute(
    onSplashFinished: suspend (authenticated: Boolean) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        val authenticated = coroutineScope {
            val sessionCheck = async { viewModel.restoreSession() }
            delay(SPLASH_DISPLAY_DURATION_MILLIS)
            sessionCheck.await()
        }
        onSplashFinished(authenticated)
    }

    SplashScreen()
}
