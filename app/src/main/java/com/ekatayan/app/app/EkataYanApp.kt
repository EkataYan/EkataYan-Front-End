package com.ekatayan.app.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
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
            // This is the only owner of status-bar, cutout, and navigation-bar
            // geometry. Destination selection and persisted state cannot bypass it.
            .safeDrawingPadding(),
    ) {
        EkataYanNavHost(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
