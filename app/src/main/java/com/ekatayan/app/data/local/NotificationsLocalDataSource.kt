package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.NotificationItem
import javax.inject.Inject
import javax.inject.Singleton

/** Empty local source; real notifications and invitations are account events. */
@Singleton
class NotificationsLocalDataSource @Inject constructor() {
    fun getInitialNotifications(): List<NotificationItem> = emptyList()
}
