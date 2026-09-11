package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.WishlistCover
import com.ekatayan.app.data.model.WishlistData
import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem

import com.ekatayan.app.data.repository.WishlistRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class WishlistViewModel @Inject constructor(private val repository: WishlistRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(repository.state.value.toUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.state.collect { _uiState.value = it.toUiState() }
        }
    }

    private fun WishlistData.toUiState() = WishlistUiState(groups, availableDestinations)

    fun createGroup(name: String): Boolean {
        return createGroup(name, initialItem = null)
    }

    fun createGroupWithPlace(name: String, item: WishlistItem): Boolean {
        return createGroup(name, initialItem = item)
    }

    private fun createGroup(name: String, initialItem: WishlistItem?): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return false
        repository.update { state ->
            val nextId = (state.groups.maxOfOrNull(WishlistGroup::id) ?: 0) + 1
            state.copy(
                groups = state.groups + WishlistGroup(
                    id = nextId,
                    name = trimmedName,
                    items = listOfNotNull(initialItem).distinctBy(WishlistItem::id),
                ),
            )
        }
        return true
    }

    fun renameGroup(groupId: Int, newName: String): Boolean {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) return false
        repository.update { state ->
            state.copy(groups = state.groups.map { group ->
                if (group.id == groupId) group.copy(name = trimmedName) else group
            })
        }
        return true
    }

    fun deleteGroup(groupId: Int) {
        repository.update { it.copy(groups = it.groups.filterNot { group -> group.id == groupId }) }
    }

    fun updateGroupCoverFromDevice(groupId: Int, imageUri: String) {
        repository.update { state ->
            state.copy(groups = state.groups.map { group ->
                if (group.id == groupId) {
                    group.copy(cover = WishlistCover.FromDevice(imageUri))
                } else group
            })
        }
    }

    fun updateGroupCoverFromPlace(groupId: Int, placeId: Int): Boolean {
        val group = repository.state.value.groups.find { it.id == groupId } ?: return false
        if (group.items.none { it.id == placeId }) return false
        repository.update { state ->
            state.copy(groups = state.groups.map { current ->
                if (current.id == groupId) current.copy(cover = WishlistCover.FromPlace(placeId)) else current
            })
        }
        return true
    }

    fun searchAvailableDestinations(groupId: Int, query: String): List<WishlistItem> {
        val savedIds = repository.state.value.groups
            .find { it.id == groupId }
            ?.items
            ?.mapTo(hashSetOf(), WishlistItem::id)
            .orEmpty()
        val normalizedQuery = query.trim()
        return repository.state.value.availableDestinations.filter { destination ->
            destination.id !in savedIds && (
                normalizedQuery.isEmpty() ||
                    destination.name.contains(normalizedQuery, ignoreCase = true) ||
                    destination.location?.contains(normalizedQuery, ignoreCase = true) == true ||
                    destination.description.contains(normalizedQuery, ignoreCase = true)
                )
        }
    }

    fun hasDestinationMatch(query: String): Boolean {
        val normalizedQuery = query.trim()
        return normalizedQuery.isEmpty() || repository.state.value.availableDestinations.any { destination ->
            destination.name.contains(normalizedQuery, ignoreCase = true) ||
                destination.location?.contains(normalizedQuery, ignoreCase = true) == true ||
                destination.description.contains(normalizedQuery, ignoreCase = true)
        }
    }

    fun addPlaceToGroup(groupId: Int, item: WishlistItem): Boolean {
        val group = repository.state.value.groups.find { it.id == groupId } ?: return false
        if (group.items.any { it.id == item.id }) return false
        repository.update { state ->
            state.copy(groups = state.groups.map { current ->
                if (current.id == groupId) current.copy(items = current.items + item) else current
            })
        }
        return true
    }

    fun removePlaceFromGroup(groupId: Int, itemId: Int) {
        repository.update { state ->
            state.copy(groups = state.groups.map { group ->
                if (group.id == groupId) {
                    group.copy(
                        items = group.items.filterNot { it.id == itemId },
                        cover = if ((group.cover as? WishlistCover.FromPlace)?.placeId == itemId) WishlistCover.None else group.cover,
                    )
                } else group
            })
        }
    }
}

