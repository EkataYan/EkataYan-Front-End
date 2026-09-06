package com.ekatayan.app.feature.businesspartner

import org.junit.Assert.*
import org.junit.Test

class BusinessPartnerViewModelTest {
    private fun model() = BusinessPartnerViewModel(LocalBusinessPartnerRepository())

    private fun register(vm: BusinessPartnerViewModel) {
        vm.updateProfile { it.copy(businessType = BusinessType.HOTEL, businessName = "Sunrise Hotel",
            email = "owner@example.com", phone = "+94771234567", address = "Beach Road",
            city = "Galle", district = "Galle", country = "Sri Lanka", ownerName = "Amara",
            contactEmail = "contact@example.com", contactPhone = "+94771234567",
            locationLabel = "Galle, Sri Lanka", description = "Seaside accommodation",
            businessImageUris = listOf("content://photos/1")) }
        DocumentType.entries.filter { it.required }.forEach { type ->
            vm.selectDocument(type)
            vm.documentPicked(BusinessDocument("content://documents/${type.name}", "${type.name}.pdf"))
        }
        assertTrue(vm.submit())
        vm.setLoginPassword("demo")
        assertTrue(vm.login())
    }

    @Test fun signOutClearsSessionAndDraftsButPreservesAccountChangesOnLogin() {
        val vm = model()
        register(vm)
        vm.deleteListing("room")
        vm.changeBookingStatus("booking-1", BookingStatus.CONFIRMED)
        val before = vm.uiState.value
        vm.beginListing()
        vm.beginProfileEdit()
        vm.setLoginPassword("temporary")
        vm.pickerError(true)
        vm.signOut()
        val signedOut = vm.uiState.value
        assertFalse(signedOut.loggedIn)
        assertEquals("", signedOut.loginEmail)
        assertEquals("", signedOut.loginPassword)
        assertNull(signedOut.listingDraft)
        assertNull(signedOut.profileDraft)
        assertFalse(signedOut.pickerError)
        assertEquals(before.profile, signedOut.profile)
        vm.setLoginEmail("owner@example.com")
        vm.setLoginPassword("demo")
        assertTrue(vm.login())
        assertEquals(before.listings, vm.uiState.value.listings)
        assertEquals(before.bookings, vm.uiState.value.bookings)
    }

    @Test fun deleteAccountClearsAllLocalDataAndCannotRestoreOldAccount() {
        val vm = model()
        register(vm)
        vm.beginListing()
        vm.beginProfileEdit()
        vm.selectDocument(DocumentType.OWNER_ID)
        vm.deleteAccount()
        assertEquals(BusinessPartnerState(), vm.uiState.value)
        assertFalse(vm.login())
        assertFalse(vm.submit())
    }

    @Test fun onboardingRequiresInformationPhotosAndBothDocuments() {
        val vm = model()
        assertFalse(vm.canContinue(1))
        assertFalse(vm.submit())
        register(vm)
        vm.removeDocument(DocumentType.OWNER_ID)
        assertFalse(vm.canContinue(4))
        vm.updateProfile { it.copy(businessImageUris = emptyList()) }
        assertFalse(vm.canContinue(3))
    }

    @Test fun listingCreateEditCancelStatusAndDeleteShareOneState() {
        val vm = model()
        register(vm)
        val count = vm.uiState.value.listings.size
        vm.beginListing()
        assertFalse(vm.saveListing())
        vm.updateListingDraft { it.copy(title = "Garden Room", description = "Quiet room", price = "9500",
            priceUnit = "night", imageUris = listOf("content://photos/2")) }
        assertTrue(vm.saveListing())
        val added = vm.uiState.value.listings.first()
        assertEquals(count + 1, vm.uiState.value.listings.size)
        vm.beginListing(added.id)
        vm.updateListingDraft { it.copy(title = "Discarded") }
        vm.discardListingDraft()
        assertEquals("Garden Room", vm.uiState.value.listings.first().title)
        vm.beginListing(added.id)
        vm.updateListingDraft { it.copy(title = "Family Room", price = "11000") }
        assertTrue(vm.saveListing())
        assertEquals("Family Room", vm.uiState.value.listings.first().title)
        vm.changeStatus(added.id, ListingStatus.ACTIVE)
        assertEquals(ListingStatus.ACTIVE, vm.uiState.value.listings.first().status)
        vm.deleteListing(added.id)
        assertEquals(count, vm.uiState.value.listings.size)
        assertFalse(vm.uiState.value.listings.any { it.id == added.id })
    }

