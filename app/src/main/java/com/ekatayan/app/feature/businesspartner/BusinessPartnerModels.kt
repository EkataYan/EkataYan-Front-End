package com.ekatayan.app.feature.businesspartner

import androidx.annotation.StringRes
import com.ekatayan.app.R

enum class BusinessType(@get:StringRes val label: Int) {
    HOTEL(R.string.bp_hotel), RESTAURANT(R.string.bp_restaurant), TRANSPORT(R.string.bp_transport),
    TOURS(R.string.bp_tours), VACATION(R.string.bp_vacation), GEAR(R.string.bp_gear),
    EQUIPMENT(R.string.bp_equipment), OTHER(R.string.bp_other),
}

enum class ListingCategory(@get:StringRes val label: Int) {
    ROOMS(R.string.bp_rooms), ACTIVITIES(R.string.bp_activities), RENTALS(R.string.bp_rentals), DINING(R.string.bp_dining),
    TRANSPORT(R.string.bp_transport), VACATION(R.string.bp_vacation), OTHER(R.string.bp_other),
}

enum class ListingStatus(@get:StringRes val label: Int) {
    ACTIVE(R.string.bp_active), INACTIVE(R.string.bp_inactive), DRAFT(R.string.bp_draft),
}

enum class BookingStatus(@get:StringRes val label: Int) {
    PENDING(R.string.bp_pending), CONFIRMED(R.string.bp_confirmed), COMPLETED(R.string.bp_completed), CANCELLED(R.string.bp_cancelled),
}

enum class DocumentType(@get:StringRes val label: Int, val required: Boolean) {
    REGISTRATION(R.string.bp_registration, true), OWNER_ID(R.string.bp_owner_id, true), SUPPORTING(R.string.bp_supporting, false),
}

enum class BusinessDay(@get:StringRes val label: Int) {
    MONDAY(R.string.bp_monday), TUESDAY(R.string.bp_tuesday), WEDNESDAY(R.string.bp_wednesday),
    THURSDAY(R.string.bp_thursday), FRIDAY(R.string.bp_friday), SATURDAY(R.string.bp_saturday), SUNDAY(R.string.bp_sunday),
}

enum class SocialPlatform(@get:StringRes val label: Int) {
    WEBSITE(R.string.bp_website), FACEBOOK(R.string.bp_facebook), INSTAGRAM(R.string.bp_instagram),
    TIKTOK(R.string.bp_tiktok), WHATSAPP(R.string.bp_whatsapp),
}

data class BusinessHours(val day: BusinessDay, val open: Boolean = true, val openingMinutes: Int = 540, val closingMinutes: Int = 1020)
data class BusinessDocument(val uri: String, val filename: String)
data class SocialLink(val platform: SocialPlatform, val url: String)

data class BusinessPartnerProfile(
    val id: String = "local-business",
    val businessType: BusinessType? = null,
    val businessName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val locationLabel: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val businessImageUris: List<String> = emptyList(),
    val businessHours: List<BusinessHours> = BusinessDay.entries.map { BusinessHours(it) },
    val socialLinks: List<SocialLink> = emptyList(),
    val submitted: Boolean = false,
    val city: String = "",
    val district: String = "",
    val country: String = "",
    val ownerName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
)

data class BusinessListing(
    val id: String,
    val title: String,
    val category: ListingCategory,
    val description: String,
    val price: Double,
    val priceUnit: String,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val location: String = "",
    val imageUris: List<String> = emptyList(),
    val imageRes: Int? = null,
    val details: String = "",
    val views: Int = 0,
    val inquiries: Int = 0,
    val serviceFields: Map<ListingField, String> = emptyMap(),
    val availability: ListingAvailability = ListingAvailability(),
)

data class ListingDraft(
    val id: String? = null,
    val title: String = "",
    val category: ListingCategory = ListingCategory.ROOMS,
    val description: String = "",
    val price: String = "",
    val priceUnit: String = "",
    val status: ListingStatus = ListingStatus.DRAFT,
    val location: String = "",
    val imageUris: List<String> = emptyList(),
    val imageRes: Int? = null,
    val details: String = "",
    val serviceFields: Map<ListingField, String> = emptyMap(),
    val availability: ListingAvailability = ListingAvailability(),
)

data class PartnerBooking(
    val id: String,
    val travellerName: String,
    val listingName: String,
    val dateLabel: String,
    val amount: Double,
    val message: String,
    val status: BookingStatus = BookingStatus.PENDING,
    val bookedOn: String = "",
    val quantity: Int = 1,
)

data class BusinessPartnerState(
    val profile: BusinessPartnerProfile = BusinessPartnerProfile(),
    val documents: Map<DocumentType, BusinessDocument> = emptyMap(),
    val listings: List<BusinessListing> = emptyList(),
    val bookings: List<PartnerBooking> = emptyList(),
    val listingDraft: ListingDraft? = null,
    val profileDraft: BusinessPartnerProfile? = null,
    val listingFilter: ListingCategory? = null,
    val bookingFilter: BookingStatus? = null,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val loggedIn: Boolean = false,
    val pickerDocumentType: DocumentType? = null,
    val pickerError: Boolean = false,
) {
    val profileViews: Int get() = listings.sumOf { it.views }
    val inquiries: Int get() = listings.sumOf { it.inquiries }
}

object PartnerValidation {
    fun phone(value: String): Boolean = value.count(Char::isDigit) in 7..15 &&
        value.all { it.isDigit() || it in "+ ()-" } && value.count { it == '+' } <= 1 &&
        ('+' !in value || value.trim().startsWith('+'))
    fun email(value: String): Boolean = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim())
    fun information(profile: BusinessPartnerProfile): Boolean = profile.businessName.isNotBlank() &&
        profile.businessType != null && email(profile.email) && phone(profile.phone) &&
        profile.address.isNotBlank() && profile.city.isNotBlank() && profile.district.isNotBlank() &&
        profile.country.isNotBlank() && profile.ownerName.isNotBlank() &&
        email(profile.contactEmail) && phone(profile.contactPhone)
    fun details(profile: BusinessPartnerProfile): Boolean = profile.description.isNotBlank() &&
        profile.description.length <= 500 && profile.businessImageUris.isNotEmpty()
    fun listing(draft: ListingDraft): Boolean = draft.title.isNotBlank() && draft.description.isNotBlank() &&
        draft.price.toDoubleOrNull()?.let { it.isFinite() && it > 0 } == true &&
        draft.priceUnit.isNotBlank() && draft.location.isNotBlank() &&
        (draft.imageUris.isNotEmpty() || draft.imageRes != null) && draft.availability.isValid() &&
        draft.category.fields.all { it.isValid(draft.serviceFields[it].orEmpty()) }
}
