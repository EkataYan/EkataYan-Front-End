package com.ekatayan.app.ui.trips

import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.viewmodel.TripsViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TripsRoute(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddTripClick: () -> Unit,
    onTripClick: (Trip) -> Unit = {},
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    TripsScreen(
        uiState = uiState,
        onPreviousMonthClick = viewModel::showPreviousMonth,
        onNextMonthClick = viewModel::showNextMonth,
        onDateClick = viewModel::selectDate,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onAddTripClick = onAddTripClick,
        onNotificationClick = onNotificationClick,
        onSettingsClick = onSettingsClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
        onDeleteTrip = viewModel::deleteTrip,
        onTripClick = { trip ->
            viewModel.selectDate(trip.startDate)
            onTripClick(trip)
        },
    )
}
