package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModelStore
import com.ekatayan.app.data.model.WishlistCover
import com.ekatayan.app.data.repository.WishlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistViewModelTest {
    private val store = ViewModelStore()
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    @Test
    fun removingCoverPlaceClearsCoverAndPlaceCanBeAddedAgain() {
        val repository = WishlistRepository()
        val viewModel = WishlistViewModel(repository).also { store.put("wishlist", it) }
        val place = viewModel.uiState.value.availableDestinations.first()
        assertTrue(viewModel.createGroupWithPlace("Test wishlist", place))
        val group = viewModel.uiState.value.groups.first()
        assertFalse(viewModel.addPlaceToGroup(group.id, place))
        assertTrue(viewModel.updateGroupCoverFromPlace(group.id, place.id))
        viewModel.removePlaceFromGroup(group.id, place.id)
        assertEquals(WishlistCover.None, repository.state.value.groups.first().cover)
        assertTrue(viewModel.searchAvailableDestinations(group.id, place.name).any { it.id == place.id })
        assertTrue(viewModel.addPlaceToGroup(group.id, place))
        assertEquals(1, viewModel.uiState.value.groups.first().items.count { it.id == place.id })
    }

    @Test
    fun repositoryChangesReachConsumersAndBlankNamesAreRejected() {
        val repository = WishlistRepository()
        val first = WishlistViewModel(repository).also { store.put("first", it) }
        val second = WishlistViewModel(repository).also { store.put("second", it) }
        val count = first.uiState.value.groups.size
        assertFalse(first.createGroup("  "))
        assertTrue(first.createGroup("  Weekend  "))
        assertEquals(count + 1, second.uiState.value.groups.size)
        assertEquals("Weekend", second.uiState.value.groups.last().name)
        val id = second.uiState.value.groups.last().id
        assertFalse(second.renameGroup(id, ""))
        second.deleteGroup(id)
        assertEquals(count, first.uiState.value.groups.size)
    }

    @Test
    fun createGroupFromHomeAddsPlaceOnceAndSynchronizesConsumers() {
        val repository = WishlistRepository()
        val homeConsumer = WishlistViewModel(repository).also { store.put("home", it) }
        val wishlistConsumer = WishlistViewModel(repository).also { store.put("wishlist", it) }
        val place = homeConsumer.uiState.value.availableDestinations.first { it.id == 14 }

        assertTrue(homeConsumer.createGroupWithPlace("Ancient Cities", place))

        val createdGroup = wishlistConsumer.uiState.value.groups.last()
        assertEquals("Ancient Cities", createdGroup.name)
        assertEquals(listOf(place.id), createdGroup.items.map { it.id })
        assertFalse(wishlistConsumer.addPlaceToGroup(createdGroup.id, place))
        assertEquals(1, createdGroup.items.count { it.id == place.id })

        wishlistConsumer.removePlaceFromGroup(createdGroup.id, place.id)
        assertTrue(homeConsumer.uiState.value.groups.last().items.none { it.id == place.id })
    }
}
