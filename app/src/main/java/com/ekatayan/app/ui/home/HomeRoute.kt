package com.ekatayan.app.ui.home

import com.ekatayan.app.viewmodel.HomeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeRoute(
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
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
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}