    @Test fun profileEditsCommitOrCancelAndPhotoUpdatesImmediately() {
        val vm = model()
        register(vm)
        vm.beginProfileEdit()
        vm.updateProfile { it.copy(businessName = "Changed") }
        assertEquals("Sunrise Hotel", vm.uiState.value.profile.businessName)
        vm.discardProfileEdit()
        vm.beginProfileEdit()
        vm.updateProfile { it.copy(businessName = "Ocean Hotel") }
        assertTrue(vm.saveProfile())
        assertEquals("Ocean Hotel", vm.uiState.value.profile.businessName)
        vm.setProfilePhoto("content://photos/logo")
        assertEquals("content://photos/logo", vm.uiState.value.profile.imageUri)
    }

    @Test fun bookingTransitionsRejectChangesAfterCompletion() {
        val vm = model()
        register(vm)
        val id = vm.uiState.value.bookings.first().id
        vm.changeBookingStatus(id, BookingStatus.COMPLETED)
        assertEquals(BookingStatus.PENDING, vm.uiState.value.bookings.first().status)
        vm.changeBookingStatus(id, BookingStatus.CONFIRMED)
        vm.changeBookingStatus(id, BookingStatus.COMPLETED)
        vm.changeBookingStatus(id, BookingStatus.CANCELLED)
        assertEquals(BookingStatus.COMPLETED, vm.uiState.value.bookings.first().status)
    }

    @Test fun invalidPricesAndEmailsAreRejected() {
        assertFalse(PartnerValidation.email("bad@"))
        assertFalse(PartnerValidation.email("a b@example.com"))
        val draft = ListingDraft(title = "Room", description = "Room", priceUnit = "night", location = "Galle", imageUris = listOf("content://photo"))
        listOf("", "-1", "0", "NaN", "Infinity", "abc").forEach { assertFalse(PartnerValidation.listing(draft.copy(price = it))) }
        assertTrue(PartnerValidation.listing(draft.copy(price = "12500.50")))
    }

    @Test fun returningLoginDoesNotResetDeletedListings() {
        val vm = model()
        register(vm)
        vm.deleteListing("room")
        vm.setLoginPassword("demo")
        assertTrue(vm.login())
        assertFalse(vm.uiState.value.listings.any { it.id == "room" })
    }

    @Test fun categoryDefaultsCoverEveryBusinessType() {
        val expected = mapOf(BusinessType.HOTEL to ListingCategory.ROOMS,
            BusinessType.RESTAURANT to ListingCategory.DINING, BusinessType.TRANSPORT to ListingCategory.TRANSPORT,
            BusinessType.TOURS to ListingCategory.ACTIVITIES, BusinessType.VACATION to ListingCategory.VACATION,
            BusinessType.GEAR to ListingCategory.RENTALS, BusinessType.EQUIPMENT to ListingCategory.RENTALS,
            BusinessType.OTHER to ListingCategory.OTHER)
        val vm = model()
        expected.forEach { (type, category) ->
            vm.updateProfile { it.copy(businessType = type, city = "Galle", country = "Sri Lanka") }
            vm.beginListing()
            assertEquals(category, vm.uiState.value.listingDraft!!.category)
            assertEquals("Galle, Sri Lanka", vm.uiState.value.listingDraft!!.location)
        }
    }

