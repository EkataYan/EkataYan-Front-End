package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModelStore
import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.model.BusinessDocument
import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.MessageType
import com.ekatayan.app.data.local.database.groupHubData
import com.ekatayan.app.data.local.database.toSnapshot
import com.ekatayan.app.data.repository.DocumentRepository
import com.ekatayan.app.data.repository.GroupHubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupHubSharePlaceTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun sharePlaceAppendsToGroupsAndCreatesDirectChatInSameState() {
        val repository = GroupHubRepository()
        val documents = object : DocumentRepository {
            override suspend fun persistAndReadName(value: String): String? = null
            override suspend fun readBusinessDocument(value: String): BusinessDocument? = null
        }
        val viewModel = GroupHubViewModel(repository, documents).also { store.put("group-hub-share", it) }
        assertTrue(viewModel.createGroup("Travel Crew", "Weekend plans", emptySet(), null))
        val groupId = viewModel.uiState.value.groups.first().id
        val destination = WishlistDestinationCatalog.destinations.first { it.id == 14 }

        assertTrue(viewModel.sharePlace(setOf(groupId), setOf("nethmi"), destination))

        val state = repository.state.value
        val groupMessage = state.messagesByGroup[groupId].orEmpty().single()
        assertEquals(MessageType.SharedPlace, groupMessage.type)
        assertEquals(destination.id, groupMessage.placeId)
        val directGroup = state.groups.find { it.id == "direct-nethmi" }
        assertNotNull(directGroup)
        assertEquals(destination.id, state.messagesByGroup[directGroup?.id].orEmpty().single().placeId)
        assertTrue(state.users.any { it.id == CURRENT_USER_ID })
        assertTrue(state.users.any { it.id == "nethmi" })
    }

    @Test
    fun sharedPlaceMessageSurvivesGroupHubSnapshotMapping() {
        val repository = GroupHubRepository()
        val documents = object : DocumentRepository {
            override suspend fun persistAndReadName(value: String): String? = null
            override suspend fun readBusinessDocument(value: String): BusinessDocument? = null
        }
        val viewModel = GroupHubViewModel(repository, documents).also { store.put("group-hub-persistence", it) }
        assertTrue(viewModel.createGroup("History Group", "", emptySet(), null))
        val groupId = viewModel.uiState.value.groups.first().id
        val destination = WishlistDestinationCatalog.destinations.first { it.id == 14 }
        assertTrue(viewModel.sharePlace(setOf(groupId), emptySet(), destination))

        val snapshot = repository.state.value.toSnapshot()
        val restored = groupHubData(snapshot.users, snapshot.groups, snapshot.members, snapshot.messages, snapshot.reactions)

        assertEquals(MessageType.SharedPlace, restored.messagesByGroup[groupId].orEmpty().single().type)
        assertEquals(destination.id, restored.messagesByGroup[groupId].orEmpty().single().placeId)
    }
}
