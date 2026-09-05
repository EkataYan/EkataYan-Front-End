package com.ekatayan.app.feature.booking

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class BookingViewModel @Inject constructor() : ViewModel() {
    private val allPlaces = BookingCatalog.places
    private val destinations = allPlaces.map { it.location }.distinct()
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        val state = _uiState.value
        _uiState.value = buildState(query, state.selectedDestination, state.selectedCategory)
    }

    fun onDestinationSelected(destination: String?) {
        val state = _uiState.value
        _uiState.value = buildState(state.searchQuery, destination?.takeUnless { it.isBlank() }, state.selectedCategory)
    }

    fun onCategorySelected(category: BookingCategory) {
        val state = _uiState.value
        _uiState.value = buildState(state.searchQuery, state.selectedDestination, category)
    }

    fun clearDestination() {
        onDestinationSelected(null)
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    fun resetFilters() {
        _uiState.value = buildState()
    }

    private fun buildState(
        searchQuery: String = "",
        selectedDestination: String? = null,
        selectedCategory: BookingCategory = BookingCategory.ALL,
    ): BookingUiState {
        val filteredPlaces = filterBookingPlaces(
            places = allPlaces,
            searchQuery = searchQuery,
            selectedDestination = selectedDestination,
            selectedCategory = selectedCategory,
        )
        return BookingUiState(
            searchQuery = searchQuery,
            selectedDestination = selectedDestination,
            selectedCategory = selectedCategory,
            availableDestinations = destinations,
            popularPlaces = filteredPlaces.filter { it.isPopular },
            recommendedPlaces = filteredPlaces.filter { it.isRecommended },
        )
    }
}
