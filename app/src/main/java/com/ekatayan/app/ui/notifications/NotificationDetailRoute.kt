package com.ekatayan.app.ui.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsViewModel

@Composable
fun NotificationDetailRoute(
    notificationId: String,
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notification = uiState.notifications.find { it.id == notificationId }
    LaunchedEffect(notificationId, notification?.isUnread) {
        if (notification?.isUnread == true) viewModel.markAsRead(notificationId)
    }

    NotificationDetailScreen(
        notification = notification,
        onBackClick = onBackClick,
    )
}
