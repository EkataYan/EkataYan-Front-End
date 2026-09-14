package com.ekatayan.app.ui.notifications

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsViewModel

const val NOTIFICATION_DETAIL_ROUTE = "notifications/{notificationId}"
private const val NOTIFICATION_ID_ARGUMENT = "notificationId"

fun notificationDetailRoute(notificationId: String) = "notifications/$notificationId"

fun NavGraphBuilder.notificationDetailScreen(
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit,
) {
    composable(
        route = NOTIFICATION_DETAIL_ROUTE,
    ) { entry ->
        NotificationDetailRoute(
            notificationId = requireNotNull(entry.arguments?.getString(NOTIFICATION_ID_ARGUMENT)),
            viewModel = viewModel,
            onBackClick = onBackClick,
        )
    }
}
