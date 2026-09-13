package com.ekatayan.app.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ekatayan.app.data.model.NotificationFilter
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.model.TripInvitation
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
    val invitations: List<TripInvitation> = emptyList(),
    val loadingInvitations:Boolean=false,
    val respondingInvites: Map<String, String> = emptyMap(),
    val invitationError:String?=null,
) {
    val unreadCount: Int
        get() = notifications.count(NotificationItem::isUnread)

    val hasUnreadNotifications: Boolean
        get() = unreadCount > 0 || invitations.isNotEmpty()

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
        refreshInvitations()
        viewModelScope.launch {
            repository.notifications.collect { notifications ->
                _uiState.update { it.copy(notifications = notifications) }
            }
        }
    }

    fun markAsRead(notificationId: Int) = repository.markAsRead(notificationId)
    fun refreshInvitations()=viewModelScope.launch { _uiState.update{it.copy(loadingInvitations=true,invitationError=null)}; runCatching{repository.refreshInvitations()}.onSuccess{_uiState.update{it.copy(loadingInvitations=false,invitations=repository.invitations.value)}}.onFailure{e->_uiState.update{it.copy(loadingInvitations=false,invitationError=e.message)}} }
    fun acceptInvitation(id: String) = respondToInvitation(id, "joining") { repository.acceptInvitation(id) }
    fun declineInvitation(id: String) = respondToInvitation(id, "declining") { repository.declineInvitation(id) }

    private fun respondToInvitation(id: String, action: String, request: suspend () -> Unit) = viewModelScope.launch {
        if (id in _uiState.value.respondingInvites) return@launch
        _uiState.update { it.copy(respondingInvites = it.respondingInvites + (id to action), invitationError = null) }
        runCatching { request() }
            .onSuccess {
                _uiState.update {
                    it.copy(invitations = repository.invitations.value, respondingInvites = it.respondingInvites - id)
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(respondingInvites = it.respondingInvites - id, invitationError = error.message)
                }
            }
    }
}
