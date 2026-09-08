package com.ekatayan.app.ui.notifications

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ekatayan.app.viewmodel.NotificationsViewModel

const val NOTIFICATION_DETAIL_ROUTE = "notifications/{notificationId}"
private const val NOTIFICATION_ID_ARGUMENT = "notificationId"

fun notificationDetailRoute(notificationId: Int) = "notifications/$notificationId"

fun NavGraphBuilder.notificationDetailScreen(
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit,
) {
    composable(
        route = NOTIFICATION_DETAIL_ROUTE,
        arguments = listOf(navArgument(NOTIFICATION_ID_ARGUMENT) { type = NavType.IntType }),
    ) { entry ->
        NotificationDetailRoute(
            notificationId = requireNotNull(entry.arguments?.getInt(NOTIFICATION_ID_ARGUMENT)),
            viewModel = viewModel,
            onBackClick = onBackClick,
        )
    }
}
