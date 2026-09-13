package com.ekatayan.app.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ekatayan.app.app.navigation.EkataYanNavHost

@Composable
fun EkataYanApp() {
    val navController = rememberNavController()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Keep the shared app bounds stable when the IME opens. safeDrawingPadding()
            // also includes the IME and used to resize every destination (including the
            // authentication backdrop). Individual input screens own their IME padding.
            .systemBarsPadding()
            .displayCutoutPadding(),
    ) {
        EkataYanNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
