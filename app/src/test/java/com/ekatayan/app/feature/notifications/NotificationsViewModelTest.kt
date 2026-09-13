package com.ekatayan.app.feature.notifications

import com.ekatayan.app.data.local.NotificationsLocalDataSource
import com.ekatayan.app.data.repository.NotificationsRepository
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
    override fun markAsRead(notificationId: Int) {
        items.value = items.value.map { if (it.id == notificationId) it.copy(isUnread = false) else it }
    }
    override suspend fun refreshInvitations() = Unit
    override suspend fun acceptInvitation(id: String) = Unit
    override suspend fun declineInvitation(id: String) = Unit
}



@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {
    private val store = ViewModelStore()
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }


    private fun createViewModel(): NotificationsViewModel {
        val dataSource = NotificationsLocalDataSource()
        val repository = FakeNotificationsRepository(dataSource.getInitialNotifications())
        return NotificationsViewModel(repository).also { store.put("notifications", it) }
    }

    @Test
    fun openingNotificationMarksOnlyThatNotificationAsRead() {
        val viewModel = createViewModel()

        viewModel.markAsRead(1)

        assertFalse(viewModel.uiState.value.notifications.single { it.id == 1 }.isUnread)
        assertTrue(viewModel.uiState.value.notifications.single { it.id == 2 }.isUnread)
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
        val repository = FakeNotificationsRepository(NotificationsLocalDataSource().getInitialNotifications())
        val first = NotificationsViewModel(repository).also { store.put("first", it) }
        val second = NotificationsViewModel(repository).also { store.put("second", it) }
        second.onFilterSelected(NotificationFilter.TRIPS)
        first.markAsRead(1)
        assertFalse(second.uiState.value.notifications.single { it.id == 1 }.isUnread)
        assertEquals(NotificationFilter.TRIPS, second.uiState.value.selectedFilter)
        repository.markAsRead(2)
        assertFalse(first.uiState.value.notifications.single { it.id == 2 }.isUnread)
        repository.markAsRead(-1)
        assertEquals(first.uiState.value.notifications, second.uiState.value.notifications)
    }
}
