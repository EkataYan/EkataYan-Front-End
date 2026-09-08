package com.ekatayan.app.data.model

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

