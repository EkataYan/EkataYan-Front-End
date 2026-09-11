package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.*
import com.ekatayan.app.data.remote.api.*
import java.time.LocalDate
import javax.inject.Inject

data class ItineraryPlanInput(val destination: String, val startDate: LocalDate, val endDate: LocalDate, val budget: String, val travelers: Int, val accommodation: String, val transport: String, val travelStyle: String, val interests: List<String>)

class ItineraryRepository @Inject constructor(private val api: EkataYanApiService) {
    private var pendingInput: ItineraryPlanInput? = null
    private var pendingTrip: TripDto? = null

    suspend fun generate(input: ItineraryPlanInput): Itinerary {
        val reusableTrip = pendingTrip?.takeIf { pendingInput == input }
        val trip = reusableTrip ?: run {
                val tripBody = TripRequest("${input.destination} itinerary", listOf(input.destination), input.startDate.toString(), input.endDate.toString(), input.budget, travelers=input.travelers, interests=input.interests, travelStyle=input.travelStyle, accommodationPreference=input.accommodation, transportationPreference=input.transport)
                val tripEnvelope = api.createTrip(tripBody)
                tripEnvelope.data?.also { created ->
                    pendingInput = input
                    pendingTrip = created
                } ?: throw IllegalStateException(tripEnvelope.error?.message ?: "Trip could not be created.")
            }
        val request = GenerateItineraryRequest(trip.id, trip.name, trip.destinations, trip.startDate, trip.endDate, trip.budget, trip.currency, trip.travelers, trip.interests, trip.preferredActivities, trip.travelStyle, trip.accommodationPreference, trip.transportationPreference, trip.additionalRequirements)
        val response = api.generateItinerary(request)
        return response.data?.toDomain()?.also {
            pendingInput = null
            pendingTrip = null
        } ?: throw IllegalStateException(response.error?.message ?: "Itinerary generation failed.")
    }

    private fun ItineraryDto.toDomain() = Itinerary(id, tripId, overview, currency, days.map { day ->
        ItineraryDay(day.dayNumber, day.date, day.locations, day.activities.map { ItineraryActivity(it.suggestedTime, it.title, it.location, it.description, it.estimatedCost, it.transport) }, day.notes)
    }, recommendations)
}
