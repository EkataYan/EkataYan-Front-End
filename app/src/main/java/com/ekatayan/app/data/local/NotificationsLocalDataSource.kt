package com.ekatayan.app.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ekatayan.app.R
import com.ekatayan.app.data.model.NotificationCategory
import com.ekatayan.app.data.model.NotificationItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsLocalDataSource @Inject constructor() {

    fun getInitialNotifications(): List<NotificationItem> = listOf(
        notificationItem(1, R.string.notifications_itinerary_title, R.string.notifications_itinerary_message, R.string.notifications_just_now, NotificationCategory.UPDATES, true),
        notificationItem(2, R.string.notifications_expense_title, R.string.notifications_expense_message, R.string.notifications_25_min, NotificationCategory.EXPENSES, true),
        notificationItem(3, R.string.notifications_trip_title, R.string.notifications_trip_message, R.string.notifications_2_hours, NotificationCategory.TRIPS, true),
        notificationItem(4, R.string.notifications_weather_title, R.string.notifications_weather_message, R.string.notifications_yesterday, NotificationCategory.UPDATES, false),
        notificationItem(5, R.string.notifications_transport_title, R.string.notifications_transport_message, R.string.notifications_2_days, NotificationCategory.TRIPS, false),
    )

    private fun notificationItem(
        id: Int,
        titleRes: Int,
        messageRes: Int,
        timeLabelRes: Int,
        category: NotificationCategory,
        unread: Boolean,
    ): NotificationItem {
        val visual = notificationVisual(category, id)
        return NotificationItem(id, titleRes, messageRes, timeLabelRes, category, visual.first, visual.second, visual.third, unread)
    }

    private fun notificationVisual(category: NotificationCategory, id: Int): Triple<ImageVector, Color, Color> = when {
        id == 1 -> Triple(Icons.Default.AutoAwesome, Color(0xFF28A96B), Color(0xFFE1F5E9))
        id == 4 -> Triple(Icons.Default.Umbrella, Color(0xFFF39A35), Color(0xFFFFEEDC))
        id == 5 -> Triple(Icons.Default.DirectionsBus, Color(0xFF7557D9), Color(0xFFEDE7FB))
        category == NotificationCategory.EXPENSES -> Triple(Icons.Default.AccountBalanceWallet, Color(0xFF25A866), Color(0xFFE1F5E9))
        else -> Triple(Icons.Default.Groups, Color(0xFF3C7DE0), Color(0xFFE2EDFC))
    }
}
