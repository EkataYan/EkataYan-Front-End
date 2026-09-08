package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.BookingCategory
import com.ekatayan.app.data.model.BookingPlace

import com.ekatayan.app.data.repository.BookingRepository
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class BookingViewModel @Inject constructor(repository: BookingRepository) : ViewModel() {
    private val allPlaces = repository.getPlaces()
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

internal fun filterBookingPlaces(
    places: List<BookingPlace>,
    searchQuery: String,
    selectedDestination: String?,
    selectedCategory: BookingCategory,
): List<BookingPlace> {
    val normalizedQuery = searchQuery.trim().lowercase()
    return places.filter { place ->
        val matchesDestination = selectedDestination.isNullOrBlank() || place.location.equals(selectedDestination, ignoreCase = true)
        val matchesCategory = selectedCategory == BookingCategory.ALL || place.category == selectedCategory
        val matchesQuery = normalizedQuery.isBlank() || listOf(place.name, place.location, place.category.chipLabel, place.category.badgeLabel)
            .any { it.lowercase().contains(normalizedQuery) }
        matchesDestination && matchesCategory && matchesQuery
    }
}
