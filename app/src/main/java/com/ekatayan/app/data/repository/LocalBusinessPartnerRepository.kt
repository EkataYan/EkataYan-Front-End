package com.ekatayan.app.data.repository

import com.ekatayan.app.R
import com.ekatayan.app.data.local.database.*
import com.ekatayan.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Room-backed frontend demo store. No account, file, listing, or booking leaves this device. */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class LocalBusinessPartnerRepository private constructor(private val dao: BusinessPartnerDao?, testMode: Boolean) {
    @Inject constructor(dao: BusinessPartnerDao) : this(dao, false)
    constructor() : this(null, true)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val mutableState = MutableStateFlow(BusinessPartnerState())
    val state = mutableState.asStateFlow()

    init {
        if (!testMode && dao != null) scope.launch {
            val primary = combine(dao.observeProfiles(), dao.observeImages(), dao.observeHours(), dao.observeLinks(), dao.observeDocuments(), ::PartnerPrimary)
            val secondary = combine(dao.observeListings(), dao.observeListingImages(), dao.observeListingFields(), dao.observeBookings(), dao.observeSession(), ::PartnerSecondary)
            combine(primary, secondary) { first, second ->
                businessPartnerState(first.profiles, first.images, first.hours, first.links, first.documents, second.listings,
                    second.listingImages, second.listingFields, second.bookings, second.session)
            }.collect { persisted ->
                val transient = mutableState.value
                mutableState.value = persisted.copy(listingDraft = transient.listingDraft, profileDraft = transient.profileDraft,
                    listingFilter = transient.listingFilter, bookingFilter = transient.bookingFilter, loginPassword = transient.loginPassword,
                    pickerDocumentType = transient.pickerDocumentType, pickerError = transient.pickerError)
            }
        }
    }

    fun update(transform: (BusinessPartnerState) -> BusinessPartnerState) {
        val updated = synchronized(this) { transform(mutableState.value).also { mutableState.value = it } }
        dao?.let { storage -> scope.launch { runCatching { storage.replace(updated.toSnapshot()) } } }
    }

    fun signOut() = update { current -> BusinessPartnerState(profile = current.profile, documents = current.documents,
        listings = current.listings, bookings = current.bookings, demoLoaded = current.demoLoaded) }

    fun deleteAccount() = update { BusinessPartnerState() }

    fun populateDemo() = update { current ->
        if (current.demoLoaded) current.copy(loggedIn = true) else current.copy(
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
                PartnerBooking("booking-2", "Daniel Silva", "Yala Safari Tour", "18 Sep 2026", 36000.0, "Two travellers, morning departure please.", BookingStatus.CONFIRMED, "2026-09-02", 2),
                PartnerBooking("booking-3", "Nimali Fernando", "Travel Camping Kit", "1–3 Sep 2026", 36000.0, "Thank you for the equipment!", BookingStatus.COMPLETED, "2026-08-28", 1),
            ),
        )
    }
}

private data class PartnerPrimary(
    val profiles: List<BusinessProfileEntity>, val images: List<BusinessImageEntity>, val hours: List<BusinessHoursEntity>,
    val links: List<BusinessSocialLinkEntity>, val documents: List<BusinessDocumentEntity>,
)
private data class PartnerSecondary(
    val listings: List<BusinessListingEntity>, val listingImages: List<BusinessListingImageEntity>,
    val listingFields: List<BusinessListingFieldEntity>, val bookings: List<PartnerBookingEntity>, val session: BusinessPartnerSessionEntity?,
)
