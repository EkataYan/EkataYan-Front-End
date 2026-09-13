package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.TripsRepository
import com.ekatayan.app.utils.parseTripDate
import com.ekatayan.app.utils.tripDateFormatter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CreateTripField { NAME, DESTINATION, START, END, BUDGET, NOTES }

data class CreateTripUiState(
    val name: String = "", val destination: String = "",
    val startText: String = "", val endText: String = "",
    val budget: String = "", val notes: String = "", val errorRes: Int? = null,
) {
    val startDate: LocalDate? get() = parseTripDate(startText)
    val endDate: LocalDate? get() = parseTripDate(endText)
}

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val repository: TripsRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CreateTripUiState(
        name = savedState["draftName"] ?: "",
        destination = savedState["draftDestination"] ?: savedState["destination"] ?: "",
        startText = savedState["draftStart"] ?: savedState["start"] ?: "",
        endText = savedState["draftEnd"] ?: savedState["end"] ?: "",
        budget = savedState["draftBudget"] ?: savedState["budget"] ?: "",
        notes = savedState["draftNotes"] ?: savedState.get<String>("preferences")?.let { "AI preferences: $it" } ?: "",
        errorRes = savedState["draftError"],
    ))
    val uiState = mutableState.asStateFlow()

    private fun update(state: CreateTripUiState) {
        savedState["draftName"] = state.name
        savedState["draftDestination"] = state.destination
        savedState["draftStart"] = state.startText
        savedState["draftEnd"] = state.endText
        savedState["draftBudget"] = state.budget
        savedState["draftNotes"] = state.notes
        savedState["draftError"] = state.errorRes
        mutableState.value = state
    }

    fun updateField(field: CreateTripField, value: String) {
        val state = uiState.value.copy(errorRes = null)
        update(when (field) {
            CreateTripField.NAME -> state.copy(name = value)
            CreateTripField.DESTINATION -> state.copy(destination = value)
            CreateTripField.START -> state.copy(startText = value)
            CreateTripField.END -> state.copy(endText = value)
            CreateTripField.BUDGET -> state.copy(budget = value)
            CreateTripField.NOTES -> state.copy(notes = value)
        })
    }

    fun selectDate(forStart: Boolean, date: LocalDate): Boolean {
        val state = uiState.value
        if (!forStart && state.startDate?.let { date.isBefore(it) } == true) {
            update(state.copy(errorRes = R.string.create_trip_date_range_invalid))
            return false
        }
        update(if (forStart) state.copy(startText = date.format(tripDateFormatter),
            endText = if (state.endDate?.let { date.isAfter(it) } == true) "" else state.endText, errorRes = null)
        else state.copy(endText = date.format(tripDateFormatter), errorRes = null))
        return true
    }

    fun save(): Boolean {
        val state = uiState.value
        val start = state.startDate
        val end = state.endDate
        val error = when {
            state.name.isBlank() -> R.string.create_trip_name_required
            state.destination.isBlank() -> R.string.create_trip_destination_required
            start == null || end == null -> if (state.startText.isNotBlank() || state.endText.isNotBlank()) R.string.create_trip_date_invalid else R.string.create_trip_dates_required
            start.isAfter(end) -> R.string.create_trip_date_range_invalid
            state.budget.isNotBlank() && state.budget.toDoubleOrNull() == null -> R.string.create_trip_budget_invalid
            else -> null
        }
        update(state.copy(errorRes = error))
        if (error != null || start == null || end == null) return false
        repository.addTrip(state.name.trim(), state.destination.trim(), start, end, state.budget.trim(), state.notes.trim())
        viewModelScope.launch {
            runCatching { repository.createManualTrip(state.name.trim(), state.destination.trim(), start, end, state.budget.trim(), state.notes.trim()) }
        }
        return true
    }
}
