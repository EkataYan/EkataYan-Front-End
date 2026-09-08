package com.ekatayan.app.feature.businesspartner

import com.ekatayan.app.R
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory demo store. No account, submission, file or booking leaves this device. */
class LocalBusinessPartnerRepository @Inject constructor() {
    private val mutableState = MutableStateFlow(BusinessPartnerState())
    val state = mutableState.asStateFlow()

    fun update(transform: (BusinessPartnerState) -> BusinessPartnerState) = mutableState.update(transform)

    fun signOut() = update { state ->
        BusinessPartnerState(profile = state.profile, documents = state.documents,
            listings = state.listings, bookings = state.bookings, demoLoaded = state.demoLoaded)
    }

    fun deleteAccount() = update { BusinessPartnerState() }

    fun populateDemo() = update { state ->
        if (state.demoLoaded) state.copy(loggedIn = true) else state.copy(
            loggedIn = true,
            demoLoaded = true,
            listings = listOf(
                BusinessListing("room", "Deluxe Sea View Room", ListingCategory.ROOMS,
                    "Wake up to ocean views in a spacious room with a private balcony.", 25000.0, "night",
                    location = "Galle, Sri Lanka", imageRes = R.drawable.shangri_la, details = "2 guests · 1 king bed · Wi-Fi · Breakfast", views = 205, inquiries = 42),
                BusinessListing("safari", "Yala Safari Tour", ListingCategory.ACTIVITIES,
                    "Explore Yala with a local guide on a morning wildlife safari.", 18000.0, "person",
                    location = "Yala, Sri Lanka", imageRes = R.drawable.yala_safari, details = "4 hours · Guided tour · Transport included", views = 32, inquiries = 5),
                BusinessListing("rental", "Travel Camping Kit", ListingCategory.RENTALS,
                    "Everything you need for a comfortable outdoor adventure.", 12000.0, "day", ListingStatus.INACTIVE,
                    location = "Galle, Sri Lanka", imageRes = R.drawable.camping_rentals, details = "Tent · Sleeping bags · Cooking kit", views = 8, inquiries = 1),
            ),
            bookings = listOf(
                PartnerBooking("booking-1", "Amara Perera", "Deluxe Sea View Room", "12–14 Sep 2026", 50000.0, "Could we arrange an early check-in?", bookedOn = "2026-09-01", quantity = 2),
                PartnerBooking("booking-2", "Daniel Silva", "Yala Safari Tour", "18 Sep 2026", 36000.0, "Two travellers, morning departure please.", BookingStatus.CONFIRMED, bookedOn = "2026-09-02", quantity = 2),
                PartnerBooking("booking-3", "Nimali Fernando", "Travel Camping Kit", "1–3 Sep 2026", 36000.0, "Thank you for the equipment!", BookingStatus.COMPLETED, bookedOn = "2026-08-28", quantity = 1),
            ),
        )
    }
}
