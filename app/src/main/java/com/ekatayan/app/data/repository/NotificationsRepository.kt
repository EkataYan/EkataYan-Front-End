package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.NotificationsLocalDataSource
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.model.TripInvitation
import com.ekatayan.app.data.remote.api.EkataYanApiService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface NotificationsRepository {
    val notifications: StateFlow<List<NotificationItem>>
    fun markAsRead(notificationId: Int)
    val invitations: StateFlow<List<TripInvitation>>
    suspend fun refreshInvitations()
    suspend fun acceptInvitation(id:String)
    suspend fun declineInvitation(id:String)
}

@Singleton
class DefaultNotificationsRepository @Inject constructor(
    localDataSource: NotificationsLocalDataSource,
    private val api: EkataYanApiService,
    private val tripsRepository: TripsRepository,
) : NotificationsRepository {

    private val _notifications = MutableStateFlow(localDataSource.getInitialNotifications())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    private val mutableInvitations=MutableStateFlow<List<TripInvitation>>(emptyList())
    override val invitations=mutableInvitations.asStateFlow()

    override fun markAsRead(notificationId: Int) {
        _notifications.update { current ->
            current.map { notification ->
                if (notification.id == notificationId) notification.copy(isUnread = false) else notification
            }
        }
    }
    override suspend fun refreshInvitations() {
        mutableInvitations.value = api.myTripInvites().data.orEmpty().map {
            TripInvitation(it.inviteId, it.tripId, it.tripName, it.tripStartDate, it.tripEndDate,
                it.inviterDisplayName, it.inviterUsername, it.inviterAvatarUrl, it.createdAt)
        }
    }

    override suspend fun acceptInvitation(id: String) {
        api.acceptTripInvite(id)
        refreshInvitations()
        tripsRepository.refreshTrips()
    }

    override suspend fun declineInvitation(id: String) {
        api.declineTripInvite(id)
        refreshInvitations()
    }
}