    @Test fun serviceFieldsAndAvailabilitySurviveSaveEditAndCancelForEveryCategory() {
        val vm = model()
        register(vm)
        ListingCategory.entries.forEach { category ->
            val fields = category.fields.associateWith { field -> when (field.kind) {
                FieldKind.TEXT -> "Service information"
                FieldKind.COUNT, FieldKind.QUANTITY -> "2"
                FieldKind.MONEY -> "500.50"
                FieldKind.TIME -> "14:30"
                FieldKind.BOOLEAN -> "true"
            } }
            val availability = ListingAvailability(false, "2026-10-01", "2026-10-31",
                if (category == ListingCategory.ACTIVITIES) "09:00, 14:30" else "")
            vm.beginListing()
            vm.updateListingDraft { it.copy(title = "Service", description = "Description", price = "100.50",
                priceUnit = "item", imageUris = listOf("content://photo/1"), category = category,
                serviceFields = fields, availability = availability, details = "Terms") }
            assertTrue(vm.saveListing())
            val saved = vm.uiState.value.listings.first()
            assertEquals(fields, saved.serviceFields)
            assertEquals(availability, saved.availability)
            vm.beginListing(saved.id)
            assertEquals(fields, vm.uiState.value.listingDraft!!.serviceFields)
            vm.updateListingDraft { it.copy(availability = it.availability.copy(available = true), serviceFields = emptyMap()) }
            vm.discardListingDraft()
            assertEquals(saved, vm.uiState.value.listings.first())
            vm.beginListing(saved.id)
            vm.updateListingDraft { it.copy(availability = it.availability.copy(available = true)) }
            assertTrue(vm.saveListing())
            assertTrue(vm.uiState.value.listings.first().availability.available)
            assertEquals(fields, vm.uiState.value.listings.first().serviceFields)
        }
    }

    @Test fun invalidAvailabilityAndServiceNumbersCannotBeSaved() {
        val vm = model()
        register(vm)
        vm.beginListing("room")
        listOf(ListingAvailability(from = "2026-10-01"),
            ListingAvailability(from = "2026-02-30", until = "2026-03-01"),
            ListingAvailability(from = "2026-10-10", until = "2026-10-01"),
            ListingAvailability(timeSlots = "25:00")).forEach { availability ->
            vm.updateListingDraft { it.copy(availability = availability) }
            assertFalse(vm.saveListing())
        }
        vm.updateListingDraft { it.copy(availability = ListingAvailability(), serviceFields = mapOf(ListingField.GUESTS to "-1")) }
        assertFalse(vm.saveListing())
        assertFalse(ListingField.QUANTITY.isValid("1.5"))
        assertFalse(ListingField.DEPOSIT.isValid("NaN"))
        assertFalse(ListingField.CHECK_IN.isValid("24:00"))
        assertTrue(ListingField.QUANTITY.isValid("0"))
    }

    @Test fun changingCategoryDoesNotSaveUnrelatedFieldsOrTimeSlots() {
        val vm = model()
        register(vm)
        vm.beginListing("safari")
        vm.updateListingDraft { it.copy(category = ListingCategory.TRANSPORT,
            serviceFields = mapOf(ListingField.DURATION to "4 hours", ListingField.PASSENGERS to "8"),
            availability = ListingAvailability(timeSlots = "09:00")) }
        assertTrue(vm.saveListing())
        val saved = vm.uiState.value.listings.first { it.id == "safari" }
        assertEquals(mapOf(ListingField.PASSENGERS to "8"), saved.serviceFields)
        assertEquals("", saved.availability.timeSlots)
        assertEquals(32, saved.views)
    }

    @Test fun ownerAndAddressFieldsAreRequiredAndProfileEditsPreserveThem() {
        val vm = model()
        register(vm)
        val valid = vm.uiState.value.profile
        listOf(valid.copy(city = ""), valid.copy(district = ""), valid.copy(country = ""),
            valid.copy(ownerName = ""), valid.copy(contactEmail = "invalid"),
            valid.copy(contactPhone = "abc1234567"), valid.copy(businessType = null)).forEach {
            assertFalse(PartnerValidation.information(it))
        }
        vm.beginProfileEdit()
        vm.updateProfile { it.copy(city = "Matara", ownerName = "New Manager") }
        assertTrue(vm.saveProfile())
        assertEquals("Matara", vm.uiState.value.profile.city)
        assertEquals("New Manager", vm.uiState.value.profile.ownerName)
    }
}
