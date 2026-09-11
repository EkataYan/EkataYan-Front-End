package com.ekatayan.app.ui.planner

import com.ekatayan.app.viewmodel.PlannerUiState
import com.ekatayan.app.viewmodel.PlannerViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlannerRoute(onCreateTrip: (PlannerUiState) -> Unit, onHomeClick: () -> Unit, onTripsClick: () -> Unit, onExpensesClick: () -> Unit, onProfileClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, notificationsUiState: StateFlow<NotificationsUiState>, viewModel: PlannerViewModel = hiltViewModel()) {
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState
    PlannerScreen(
        uiState = uiState,
        onDestinationChange = viewModel::updateDestination,
        onTravellerTypeSelected = viewModel::updateTravellerType,
        onPeopleCountChange = viewModel::updateCustomPeopleCount,
        onStartDateSelected = viewModel::updateStartDate,
        onEndDateSelected = viewModel::updateEndDate,
        onDateValidationError = viewModel::setError,
        onAskAiClick = { if (viewModel.validate()) onCreateTrip(viewModel.uiState.value) },
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onNotificationClick = onNotificationClick,
        onSettingsClick = onSettingsClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}
