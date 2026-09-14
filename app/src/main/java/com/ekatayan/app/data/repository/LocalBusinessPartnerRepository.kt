package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.database.*
import com.ekatayan.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Room-backed store for business data explicitly entered on this device. */
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

    fun signOut() = update { current -> BusinessPartnerState(
        profile = current.profile,
        documents = current.documents,
        listings = current.listings,
        bookings = current.bookings,
    ) }

    fun deleteAccount() = update { BusinessPartnerState() }
}

private data class PartnerPrimary(
    val profiles: List<BusinessProfileEntity>, val images: List<BusinessImageEntity>, val hours: List<BusinessHoursEntity>,
    val links: List<BusinessSocialLinkEntity>, val documents: List<BusinessDocumentEntity>,
)

private data class PartnerSecondary(
    val listings: List<BusinessListingEntity>, val listingImages: List<BusinessListingImageEntity>,
    val listingFields: List<BusinessListingFieldEntity>, val bookings: List<PartnerBookingEntity>, val session: BusinessPartnerSessionEntity?,
)
