package com.ekatayan.app.ui.expenses

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val EXPENSES_ROUTE = "expenses"

fun NavGraphBuilder.expensesScreen(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
) {
    composable(EXPENSES_ROUTE) { ExpensesRoute(onHomeClick, onTripsClick, onPlannerClick, onProfileClick, onSettingsClick, onNotificationClick, notificationsUiState) }
}
