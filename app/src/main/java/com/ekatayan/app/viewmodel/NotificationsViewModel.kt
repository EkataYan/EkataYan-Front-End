package com.ekatayan.app.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ekatayan.app.data.model.NotificationFilter
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.model.TripInvitation
import com.ekatayan.app.data.repository.NotificationsRepository
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.ekatayan.app.utils.runSuspendCatching

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
        get() {
            val notifiedInviteIds = notifications.mapNotNullTo(mutableSetOf(), NotificationItem::relatedInviteId)
            return notifications.count(NotificationItem::isUnread) +
                invitations.count { it.id !in notifiedInviteIds }
        }

    val hasUnreadNotifications: Boolean
        get() = unreadCount > 0 || invitations.isNotEmpty()

    val filteredNotifications: List<NotificationItem>
        get() {
            val representedInvites = invitations.mapTo(mutableSetOf(), TripInvitation::id)
            val visible = notifications.filterNot {
                it.type == "trip_invite" && it.relatedInviteId in representedInvites
            }
            return if (selectedFilter == NotificationFilter.ALL) {
                visible
            } else {
                visible.filter { it.category.name == selectedFilter.name }
            }
        }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
    private val strings: StringResourceProvider,
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
        viewModelScope.launch {
            repository.invitations.collect { invitations ->
                _uiState.update { it.copy(invitations = invitations, loadingInvitations = false) }
            }
        }
    }

    fun markAsRead(notificationId: String) = viewModelScope.launch {
        runSuspendCatching { repository.markAsRead(notificationId) }
            .onFailure { _ -> _uiState.update { it.copy(invitationError = strings[R.string.notifications_error_update]) } }
    }
    fun onAppForeground() = repository.onAppForeground()
    fun onNotificationsOpened() {
        repository.onAppForeground()
    }
    fun refreshInvitations()=viewModelScope.launch { _uiState.update{it.copy(loadingInvitations=true,invitationError=null)}; runSuspendCatching{repository.refreshInvitations()}.onSuccess{_uiState.update{it.copy(loadingInvitations=false,invitations=repository.invitations.value)}}.onFailure{_->_uiState.update{it.copy(loadingInvitations=false,invitationError=strings[R.string.notifications_error_invitations])}} }
    fun acceptInvitation(id: String) = respondToInvitation(id, "joining") { repository.acceptInvitation(id) }
    fun declineInvitation(id: String) = respondToInvitation(id, "declining") { repository.declineInvitation(id) }

    private fun respondToInvitation(id: String, action: String, request: suspend () -> Unit) = viewModelScope.launch {
        if (id in _uiState.value.respondingInvites) return@launch
        _uiState.update { it.copy(respondingInvites = it.respondingInvites + (id to action), invitationError = null) }
        runSuspendCatching { request() }
            .onSuccess {
                _uiState.update {
                    it.copy(invitations = repository.invitations.value, respondingInvites = it.respondingInvites - id)
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(respondingInvites = it.respondingInvites - id, invitationError = strings[R.string.notifications_error_invitation_response])
                }
            }
    }
}
