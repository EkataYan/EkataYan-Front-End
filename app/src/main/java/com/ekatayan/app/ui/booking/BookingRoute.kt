package com.ekatayan.app.ui.booking

import com.ekatayan.app.viewmodel.BookingViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BookingRoute(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
    viewModel: BookingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    BookingScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDestinationSelected = viewModel::onDestinationSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onClearDestination = viewModel::clearDestination,
        onClearSearch = viewModel::clearSearch,
        onResetFilters = viewModel::resetFilters,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onNotificationClick = onNotificationClick,
        onSettingsClick = onSettingsClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}
