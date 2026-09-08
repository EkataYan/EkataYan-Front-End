package com.ekatayan.app.ui.notifications

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.viewmodel.NotificationsViewModel

const val NOTIFICATIONS_ROUTE = "notifications"

fun NavGraphBuilder.notificationsScreen(
    viewModel: NotificationsViewModel,
    selectedBottomNavItem: () -> AppBottomNavItem,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: (Int) -> Unit,
) {
    composable(NOTIFICATIONS_ROUTE) {
        NotificationsRoute(
            viewModel = viewModel,
            selectedBottomNavItem = selectedBottomNavItem(),
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick,
        )
    }
}
