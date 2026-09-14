package com.ekatayan.app.ui.home

import com.ekatayan.app.viewmodel.HomeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ekatayan.app.viewmodel.SettingsViewModel
import com.ekatayan.app.viewmodel.WeatherErrorKind

@Composable
fun HomeRoute(
    onDestinationClick: (Int) -> Unit,
    onWishlistClick: () -> Unit,
    onGroupHubClick: () -> Unit,
    onPartnershipClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onTripsClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBookingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var locationDisabledDialogDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.weatherErrorKind) {
        if (uiState.weatherErrorKind != WeatherErrorKind.LOCATION_DISABLED) {
            locationDisabledDialogDismissed = false
        }
    }
    fun hasLocationPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.onLocationPermissionChanged()
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onHomeResumed()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION ||
                    intent?.action == LocationManager.MODE_CHANGED_ACTION
                ) viewModel.onHomeResumed()
            }
        }
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }
    HomeScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchSubmit = viewModel::onSearchSubmit,
        onWishlistClick = onWishlistClick,
        onGroupHubClick = onGroupHubClick,
        onPartnershipClick = onPartnershipClick,
        onBookingClick = onBookingClick,
        onPlannerClick = onPlannerClick,
        onTripsClick = onTripsClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onNotificationClick = onNotificationClick,
        onRecommendedDestinationClick = onDestinationClick,
        onPopularDestinationClick = onDestinationClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
        onWeatherAction = {
            if (hasLocationPermission()) viewModel.refreshWeather()
            else locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        },
    )
    if (settingsState.loaded && !settingsState.locationPermissionPromptShown && !hasLocationPermission()) {
        AlertDialog(
            onDismissRequest = settingsViewModel::markLocationPermissionPromptShown,
            title = { Text("Use your location?") },
            text = { Text("EkataYan uses your location to show local weather and improve nearby travel recommendations.") },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.markLocationPermissionPromptShown()
                    locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                }) { Text("Allow location") }
            },
            dismissButton = { TextButton(onClick = settingsViewModel::markLocationPermissionPromptShown) { Text("Not now") } },
        )
    }
    if (uiState.weatherErrorKind == WeatherErrorKind.LOCATION_DISABLED && !locationDisabledDialogDismissed) {
        AlertDialog(
            onDismissRequest = { locationDisabledDialogDismissed = true },
            title = { Text("Please enable Location") },
            text = { Text("Turn on Android Location to show weather for your current area.") },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("Enable location") }
            },
            dismissButton = {
                TextButton(onClick = { locationDisabledDialogDismissed = true }) { Text("Not now") }
            },
        )
    }
}
