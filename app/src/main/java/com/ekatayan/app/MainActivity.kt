package com.ekatayan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ekatayan.app.app.EkataYanApp
import com.ekatayan.app.core.designsystem.theme.EkataYanTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.ekatayan.app.data.repository.SettingsRepository
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import com.ekatayan.app.ui.settings.applyAppLanguage

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT,
            ),
        )
        // Keep edge-to-edge deterministic after the platform splash swaps to the
        // post-splash theme. Safe-area geometry is owned by EkataYanApp.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val preferences by settingsRepository.preferences.collectAsStateWithLifecycle()
            LaunchedEffect(preferences.selectedLanguage) {
                val current = resources.configuration.locales[0]?.language
                if (current != preferences.selectedLanguage) applyAppLanguage(this@MainActivity, preferences.selectedLanguage)
            }
            val dark = when (preferences.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            EkataYanTheme(darkTheme = dark) {
                EkataYanApp()
            }
        }
    }
}
