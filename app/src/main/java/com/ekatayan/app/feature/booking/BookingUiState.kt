package com.ekatayan.app.feature.booking

data class BookingUiState(
    val searchQuery: String = "",
    val selectedDestination: String? = null,
    val selectedCategory: BookingCategory = BookingCategory.ALL,
    val availableDestinations: List<String> = emptyList(),
    val popularPlaces: List<BookingPlace> = emptyList(),
    val recommendedPlaces: List<BookingPlace> = emptyList(),
) {
    val hasResults: Boolean
        get() = popularPlaces.isNotEmpty() || recommendedPlaces.isNotEmpty()
}
