package com.ekatayan.app.viewmodel

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerViewModelTest {
    @Test
    fun fixedTravellerTypesSetPartySizeAndClearCustomCount() {
        val viewModel = PlannerViewModel()
        viewModel.updateTravellerType(TravellerType.FRIENDS)
        viewModel.updateCustomPeopleCount("6")

        viewModel.updateTravellerType(TravellerType.SOLO)
        assertEquals(1, viewModel.uiState.value.partySize)
        assertEquals("", viewModel.uiState.value.customPeopleCount)
        assertFalse(viewModel.uiState.value.requiresCustomPeopleCount)

        viewModel.updateTravellerType(TravellerType.FAMILY)
        viewModel.updateCustomPeopleCount("5")
        viewModel.updateTravellerType(TravellerType.COUPLE)
        assertEquals(2, viewModel.uiState.value.partySize)
        assertEquals("", viewModel.uiState.value.customPeopleCount)
    }

    @Test
    fun familyAndFriendsRequirePositiveNumericCustomCount() {
        val viewModel = PlannerViewModel()
        viewModel.updateDestination("Ella")
        viewModel.updateTravellerType(TravellerType.FAMILY)
        viewModel.updateCustomPeopleCount("-0 guests")
        viewModel.updateStartDate(LocalDate.of(2026, 9, 20))
        viewModel.updateEndDate(LocalDate.of(2026, 9, 22))

        assertEquals("0", viewModel.uiState.value.customPeopleCount)
        assertNull(viewModel.uiState.value.partySize)
        assertFalse(viewModel.validate())

        viewModel.updateTravellerType(TravellerType.FRIENDS)
        viewModel.updateCustomPeopleCount("6 people")
        assertEquals(6, viewModel.uiState.value.partySize)
        assertTrue(viewModel.validate())
    }

    @Test
    fun invalidEndDateIsRejectedAndCompleteSoloFormValidates() {
        val viewModel = PlannerViewModel()
        val start = LocalDate.of(2026, 10, 10)
        viewModel.updateDestination("Kandy")
        viewModel.updateTravellerType(TravellerType.SOLO)
        viewModel.updateStartDate(start)
        viewModel.updateEndDate(start.minusDays(1))

        assertNull(viewModel.uiState.value.endDate)
        assertFalse(viewModel.validate())

        viewModel.updateEndDate(start)
        assertTrue(viewModel.validate())
    }
}
