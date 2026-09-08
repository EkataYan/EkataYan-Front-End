package com.ekatayan.app.ui.profile

import com.ekatayan.app.viewmodel.ProfileViewModel

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProfileRoute(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    ProfileScreen(
        uiState = viewModel.uiState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onSettingsClick = onSettingsClick,
        onNotificationClick = onNotificationClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}
