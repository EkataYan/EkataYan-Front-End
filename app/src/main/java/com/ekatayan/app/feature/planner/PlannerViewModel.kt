package com.ekatayan.app.feature.planner

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

data class PlannerUiState(
    val destination: String = "", val startDate: LocalDate? = null, val endDate: LocalDate? = null,
    val budget: String = "", val travelers: String = "2 travelers",
    val accommodation: String = "Any accommodation", val transport: String = "Public transport",
    val tripType: String = "A balanced trip", val interests: String = "Nature & culture", val error: String? = null,
)

enum class PreferenceKind { TRAVELERS, ACCOMMODATION, TRANSPORT, TRIP_TYPE, INTERESTS }

@HiltViewModel
class PlannerViewModel @Inject constructor() : ViewModel() {
    val uiState = mutableStateOf(PlannerUiState())
    fun updateDestination(value: String) { uiState.value = uiState.value.copy(destination = value, error = null) }
    fun updateBudget(value: String) { uiState.value = uiState.value.copy(budget = value.filter(Char::isDigit), error = null) }
    fun updateStartDate(value: LocalDate) {
        val previousEnd = uiState.value.endDate
        uiState.value = uiState.value.copy(
            startDate = value,
            endDate = previousEnd?.takeUnless { it.isBefore(value) },
            error = if (previousEnd != null && previousEnd.isBefore(value)) "The end date was cleared because it was before the new start date." else null,
        )
    }
    fun updateEndDate(value: LocalDate) { uiState.value = uiState.value.copy(endDate = value, error = null) }
    fun clearStartDate() { uiState.value = uiState.value.copy(startDate = null, error = null) }
    fun clearEndDate() { uiState.value = uiState.value.copy(endDate = null, error = null) }
    fun setError(value: String) { uiState.value = uiState.value.copy(error = value) }
    fun updatePreference(kind: PreferenceKind, value: String) { uiState.value = when (kind) {
        PreferenceKind.TRAVELERS -> uiState.value.copy(travelers = value)
        PreferenceKind.ACCOMMODATION -> uiState.value.copy(accommodation = value)
        PreferenceKind.TRANSPORT -> uiState.value.copy(transport = value)
        PreferenceKind.TRIP_TYPE -> uiState.value.copy(tripType = value)
        PreferenceKind.INTERESTS -> uiState.value.copy(interests = value)
    } }
    fun validate(): Boolean {
        val state = uiState.value
        val error = when {
            state.destination.isBlank() -> "Tell us where you would like to go."
            state.startDate == null -> "Choose a start date."
            state.endDate == null -> "Choose an end date."
            state.endDate.isBefore(state.startDate) -> "End date cannot be before the start date."
            state.budget.isBlank() -> "Enter a budget to continue."
            state.budget.toLongOrNull() == null -> "Enter a valid budget."
            else -> null
        }
        uiState.value = state.copy(error = error)
        return error == null
    }
}
