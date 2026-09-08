package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.NotificationsLocalDataSource
import com.ekatayan.app.data.model.NotificationItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface NotificationsRepository {
    val notifications: StateFlow<List<NotificationItem>>
    fun markAsRead(notificationId: Int)
}

@Singleton
class DefaultNotificationsRepository @Inject constructor(
    localDataSource: NotificationsLocalDataSource,
) : NotificationsRepository {

    private val _notifications = MutableStateFlow(localDataSource.getInitialNotifications())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    override fun markAsRead(notificationId: Int) {
        _notifications.update { current ->
            current.map { notification ->
                if (notification.id == notificationId) notification.copy(isUnread = false) else notification
            }
        }
    }
}
