package com.ekatayan.app.feature.businesspartner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BusinessPartnerViewModel @Inject constructor(private val repository: LocalBusinessPartnerRepository) : ViewModel() {
    val uiState = repository.state

    fun updateProfile(transform: (BusinessPartnerProfile) -> BusinessPartnerProfile) = repository.update { state ->
        if (state.profileDraft != null) state.copy(profileDraft = transform(state.profileDraft))
        else state.copy(profile = transform(state.profile))
    }

    fun canContinue(step: Int): Boolean = uiState.value.let { state -> when (step) {
        1 -> state.profile.businessType != null
        2 -> PartnerValidation.information(state.profile)
        3 -> PartnerValidation.details(state.profile)
        4 -> DocumentType.entries.filter { it.required }.all { it in state.documents }
        else -> true
    } }

    fun submit(): Boolean {
        if (!(1..4).all(::canContinue)) return false
        repository.update { it.copy(profile = it.profile.copy(submitted = true), loginEmail = it.profile.email) }
        return true
    }

    fun setLoginEmail(value: String) = repository.update { it.copy(loginEmail = value) }
    fun setLoginPassword(value: String) = repository.update { it.copy(loginPassword = value) }
    fun login(): Boolean {
        if (!PartnerValidation.email(uiState.value.loginEmail) || uiState.value.loginPassword.isBlank()) return false
        repository.populateDemo()
        repository.update { it.copy(loginPassword = "") }
        return true
    }

    fun signOut() = repository.signOut()
    fun deleteAccount() = repository.deleteAccount()

    fun selectDocument(type: DocumentType) = repository.update { it.copy(pickerDocumentType = type) }
    fun documentPicked(document: BusinessDocument?) = repository.update { state ->
        val type = state.pickerDocumentType
        state.copy(documents = if (type != null && document != null) state.documents + (type to document) else state.documents, pickerDocumentType = null)
    }
    fun removeDocument(type: DocumentType) = repository.update { it.copy(documents = it.documents - type) }
    fun pickerError(value: Boolean) = repository.update { it.copy(pickerError = value) }
    fun filterListings(value: ListingCategory?) = repository.update { it.copy(listingFilter = value) }
    fun filterBookings(value: BookingStatus?) = repository.update { it.copy(bookingFilter = value) }

    fun beginListing(id: String? = null) = repository.update { state ->
        val listing = state.listings.find { it.id == id }
        state.copy(listingDraft = if (listing != null) ListingDraft(
            listing.id, listing.title, listing.category, listing.description, listing.price.toString(), listing.priceUnit,
            listing.status, listing.location, listing.imageUris, listing.imageRes, listing.details,
            listing.serviceFields, listing.availability,
        ) else ListingDraft(category = state.profile.businessType.listingCategory(),
            location = state.profile.locationLabel.ifBlank {
                listOf(state.profile.city, state.profile.district, state.profile.country).filter(String::isNotBlank).joinToString(", ")
            }))
    }

    fun updateListingDraft(transform: (ListingDraft) -> ListingDraft) = repository.update { state ->
        state.copy(listingDraft = state.listingDraft?.let(transform))
    }
    fun discardListingDraft() = repository.update { it.copy(listingDraft = null) }
    fun saveListing(): Boolean {
        val draft = uiState.value.listingDraft ?: return false
        if (!PartnerValidation.listing(draft)) return false
        repository.update { state ->
            val previous = state.listings.find { it.id == draft.id }
            val listing = BusinessListing(draft.id ?: UUID.randomUUID().toString(), draft.title.trim(), draft.category,
                draft.description.trim(), draft.price.toDouble(), draft.priceUnit.trim(), draft.status,
                draft.location.trim(), draft.imageUris, draft.imageRes, draft.details.trim(), previous?.views ?: 0, previous?.inquiries ?: 0,
                draft.serviceFields.filterKeys { it in draft.category.fields }.mapValues { it.value.trim() },
                draft.availability.copy(timeSlots = if (draft.category == ListingCategory.ACTIVITIES) draft.availability.timeSlots else ""))
            state.copy(listings = if (previous == null) listOf(listing) + state.listings else state.listings.map { if (it.id == listing.id) listing else it },
                listingDraft = null, listingFilter = null)
        }
        return true
    }
    fun deleteListing(id: String) = repository.update { it.copy(listings = it.listings.filterNot { listing -> listing.id == id }) }
    fun changeStatus(id: String, status: ListingStatus) = repository.update { state ->
        state.copy(listings = state.listings.map { if (it.id == id) it.copy(status = status) else it })
    }
    fun changeBookingStatus(id: String, status: BookingStatus) = repository.update { state ->
        state.copy(bookings = state.bookings.map { booking ->
            val allowed = when (booking.status) {
                BookingStatus.PENDING -> status == BookingStatus.CONFIRMED || status == BookingStatus.CANCELLED
                BookingStatus.CONFIRMED -> status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED
                else -> false
            }
            if (booking.id == id && allowed) booking.copy(status = status) else booking
        })
    }
    fun beginProfileEdit() = repository.update { it.copy(profileDraft = it.profile) }
    fun discardProfileEdit() = repository.update { it.copy(profileDraft = null) }
    fun saveProfile(): Boolean {
        val draft = uiState.value.profileDraft ?: return false
        if (!PartnerValidation.information(draft) || !PartnerValidation.details(draft)) return false
        repository.update { it.copy(profile = draft, profileDraft = null) }
        return true
    }
    fun setProfilePhoto(uri: String?) = repository.update { it.copy(profile = it.profile.copy(imageUri = uri), profileDraft = it.profileDraft?.copy(imageUri = uri)) }
}
