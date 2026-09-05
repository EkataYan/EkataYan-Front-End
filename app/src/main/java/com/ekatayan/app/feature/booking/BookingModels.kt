package com.ekatayan.app.feature.booking

import androidx.compose.ui.graphics.Color
import com.ekatayan.app.R

enum class BookingCategory(
    val chipLabel: String,
    val badgeLabel: String,
    val chipColor: Color,
) {
    ALL("All", "All", Color(0xFFB9DCF6)),
    HOTELS("Hotels", "Hotel", Color(0xFF86D6FF)),
    RESTAURANTS("Restaurants", "Restaurant", Color(0xFFF7B69B)),
    VACATION_RENTALS("Vacation Rentals", "Vacation Rental", Color(0xFFF1E38B)),
    TRANSPORTATION("Transportation", "Transportation", Color(0xFFF8A19A)),
    SAFARIS("Safaris", "Safaris", Color(0xFF98E693)),
    ACTIVITIES("Activities", "Activities", Color(0xFFD5E56B)),
    EVENTS("Events", "Events", Color(0xFFC69BFF)),
}

data class BookingPlace(
    val id: Int,
    val name: String,
    val location: String,
    val category: BookingCategory,
    val imageRes: Int,
    val isPopular: Boolean,
    val isRecommended: Boolean,
)

internal object BookingCatalog {
    val places = listOf(
        BookingPlace(
            id = 1,
            name = "Shangri-La",
            location = "Hambantota",
            category = BookingCategory.HOTELS,
            imageRes = R.drawable.shangri_la,
            isPopular = true,
            isRecommended = false,
        ),
        BookingPlace(
            id = 2,
            name = "Aqua Forte",
            location = "Galle",
            category = BookingCategory.RESTAURANTS,
            imageRes = R.drawable.aqua_forte,
            isPopular = true,
            isRecommended = false,
        ),
        BookingPlace(
            id = 3,
            name = "Ella Odyssey",
            location = "Ella",
            category = BookingCategory.TRANSPORTATION,
            imageRes = R.drawable.ella_odessy,
            isPopular = false,
            isRecommended = true,
        ),
        BookingPlace(
            id = 4,
            name = "Minneriya National Park",
            location = "Minneriya",
            category = BookingCategory.SAFARIS,
            imageRes = R.drawable.minneriya_national_park,
            isPopular = false,
            isRecommended = true,
        ),
        BookingPlace(
            id = 5,
            name = "Galle Face Hotel",
            location = "Colombo",
            category = BookingCategory.HOTELS,
            imageRes = R.drawable.galle_face_hotel,
            isPopular = false,
            isRecommended = true,
        ),
        BookingPlace(
            id = 6,
            name = "Kalpitiya Lagoon",
            location = "Kalpitiya",
            category = BookingCategory.ACTIVITIES,
            imageRes = R.drawable.kalpitiya_lagoons,
            isPopular = false,
            isRecommended = true,
        ),
        BookingPlace(
            id = 7,
            name = "Kandy Esala Perahera",
            location = "Kandy",
            category = BookingCategory.EVENTS,
            imageRes = R.drawable.kandy_esala_perahara,
            isPopular = false,
            isRecommended = true,
        ),
        BookingPlace(
            id = 8,
            name = "Royal Indigo Villa",
            location = "Bentota",
            category = BookingCategory.VACATION_RENTALS,
            imageRes = R.drawable.royal_indigo_villa,
            isPopular = false,
            isRecommended = true,
        ),
    )
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
