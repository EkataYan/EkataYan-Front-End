package com.ekatayan.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ekatayan.app.R
import com.ekatayan.app.data.repository.TripsRepository
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.After
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTripViewModelTest {
    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

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
        assertEquals(initialCount, repository.trips.value.size)
        assertFalse(viewModel.uiState.value.saveSucceeded)
        assertNotNull(viewModel.uiState.value.operationErrorRes)
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
        assertEquals("Nature", first.uiState.value.notes)
        first.updateField(CreateTripField.DESTINATION, "Galle")
        first.updateField(CreateTripField.NOTES, "")
        val recreated = CreateTripViewModel(repository, handle)
        assertEquals("Galle", recreated.uiState.value.destination)
        assertEquals("", recreated.uiState.value.notes)
    }
}
