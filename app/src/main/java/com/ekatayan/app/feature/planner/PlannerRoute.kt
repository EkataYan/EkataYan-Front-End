package com.ekatayan.app.feature.planner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.feature.notifications.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlannerRoute(onCreateTrip: (PlannerUiState) -> Unit, onHomeClick: () -> Unit, onTripsClick: () -> Unit, onExpensesClick: () -> Unit, onProfileClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, notificationsUiState: StateFlow<NotificationsUiState>, viewModel: PlannerViewModel = hiltViewModel()) {
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    PlannerScreen(viewModel.uiState.value, viewModel::updateDestination, viewModel::updateBudget, viewModel::updateStartDate, viewModel::updateEndDate, viewModel::clearStartDate, viewModel::clearEndDate, viewModel::setError, viewModel::updatePreference, { if (viewModel.validate()) onCreateTrip(viewModel.uiState.value) }, onHomeClick, onTripsClick, onExpensesClick, onProfileClick, onNotificationClick, onSettingsClick, notificationState.hasUnreadNotifications)
}
