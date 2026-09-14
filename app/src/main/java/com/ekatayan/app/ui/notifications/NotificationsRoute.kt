package com.ekatayan.app.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.viewmodel.NotificationsViewModel

@Composable
fun NotificationsRoute(
    selectedBottomNavItem: AppBottomNavItem,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
    viewModel: NotificationsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.onNotificationsOpened() }
    NotificationsScreen(
        uiState = uiState,
        selectedBottomNavItem = selectedBottomNavItem,
        onFilterSelected = viewModel::onFilterSelected,
        onNotificationClick = onNotificationClick,
        onAcceptInvite = viewModel::acceptInvitation,
        onDeclineInvite = viewModel::declineInvitation,
        onRetryInvites = viewModel::refreshInvitations,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
    )
}
