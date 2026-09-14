package com.ekatayan.app.feature.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.graphics.Color
import com.ekatayan.app.data.model.NotificationCategory
import com.ekatayan.app.data.repository.NotificationsRepository
import com.ekatayan.app.data.repository.mergeNotificationItems
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.model.TripInvitation
import com.ekatayan.app.viewmodel.NotificationsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Before
import org.junit.After
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import com.ekatayan.app.data.model.NotificationFilter
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private class FakeNotificationsRepository(initial: List<NotificationItem>) : NotificationsRepository {
    private val items = MutableStateFlow(initial)
    override val notifications: StateFlow<List<NotificationItem>> = items
    override val invitations = MutableStateFlow<List<TripInvitation>>(emptyList())
    override fun markAsRead(notificationId: String) {
        items.value = items.value.map { if (it.id == notificationId) it.copy(isUnread = false) else it }
    }
    override suspend fun refreshNotifications() = Unit
    override suspend fun refreshInvitations() = Unit
    override suspend fun acceptInvitation(id: String) = Unit
    override suspend fun declineInvitation(id: String) = Unit
    override fun onAppForeground() = Unit
    fun insert(notification: NotificationItem) {
        items.value = mergeNotificationItems(items.value, listOf(notification))
    }
}



@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {
    private val store = ViewModelStore()
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }


    private fun createViewModel(): NotificationsViewModel {
        val repository = FakeNotificationsRepository(notificationFixtures())
        return NotificationsViewModel(repository).also { store.put("notifications", it) }
    }

    private fun notificationFixtures() = listOf(
        notification("1", NotificationCategory.TRIPS),
        notification("2", NotificationCategory.EXPENSES),
        notification("3", NotificationCategory.UPDATES),
    )

    private fun notification(id: String, category: NotificationCategory) = NotificationItem(
        id = id,
        tripId = null,
        relatedInviteId = null,
        type = category.name.lowercase(),
        title = "Title $id",
        message = "Message $id",
        timeLabel = "Just now",
        createdAt = "2026-09-14T00:00:0${id}Z",
        category = category,
        icon = Icons.Default.Groups,
        iconTint = Color.Blue,
        iconBackground = Color.LightGray,
        isUnread = true,
    )

    @Test
    fun openingNotificationMarksOnlyThatNotificationAsRead() {
        val viewModel = createViewModel()

        viewModel.markAsRead("1")

        assertFalse(viewModel.uiState.value.notifications.single { it.id == "1" }.isUnread)
        assertTrue(viewModel.uiState.value.notifications.single { it.id == "2" }.isUnread)
        assertEquals(2, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun readingEveryUnreadNotificationClearsGlobalIndicator() {
        val viewModel = createViewModel()
        viewModel.uiState.value.notifications.forEach { viewModel.markAsRead(it.id) }

        assertEquals(0, viewModel.uiState.value.unreadCount)
        assertFalse(viewModel.uiState.value.hasUnreadNotifications)
    }
    @Test
    fun repositoryUpdatesReachEveryViewModelWithoutResettingFilter() {
        val repository = FakeNotificationsRepository(notificationFixtures())
        val first = NotificationsViewModel(repository).also { store.put("first", it) }
        val second = NotificationsViewModel(repository).also { store.put("second", it) }
        second.onFilterSelected(NotificationFilter.TRIPS)
        first.markAsRead("1")
        assertFalse(second.uiState.value.notifications.single { it.id == "1" }.isUnread)
        assertEquals(NotificationFilter.TRIPS, second.uiState.value.selectedFilter)
        repository.markAsRead("2")
        assertFalse(first.uiState.value.notifications.single { it.id == "2" }.isUnread)
        repository.markAsRead("missing")
        assertEquals(first.uiState.value.notifications, second.uiState.value.notifications)
    }

    @Test
    fun realtimeInsertUpdatesSharedUnreadStateImmediately() {
        val repository = FakeNotificationsRepository(emptyList())
        val viewModel = NotificationsViewModel(repository).also { store.put("realtime", it) }

        repository.insert(notification("4", NotificationCategory.TRIPS))

        assertEquals(listOf("4"), viewModel.uiState.value.notifications.map { it.id })
        assertEquals(1, viewModel.uiState.value.unreadCount)
        assertTrue(viewModel.uiState.value.hasUnreadNotifications)
    }

    @Test
    fun refreshAndRealtimeRowsAreDeduplicatedByDatabaseId() {
        val current = notification("5", NotificationCategory.TRIPS).copy(title = "Old", isUnread = true)
        val updated = current.copy(title = "Current", isUnread = false)

        val merged = mergeNotificationItems(listOf(current), listOf(updated))

        assertEquals(1, merged.size)
        assertEquals("Current", merged.single().title)
        assertFalse(merged.single().isUnread)
    }
}
