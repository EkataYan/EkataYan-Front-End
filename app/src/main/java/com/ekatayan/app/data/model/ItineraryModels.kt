package com.ekatayan.app.data.model

data class ItineraryLocation(val name: String, val latitude: Double? = null, val longitude: Double? = null)
data class ItineraryActivity(
    val id: String,
    val name: String,
    val category: String,
    val location: ItineraryLocation,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val description: String,
    val estimatedCostLkr: Long,
    val transportFromPrevious: String,
    val travelTimeMinutes: Int,
)
data class CostRange(val min: Long, val max: Long)
data class ItineraryDay(
    val dayNumber: Int,
    val date: String,
    val destination: String,
    val title: String,
    val summary: String,
    val activities: List<ItineraryActivity>,
    val estimatedCostLkr: CostRange,
)
data class TripSummary(
    val title: String,
    val summary: String,
    val route: List<String>,
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val travellerType: String,
    val travellerCount: Int,
    val travelStyle: String,
    val travelPace: String,
)
data class CostEstimate(
    val currency: String,
    val accommodation: CostRange,
    val transport: CostRange,
    val food: CostRange,
    val activities: CostRange,
    val total: CostRange,
    val disclaimer: String,
)
data class Itinerary(
    val trip: TripSummary,
    val days: List<ItineraryDay>,
    val costEstimate: CostEstimate,
    val recommendations: List<String>,
)
