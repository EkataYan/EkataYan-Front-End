package com.ekatayan.app.data.repository

import android.util.Base64
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.Color
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.model.NotificationCategory
import com.ekatayan.app.data.model.TripInvitation
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.remote.api.NotificationDto
import com.ekatayan.app.data.remote.realtime.NotificationRealtimeClient
import com.ekatayan.app.data.remote.realtime.NotificationRealtimeEvent
import com.google.gson.JsonParser
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface NotificationsRepository {
    val notifications: StateFlow<List<NotificationItem>>
    fun markAsRead(notificationId: String)
    val invitations: StateFlow<List<TripInvitation>>
    suspend fun refreshNotifications()
    suspend fun refreshInvitations()
    suspend fun acceptInvitation(id:String)
    suspend fun declineInvitation(id:String)
    fun onAppForeground()
}

internal fun mergeNotificationItems(
    current: List<NotificationItem>,
    incoming: List<NotificationItem>,
): List<NotificationItem> = (current + incoming)
    .associateBy(NotificationItem::id)
    .values
    .sortedByDescending { runCatching { Instant.parse(it.createdAt) }.getOrDefault(Instant.MIN) }

@Singleton
class DefaultNotificationsRepository @Inject constructor(
    private val api: EkataYanApiService,
    private val tripsRepository: TripsRepository,
    private val session: UserSessionProvider,
    private val realtime: NotificationRealtimeClient,
) : NotificationsRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshMutex = Mutex()
    @Volatile private var activeUserId: String? = null
    @Volatile private var lastRefreshAt = 0L

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    private val mutableInvitations=MutableStateFlow<List<TripInvitation>>(emptyList())
    override val invitations=mutableInvitations.asStateFlow()

    init {
        scope.launch {
            realtime.events.collect { event ->
                when (event) {
                    is NotificationRealtimeEvent.Insert -> handleRealtimeInsert(event.notification)
                    NotificationRealtimeEvent.Connected -> refreshSafely(force = false)
                    NotificationRealtimeEvent.Disconnected -> Unit
                }
            }
        }
        scope.launch {
            session.accessToken.collectLatest { token ->
                val userId = token?.subjectUuid()
                activeUserId = userId
                if (token == null || userId == null) {
                    realtime.unsubscribe()
                    _notifications.value = emptyList()
                    mutableInvitations.value = emptyList()
                    lastRefreshAt = 0L
                } else {
                    realtime.subscribe(token, userId)
                    refreshSafely(force = true)
                }
            }
        }
    }

    override fun markAsRead(notificationId: String) {
        _notifications.update { current ->
            current.map { notification ->
                if (notification.id == notificationId) notification.copy(isUnread = false) else notification
            }
        }
        scope.launch {
            runCatching { api.markNotificationRead(notificationId) }
                .onFailure { logger.warning("Notification read state could not be synchronized") }
        }
    }

    override suspend fun refreshNotifications() {
        val incoming = api.notifications().data.orEmpty().map { it.toDomain() }
        _notifications.update { current -> mergeNotificationItems(current, incoming) }
    }

    override suspend fun refreshInvitations() {
        mutableInvitations.value = api.myTripInvites().data.orEmpty().map {
            TripInvitation(it.inviteId, it.tripId, it.tripName, it.tripStartDate, it.tripEndDate,
                it.inviterDisplayName, it.inviterUsername, it.inviterAvatarUrl, it.createdAt)
        }
    }

    override suspend fun acceptInvitation(id: String) {
        api.acceptTripInvite(id)
        markInvitationNotificationRead(id)
        refreshInvitations()
        tripsRepository.refreshTrips()
    }

    override suspend fun declineInvitation(id: String) {
        api.declineTripInvite(id)
        markInvitationNotificationRead(id)
        refreshInvitations()
    }

    override fun onAppForeground() {
        val token = session.currentAccessToken()
        val userId = token?.subjectUuid()
        if (token == null || userId == null) {
            realtime.unsubscribe()
            return
        }
        if (activeUserId != userId) {
            activeUserId = userId
            realtime.subscribe(token, userId)
        } else {
            realtime.ensureConnected()
        }
        scope.launch { refreshSafely(force = false) }
    }

    private suspend fun handleRealtimeInsert(dto: NotificationDto) {
        if (dto.userId != activeUserId) return
        val item = dto.toDomain()
        _notifications.update { current -> mergeNotificationItems(current, listOf(item)) }
        if (dto.type == "trip_invite") {
            runCatching { refreshInvitations() }
                .onFailure { logger.warning("Realtime invitation details could not be refreshed") }
        }
    }

    private suspend fun refreshSafely(force: Boolean) {
        if (activeUserId == null) return
        val now = System.currentTimeMillis()
        if (!force && now - lastRefreshAt < REFRESH_THROTTLE_MILLIS) return
        refreshMutex.withLock {
            val lockedNow = System.currentTimeMillis()
            if (!force && lockedNow - lastRefreshAt < REFRESH_THROTTLE_MILLIS) return
            runCatching {
                refreshNotifications()
                refreshInvitations()
            }.onSuccess {
                lastRefreshAt = System.currentTimeMillis()
            }.onFailure {
                logger.warning("Notification synchronization failed")
            }
        }
    }

    private fun markInvitationNotificationRead(inviteId: String) {
        _notifications.value
            .filter { it.relatedInviteId == inviteId && it.isUnread }
            .forEach { markAsRead(it.id) }
    }

    private fun NotificationDto.toDomain(): NotificationItem {
        val category = when {
            type.contains("expense", ignoreCase = true) -> NotificationCategory.EXPENSES
            type.contains("trip", ignoreCase = true) || type.contains("itinerary", ignoreCase = true) -> NotificationCategory.TRIPS
            else -> NotificationCategory.UPDATES
        }
        val icon = when (category) {
            NotificationCategory.TRIPS -> Icons.Default.Groups
            NotificationCategory.EXPENSES -> Icons.AutoMirrored.Filled.ReceiptLong
            NotificationCategory.UPDATES -> Icons.Default.Notifications
        }
        val tint = when (category) {
            NotificationCategory.TRIPS -> Color(0xFF1769AA)
            NotificationCategory.EXPENSES -> Color(0xFF178B57)
            NotificationCategory.UPDATES -> Color(0xFFB46816)
        }
        return NotificationItem(
            id = id,
            tripId = tripId,
            relatedInviteId = payload?.get("invite_id")?.takeUnless { it.isJsonNull }?.asString,
            type = type,
            title = title,
            message = body,
            timeLabel = createdAt.relativeTimeLabel(),
            createdAt = createdAt,
            category = category,
            icon = icon,
            iconTint = tint,
            iconBackground = tint.copy(alpha = 0.12f),
            isUnread = readAt == null,
        )
    }

    private fun String.relativeTimeLabel(): String {
        val then = toInstantOrMinimum()
        if (then == Instant.MIN) return "Recently"
        val duration = Duration.between(then, Instant.now()).coerceAtLeast(Duration.ZERO)
        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toHours() < 1 -> "${duration.toMinutes()} min ago"
            duration.toDays() < 1 -> "${duration.toHours()} hr ago"
            duration.toDays() == 1L -> "Yesterday"
            else -> "${duration.toDays()} days ago"
        }
    }

    private fun String.toInstantOrMinimum() = runCatching { Instant.parse(this) }.getOrDefault(Instant.MIN)

    private fun String.subjectUuid(): String? = runCatching {
        val encoded = split('.')[1]
        val decoded = String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        JsonParser.parseString(decoded).asJsonObject.get("sub").asString.also(UUID::fromString)
    }.getOrNull()

    private companion object {
        const val REFRESH_THROTTLE_MILLIS = 5_000L
        val logger: Logger = Logger.getLogger("EkataYanNotifications")
    }
}
