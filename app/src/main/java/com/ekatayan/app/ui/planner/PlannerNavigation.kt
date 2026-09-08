package com.ekatayan.app.ui.planner

import com.ekatayan.app.viewmodel.PlannerUiState

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val PLANNER_ROUTE = "planner"

fun NavGraphBuilder.plannerScreen(onCreateTrip: (PlannerUiState) -> Unit, onHomeClick: () -> Unit, onTripsClick: () -> Unit, onExpensesClick: () -> Unit, onProfileClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, notificationsUiState: StateFlow<NotificationsUiState>) {
    composable(PLANNER_ROUTE) { PlannerRoute(onCreateTrip, onHomeClick, onTripsClick, onExpensesClick, onProfileClick, onNotificationClick, onSettingsClick, notificationsUiState) }
}
