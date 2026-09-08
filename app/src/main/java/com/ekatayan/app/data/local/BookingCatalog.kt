package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.BookingCategory
import com.ekatayan.app.data.model.BookingPlace

import com.ekatayan.app.R

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

