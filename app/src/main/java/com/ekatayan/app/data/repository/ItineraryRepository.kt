package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.*
import com.ekatayan.app.data.remote.api.*
import java.time.LocalDate
import javax.inject.Inject
import com.ekatayan.app.data.remote.apiCall

data class ItineraryPlanInput(
    val destinations: List<String>, val travellerType: String, val startDate: LocalDate,
    val endDate: LocalDate, val travelers: Int, val accommodation: String?,
    val transports: List<String>, val travelStyle: String?, val interests: List<String>,
    val pace: String?, val specialRequests: String?, val letAiChooseDestinations: Boolean = false,
    val suggestAdditionalPlaces: Boolean = false,
)

class ItineraryRepository @Inject constructor(private val api: EkataYanApiService, private val settings: SettingsRepository = SettingsRepository()) {
    suspend fun preview(input: ItineraryPlanInput): Itinerary {
        val response = apiCall("We couldn't finish your itinerary.") { api.previewItinerary(input.toRequest()) }
        return response.data?.toDomain()
            ?: throw IllegalStateException(response.error?.message ?: "We couldn't finish your itinerary.")
    }

    suspend fun modify(input: ItineraryPlanInput, itinerary: Itinerary, instruction: String, targetDay: Int? = null): Itinerary {
        val response = apiCall("We couldn't update your itinerary.") { api.modifyItinerary(ModifyItineraryRequest(input.toRequest(), itinerary.toDto(), instruction, targetDay)) }
        return response.data?.toDomain()
            ?: throw IllegalStateException(response.error?.message ?: "We couldn't update your itinerary.")
    }

    suspend fun save(input: ItineraryPlanInput, itinerary: Itinerary): PlannedItinerarySaveDto {
        val response = apiCall("The trip could not be saved.") { api.savePlannedItinerary(PlannedItinerarySaveRequest(input.toRequest(), itinerary.toDto())) }
        return response.data
            ?: throw IllegalStateException(response.error?.message ?: "The trip could not be saved.")
    }

    private fun ItineraryPlanInput.toRequest() = PlannerPreviewRequest(
        destinations = destinations.map(::PlannerDestinationDto), travellerType = travellerType,
        travellerCount = travelers, startDate = startDate.toString(), endDate = endDate.toString(),
        transportPreferences = transports, accommodationPreference = accommodation,
        travelStyle = travelStyle, interests = interests, travelPace = pace,
        specialRequests = specialRequests?.takeIf(String::isNotBlank),
        allowAiDestinationSuggestions = letAiChooseDestinations,
        suggestAdditionalPlaces = suggestAdditionalPlaces,
        preferredLanguage = settings.preferences.value.selectedLanguage,
    )

    fun AiItineraryResponseDto.toDomain() = Itinerary(
        trip = TripSummary(trip.title, trip.summary, trip.route, trip.startDate, trip.endDate,
            trip.durationDays, trip.travellerType, trip.travellerCount, trip.travelStyle, trip.travelPace),
        days = days.map { day -> ItineraryDay(day.dayNumber, day.date, day.destination, day.title, day.summary,
            day.activities.map { activity -> ItineraryActivity(activity.id, activity.name, activity.category,
                ItineraryLocation(activity.location.name, activity.location.latitude, activity.location.longitude),
                activity.startTime, activity.endTime, activity.durationMinutes, activity.description,
                activity.estimatedCostLkr, activity.transportFromPrevious, activity.travelTimeMinutes) },
            CostRange(day.dayEstimatedCostLkr.min, day.dayEstimatedCostLkr.max)) },
        costEstimate = CostEstimate(costEstimate.currency, CostRange(costEstimate.accommodation.min, costEstimate.accommodation.max),
            CostRange(costEstimate.transport.min, costEstimate.transport.max), CostRange(costEstimate.food.min, costEstimate.food.max),
            CostRange(costEstimate.activities.min, costEstimate.activities.max), CostRange(costEstimate.total.min, costEstimate.total.max),
            costEstimate.disclaimer),
        recommendations = recommendations,
    )

    private fun Itinerary.toDto() = AiItineraryResponseDto(
        trip = TripSummaryDto(trip.title, trip.summary, trip.route, trip.startDate, trip.endDate,
            trip.durationDays, trip.travellerType, trip.travellerCount, trip.travelStyle, trip.travelPace),
        days = days.map { day -> AiItineraryDayDto(day.dayNumber, day.date, day.destination, day.title, day.summary,
            day.activities.map { activity -> AiItineraryActivityDto(activity.id, activity.name, activity.category,
                ItineraryLocationDto(activity.location.name, activity.location.latitude, activity.location.longitude),
                activity.startTime, activity.endTime, activity.durationMinutes, activity.description,
                activity.estimatedCostLkr, activity.transportFromPrevious, activity.travelTimeMinutes) },
            CostRangeDto(day.estimatedCostLkr.min, day.estimatedCostLkr.max)) },
        costEstimate = CostEstimateDto(costEstimate.currency, CostRangeDto(costEstimate.accommodation.min, costEstimate.accommodation.max),
            CostRangeDto(costEstimate.transport.min, costEstimate.transport.max), CostRangeDto(costEstimate.food.min, costEstimate.food.max),
            CostRangeDto(costEstimate.activities.min, costEstimate.activities.max), CostRangeDto(costEstimate.total.min, costEstimate.total.max),
            costEstimate.disclaimer),
        recommendations = recommendations,
    )
}
