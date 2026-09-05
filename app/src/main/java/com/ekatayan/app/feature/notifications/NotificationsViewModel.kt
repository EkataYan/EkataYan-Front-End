package com.ekatayan.app.feature.notifications

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.ekatayan.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class NotificationCategory { TRIPS, EXPENSES, UPDATES }

enum class NotificationFilter { ALL, TRIPS, EXPENSES, UPDATES }

@Immutable
data class NotificationItem(
    val id: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
    @param:StringRes val timeLabelRes: Int,
    val category: NotificationCategory,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val isUnread: Boolean,
)

@Immutable
data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
) {
    val unreadCount: Int
        get() = notifications.count(NotificationItem::isUnread)

    val hasUnreadNotifications: Boolean
        get() = unreadCount > 0

    val filteredNotifications: List<NotificationItem>
        get() = if (selectedFilter == NotificationFilter.ALL) {
            notifications
        } else {
            notifications.filter { it.category.name == selectedFilter.name }
        }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(notifications = mockNotifications()))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: NotificationFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun markAsRead(notificationId: Int) {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { notification ->
                    if (notification.id == notificationId) notification.copy(isUnread = false) else notification
                },
            )
        }
    }

}

private fun mockNotifications() = listOf(
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