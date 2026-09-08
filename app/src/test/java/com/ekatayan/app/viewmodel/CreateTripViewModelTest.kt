package com.ekatayan.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ekatayan.app.R
import com.ekatayan.app.data.repository.TripsRepository
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class CreateTripViewModelTest {
    @Test
    fun invalidFormDoesNotWriteAndValidFormSavesTrimmedValues() {
        val repository = TripsRepository()
        val initialCount = repository.trips.value.size
        val viewModel = CreateTripViewModel(repository, SavedStateHandle())
        assertFalse(viewModel.save())
        assertEquals(R.string.create_trip_name_required, viewModel.uiState.value.errorRes)
        assertEquals(initialCount, repository.trips.value.size)
        viewModel.updateField(CreateTripField.NAME, "  Weekend  ")
        viewModel.updateField(CreateTripField.DESTINATION, "  Kandy  ")
        viewModel.updateField(CreateTripField.START, "25 Sep 2026")
        viewModel.updateField(CreateTripField.END, "26 Sep 2026")
        viewModel.updateField(CreateTripField.BUDGET, "invalid")
        assertFalse(viewModel.save())
        assertEquals(R.string.create_trip_budget_invalid, viewModel.uiState.value.errorRes)
        viewModel.updateField(CreateTripField.BUDGET, " 1000 ")
        assertTrue(viewModel.save())
        assertEquals(initialCount + 1, repository.trips.value.size)
        val saved = repository.trips.value.last()
        assertEquals("Weekend", saved.customName)
        assertEquals("Kandy", saved.customLocation)
        assertEquals("1000", saved.budget)
    }

    @Test
    fun datePickerRejectsEarlierEndAndClearsEndWhenStartMovesPastIt() {
        val viewModel = CreateTripViewModel(TripsRepository(), SavedStateHandle())
        viewModel.selectDate(true, LocalDate.of(2026, 9, 25))
        assertFalse(viewModel.selectDate(false, LocalDate.of(2026, 9, 24)))
        assertEquals(R.string.create_trip_date_range_invalid, viewModel.uiState.value.errorRes)
        assertTrue(viewModel.selectDate(false, LocalDate.of(2026, 9, 26)))
        assertTrue(viewModel.selectDate(true, LocalDate.of(2026, 9, 27)))
        assertEquals("", viewModel.uiState.value.endText)
        assertNull(viewModel.uiState.value.errorRes)
    }

    @Test
    fun draftValuesTakePrecedenceOverNavigationArgumentsWhenRecreated() {
        val handle = SavedStateHandle(mapOf("destination" to "Ella", "preferences" to "Nature"))
        val repository = TripsRepository()
        val first = CreateTripViewModel(repository, handle)
        assertEquals("Ella", first.uiState.value.destination)
        assertEquals("AI preferences: Nature", first.uiState.value.notes)
        first.updateField(CreateTripField.DESTINATION, "Galle")
        first.updateField(CreateTripField.NOTES, "")
        val recreated = CreateTripViewModel(repository, handle)
        assertEquals("Galle", recreated.uiState.value.destination)
        assertEquals("", recreated.uiState.value.notes)
    }
}
