package com.ekatayan.app.ui.settings

import com.ekatayan.app.viewmodel.SettingsViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun SettingsRoute(
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAccountClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onPermissionsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onStorageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLegalClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context=LocalContext.current
    val notifications=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){viewModel.setPushNotificationsEnabled(it)}
    val setNotifications:(Boolean)->Unit={enabled->
        if(enabled && Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        else viewModel.setPushNotificationsEnabled(enabled)
    }

    SettingsScreen(
        uiState = uiState,
        onPushNotificationsChanged = setNotifications,
        onAccountClick=onAccountClick,onPasswordClick=onPasswordClick,onAppearanceClick=onAppearanceClick,onLanguageClick=onLanguageClick,onPermissionsClick=onPermissionsClick,onPrivacyClick=onPrivacyClick,onStorageClick=onStorageClick,onHelpClick=onHelpClick,onAboutClick=onAboutClick,onLegalClick=onLegalClick,
        onLogoutClick = {
            viewModel.logout()
            onLogoutClick()
        },
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
    )
}
