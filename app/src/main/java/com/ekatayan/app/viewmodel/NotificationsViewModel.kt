package com.ekatayan.app.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ekatayan.app.data.model.NotificationFilter
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

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
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NotificationsUiState(
            notifications = repository.notifications.value,
            selectedFilter = NotificationFilter.ALL,
        )
    )
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: NotificationFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    init {
        viewModelScope.launch {
            repository.notifications.collect { notifications ->
                _uiState.update { it.copy(notifications = notifications) }
            }
        }
    }

    fun markAsRead(notificationId: Int) = repository.markAsRead(notificationId)
}
