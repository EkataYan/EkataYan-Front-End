package com.ekatayan.app.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.model.Itinerary
import com.ekatayan.app.data.repository.ItineraryPlanInput
import com.ekatayan.app.data.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

enum class TravellerType(val fixedPartySize: Int?) {
    SOLO(1),
    COUPLE(2),
    FAMILY(null),
    FRIENDS(null),
}

data class PlannerUiState(
    val destination: String = "",
    val additionalDestinations: List<String> = emptyList(),
    val travellerType: TravellerType? = null,
    val customPeopleCount: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val error: String? = null,
) {
    val requiresCustomPeopleCount: Boolean
        get() = travellerType == TravellerType.FAMILY || travellerType == TravellerType.FRIENDS

    val partySize: Int?
        get() = when (travellerType) {
            TravellerType.SOLO -> 1
            TravellerType.COUPLE -> 2
            TravellerType.FAMILY, TravellerType.FRIENDS ->
                customPeopleCount.toIntOrNull()?.takeIf { it > 0 }
            null -> null
        }
}

@HiltViewModel
class PlannerViewModel @Inject constructor(private val repository: ItineraryRepository) : ViewModel() {
    private val mutableUiState = mutableStateOf(PlannerUiState())
    val uiState: State<PlannerUiState> = mutableUiState

    fun updateDestination(value: String) {
        mutableUiState.value = uiState.value.copy(destination = value, error = null)
    }

    fun addDestination() {
        mutableUiState.value = uiState.value.copy(
            additionalDestinations = uiState.value.additionalDestinations + "",
            error = null,
        )
    }

    fun updateAdditionalDestination(index: Int, value: String) {
        val destinations = uiState.value.additionalDestinations
        if (index !in destinations.indices) return
        mutableUiState.value = uiState.value.copy(
            additionalDestinations = destinations.toMutableList().apply { this[index] = value },
            error = null,
        )
    }

    fun removeAdditionalDestination(index: Int) {
        val destinations = uiState.value.additionalDestinations
        if (index !in destinations.indices) return
        mutableUiState.value = uiState.value.copy(
            additionalDestinations = destinations.filterIndexed { currentIndex, _ -> currentIndex != index },
            error = null,
        )
    }

    fun updateTravellerType(value: TravellerType) {
        mutableUiState.value = uiState.value.copy(
            travellerType = value,
            customPeopleCount = if (value.fixedPartySize != null) "" else uiState.value.customPeopleCount,
            error = null,
        )
    }

    fun updateCustomPeopleCount(value: String) {
        mutableUiState.value = uiState.value.copy(
            customPeopleCount = value.filter(Char::isDigit).take(3),
            error = null,
        )
    }

    fun updateStartDate(value: LocalDate) {
        val previousEnd = uiState.value.endDate
        mutableUiState.value = uiState.value.copy(
            startDate = value,
            endDate = previousEnd?.takeUnless { it.isBefore(value) },
            error = if (previousEnd != null && previousEnd.isBefore(value)) {
                "The end date was cleared because it was before the new start date."
            } else {
                null
            },
        )
    }

    fun updateEndDate(value: LocalDate) {
        if (uiState.value.startDate?.let(value::isBefore) == true) {
            setError("End date cannot be before the start date.")
            return
        }
        mutableUiState.value = uiState.value.copy(endDate = value, error = null)
    }

    fun setError(value: String) {
        mutableUiState.value = uiState.value.copy(error = value)
    }

    fun validate(): Boolean {
        val state = uiState.value
        val validAdditionalDestinations = state.additionalDestinations.filterNot(String::isBlank)
        val error = when {
            state.destination.isBlank() -> "Tell us where you would like to go."
            state.travellerType == null -> "Choose who will be travelling."
            state.requiresCustomPeopleCount && state.partySize == null ->
                "Enter a valid number of people greater than zero."
            state.startDate == null -> "Choose a start date."
            state.endDate == null -> "Choose an end date."
            state.endDate.isBefore(state.startDate) -> "End date cannot be before the start date."
            else -> null
        }
        mutableUiState.value = state.copy(
            additionalDestinations = validAdditionalDestinations,
            error = error,
        )
        return error == null
    }

    fun generate() {
        if (!validate()) return
        val state = uiState.value
        mutableUiState.value = state.copy(isGenerating = true, error = null, itinerary = null)
        viewModelScope.launch {
            try {
                val itinerary = repository.generate(ItineraryPlanInput(
                    destination = state.destination.trim(), startDate = requireNotNull(state.startDate),
                    endDate = requireNotNull(state.endDate), budget = state.budget,
                    travelers = Regex("\\d+").find(state.travelers)?.value?.toIntOrNull()?.coerceIn(1, 100) ?: 1,
                    accommodation = state.accommodation, transport = state.transport,
                    travelStyle = state.tripType,
                    interests = state.interests.split(',', '&').map(String::trim).filter(String::isNotEmpty),
                ))
                mutableUiState.value = mutableUiState.value.copy(isGenerating = false, itinerary = itinerary)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableUiState.value = mutableUiState.value.copy(isGenerating = false,
                    error = error.message ?: "Itinerary generation is temporarily unavailable.")
            }
        }
    }
}
