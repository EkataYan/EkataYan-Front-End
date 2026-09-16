package com.ekatayan.app.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

enum class TravellerType(val fixedPartySize: Int?) {
    SOLO(1),
    COUPLE(2),
    FAMILY(null),
    FRIENDS(null),
    GROUP(null),
}

enum class PlannerTravelStyle { BUDGET, COMFORT, PREMIUM, AI_DECIDES }
enum class TravelPace { RELAXED, BALANCED, PACKED }

data class PlannerUiState(
    val destination: String = "",
    val additionalDestinations: List<String> = emptyList(),
    val travellerType: TravellerType? = null,
    val customPeopleCount: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val letAiChooseDestinations: Boolean = false,
    val suggestAdditionalPlaces: Boolean = false,
    val personalizationExpanded: Boolean = false,
    val transportPreferences: Set<String> = emptySet(),
    val accommodationPreference: String = "Let AI decide",
    val travelStyle: PlannerTravelStyle = PlannerTravelStyle.AI_DECIDES,
    val interests: Set<String> = emptySet(),
    val pace: TravelPace = TravelPace.BALANCED,
    val specialRequests: String = "",
    val error: String? = null,
) {
    val requiresCustomPeopleCount: Boolean
        get() = travellerType in setOf(TravellerType.FAMILY, TravellerType.FRIENDS, TravellerType.GROUP)

    val minimumPartySize: Int
        get() = when (travellerType) {
            TravellerType.FAMILY, TravellerType.FRIENDS -> 2
            TravellerType.GROUP -> 3
            else -> travellerType?.fixedPartySize ?: 1
        }

    val partySize: Int?
        get() = when (travellerType) {
            TravellerType.SOLO, TravellerType.COUPLE, TravellerType.FAMILY,
            TravellerType.FRIENDS, TravellerType.GROUP -> customPeopleCount.toIntOrNull()
                ?.takeIf { it >= minimumPartySize } ?: travellerType.fixedPartySize
            null -> null
        }

    val destinations: List<String>
        get() = (listOf(destination) + additionalDestinations).filterNot(String::isBlank)
}

@HiltViewModel
class PlannerViewModel @Inject constructor(private val strings: StringResourceProvider) : ViewModel() {
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

    fun moveDestination(index: Int, direction: Int) {
        val route = uiState.value.destinations.toMutableList()
        val target = index + direction
        if (index !in route.indices || target !in route.indices) return
        val value = route.removeAt(index)
        route.add(target, value)
        mutableUiState.value = uiState.value.copy(
            destination = route.firstOrNull().orEmpty(),
            additionalDestinations = route.drop(1),
        )
    }

    fun updateTravellerType(value: TravellerType) {
        val startingCount = when (value) {
            TravellerType.SOLO -> "1"
            TravellerType.COUPLE -> "2"
            TravellerType.FAMILY, TravellerType.FRIENDS -> "4"
            TravellerType.GROUP -> "6"
        }
        mutableUiState.value = uiState.value.copy(
            travellerType = value,
            customPeopleCount = startingCount,
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
                strings[R.string.planner_end_date_cleared]
            } else {
                null
            },
        )
    }

    fun updateEndDate(value: LocalDate) {
        if (uiState.value.startDate?.let(value::isBefore) == true) {
            setError(strings[R.string.planner_end_before_start])
            return
        }
        mutableUiState.value = uiState.value.copy(endDate = value, error = null)
    }

    fun setError(value: String) {
        mutableUiState.value = uiState.value.copy(error = value)
    }

    fun togglePersonalization() { mutableUiState.value = uiState.value.copy(personalizationExpanded = !uiState.value.personalizationExpanded) }
    fun toggleAiDestinations() { mutableUiState.value = uiState.value.copy(letAiChooseDestinations = !uiState.value.letAiChooseDestinations, error = null) }
    fun toggleSuggestedPlaces() { mutableUiState.value = uiState.value.copy(suggestAdditionalPlaces = !uiState.value.suggestAdditionalPlaces) }
    fun toggleTransport(value: String) { mutableUiState.value = uiState.value.copy(transportPreferences = uiState.value.transportPreferences.toggle(value)) }
    fun updateAccommodation(value: String) { mutableUiState.value = uiState.value.copy(accommodationPreference = value) }
    fun updateTravelStyle(value: PlannerTravelStyle) { mutableUiState.value = uiState.value.copy(travelStyle = value) }
    fun toggleInterest(value: String) { mutableUiState.value = uiState.value.copy(interests = uiState.value.interests.toggle(value)) }
    fun updatePace(value: TravelPace) { mutableUiState.value = uiState.value.copy(pace = value) }
    fun updateSpecialRequests(value: String) { mutableUiState.value = uiState.value.copy(specialRequests = value.take(2000)) }

    fun validate(): Boolean {
        val state = uiState.value
        val validAdditionalDestinations = state.additionalDestinations.filterNot(String::isBlank)
        val error = when {
            state.destinations.isEmpty() && !state.letAiChooseDestinations -> strings[R.string.planner_validation_destination]
            state.travellerType == null -> strings[R.string.planner_validation_traveller]
            state.requiresCustomPeopleCount && state.partySize == null ->
                strings[R.string.planner_validation_party_size, state.minimumPartySize]
            state.startDate == null -> strings[R.string.planner_validation_start_date]
            state.endDate == null -> strings[R.string.planner_validation_end_date]
            state.endDate.isBefore(state.startDate) -> strings[R.string.planner_end_before_start]
            else -> null
        }
        mutableUiState.value = state.copy(
            additionalDestinations = validAdditionalDestinations,
            error = error,
        )
        return error == null
    }

}

private fun Set<String>.toggle(value: String): Set<String> = if (value in this) this - value else this + value
