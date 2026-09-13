package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.model.Itinerary
import com.ekatayan.app.data.repository.ItineraryPlanInput
import com.ekatayan.app.data.repository.ItineraryRepository
import com.ekatayan.app.data.repository.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PlannerPhase { EDITING, GENERATING, PREVIEW, SAVING, SAVED, ERROR }

data class PlannerGenerationState(
    val phase: PlannerPhase = PlannerPhase.EDITING,
    val itinerary: Itinerary? = null,
    val input: ItineraryPlanInput? = null,
    val status: String = "Finding the best route",
    val error: String? = null,
)

@HiltViewModel
class PlannerGenerationViewModel @Inject constructor(
    private val itineraryRepository: ItineraryRepository,
    private val tripsRepository: TripsRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(PlannerGenerationState())
    val state = mutableState.asStateFlow()
    private var statusJob: Job? = null

    fun generate(planner: PlannerUiState) {
        val start = planner.startDate ?: return
        val end = planner.endDate ?: return
        val travelers = planner.partySize ?: return
        val input = ItineraryPlanInput(
            destinations = planner.destinations,
            travellerType = planner.travellerType?.displayLabel.orEmpty(),
            startDate = start,
            endDate = end,
            travelers = travelers,
            accommodation = planner.accommodationPreference,
            transports = planner.transportPreferences.toList(),
            travelStyle = planner.travelStyle.displayLabel,
            interests = planner.interests.toList(),
            pace = planner.pace.displayLabel,
            specialRequests = planner.specialRequests,
            letAiChooseDestinations = planner.letAiChooseDestinations,
            suggestAdditionalPlaces = planner.suggestAdditionalPlaces,
        )
        mutableState.value = PlannerGenerationState(PlannerPhase.GENERATING, input = input)
        rotateStatuses()
        viewModelScope.launch {
            runCatching { itineraryRepository.preview(input) }
                .onSuccess { mutableState.value = PlannerGenerationState(PlannerPhase.PREVIEW, it, input) }
                .onFailure { mutableState.value = PlannerGenerationState(PlannerPhase.ERROR, input = input, error = it.message ?: "We couldn't finish your itinerary.") }
            statusJob?.cancel()
        }
    }

    fun retry() { state.value.input?.let(::generate) }

    private fun generate(input: ItineraryPlanInput) {
        mutableState.value = PlannerGenerationState(PlannerPhase.GENERATING, input = input)
        rotateStatuses()
        viewModelScope.launch {
            runCatching { itineraryRepository.preview(input) }
                .onSuccess { mutableState.value = PlannerGenerationState(PlannerPhase.PREVIEW, it, input) }
                .onFailure { mutableState.value = PlannerGenerationState(PlannerPhase.ERROR, input = input, error = it.message ?: "We couldn't finish your itinerary.") }
            statusJob?.cancel()
        }
    }

    fun editPreferences() { mutableState.value = state.value.copy(phase = PlannerPhase.EDITING, error = null) }

    fun removeActivity(dayIndex: Int, activityIndex: Int) = updateItinerary { itinerary ->
        itinerary.copy(days = itinerary.days.mapIndexed { index, day ->
            if (index == dayIndex) day.copy(activities = day.activities.filterIndexed { position, _ -> position != activityIndex }) else day
        })
    }

    fun moveActivity(dayIndex: Int, activityIndex: Int, direction: Int) = updateItinerary { itinerary ->
        val days = itinerary.days.toMutableList()
        val activities = days.getOrNull(dayIndex)?.activities?.toMutableList() ?: return@updateItinerary itinerary
        val target = activityIndex + direction
        if (activityIndex !in activities.indices || target !in activities.indices) return@updateItinerary itinerary
        val activity = activities.removeAt(activityIndex)
        activities.add(target, activity)
        days[dayIndex] = days[dayIndex].copy(activities = activities)
        itinerary.copy(days = days)
    }

    fun makeDayRelaxed(dayIndex: Int) {
        val input = state.value.input ?: return
        val itinerary = state.value.itinerary ?: return
        mutableState.value = state.value.copy(phase = PlannerPhase.GENERATING, status = "Relaxing day ${dayIndex + 1}", error = null)
        viewModelScope.launch {
            runCatching { itineraryRepository.modify(input, itinerary, "Make day ${dayIndex + 1} more relaxed", dayIndex + 1) }
                .onSuccess { mutableState.value = state.value.copy(phase = PlannerPhase.PREVIEW, itinerary = it) }
                .onFailure { mutableState.value = state.value.copy(phase = PlannerPhase.ERROR, error = it.message ?: "We couldn't update this day.") }
        }
    }

    fun save() {
        val input = state.value.input ?: return
        val itinerary = state.value.itinerary ?: return
        mutableState.value = state.value.copy(phase = PlannerPhase.SAVING, error = null)
        viewModelScope.launch {
            runCatching { itineraryRepository.save(input, itinerary) }
                .onSuccess { saved ->
                    tripsRepository.addAiTrip(saved)
                    mutableState.value = state.value.copy(phase = PlannerPhase.SAVED)
                }
                .onFailure { mutableState.value = state.value.copy(phase = PlannerPhase.ERROR, error = it.message ?: "The trip could not be saved.") }
        }
    }

    private fun updateItinerary(transform: (Itinerary) -> Itinerary) {
        state.value.itinerary?.let { mutableState.value = state.value.copy(itinerary = transform(it)) }
    }

    private fun rotateStatuses() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            val statuses = listOf("Finding the best route", "Organizing your destinations", "Checking realistic travel times", "Balancing activities", "Matching your travel preferences", "Building your itinerary")
            var index = 0
            while (true) {
                mutableState.value = mutableState.value.copy(status = statuses[index % statuses.size])
                index++
                delay(1400)
            }
        }
    }
}

val TravellerType.displayLabel: String get() = name.lowercase().replaceFirstChar(Char::uppercase)
val PlannerTravelStyle.displayLabel: String get() = when (this) { PlannerTravelStyle.AI_DECIDES -> "Let AI decide"; else -> name.lowercase().replaceFirstChar(Char::uppercase) }
val TravelPace.displayLabel: String get() = name.lowercase().replaceFirstChar(Char::uppercase)
