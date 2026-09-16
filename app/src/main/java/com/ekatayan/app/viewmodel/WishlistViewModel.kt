package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.WishlistCover
import com.ekatayan.app.data.model.WishlistData
import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem

import com.ekatayan.app.data.repository.WishlistRepository
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.ekatayan.app.utils.runSuspendCatching

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val repository: WishlistRepository,
    private val strings: StringResourceProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(repository.state.value.toUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.state.collect { data ->
                _uiState.update { it.copy(groups = data.groups, availableDestinations = data.availableDestinations) }
            }
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runSuspendCatching { repository.refresh() }
            .onSuccess { _uiState.update { it.copy(isLoading = false) } }
            .onFailure { _ -> _uiState.update { it.copy(isLoading = false, errorMessage = strings[R.string.wishlist_error_load]) } }
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
        launchMutation { repository.create(trimmedName, initialItem) }
        return true
    }

    fun renameGroup(groupId: Int, newName: String): Boolean {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) return false
        if (repository.state.value.groups.none { it.id == groupId }) return false
        launchMutation { repository.rename(groupId, trimmedName) }
        return true
    }

    fun deleteGroup(groupId: Int) {
        launchMutation { repository.delete(groupId) }
    }

    fun updateGroupCoverFromDevice(groupId: Int, imageUri: String) {
        _uiState.update { it.copy(errorMessage = strings[R.string.wishlist_device_cover_unsupported]) }
    }

    fun updateGroupCoverFromPlace(groupId: Int, placeId: Int): Boolean {
        val group = repository.state.value.groups.find { it.id == groupId } ?: return false
        if (group.items.none { it.id == placeId }) return false
        launchMutation { repository.setPlaceCover(groupId, placeId) }
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
        launchMutation { repository.addPlace(groupId, item) }
        return true
    }

    fun removePlaceFromGroup(groupId: Int, itemId: Int) {
        launchMutation { repository.removePlace(groupId, itemId) }
    }

    private fun launchMutation(request: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runSuspendCatching { request() }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { _ -> _uiState.update { it.copy(isLoading = false, errorMessage = strings[R.string.wishlist_error_update]) } }
        }
    }
}

