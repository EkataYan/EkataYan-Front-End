package com.ekatayan.app.ui.booking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val BOOKING_ROUTE = "booking"

fun NavGraphBuilder.bookingScreen(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
) {
    composable(BOOKING_ROUTE) {
        BookingRoute(
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick,
            onSettingsClick = onSettingsClick,
            notificationsUiState = notificationsUiState,
        )
    }
}
