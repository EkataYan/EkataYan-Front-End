package com.ekatayan.app.data.model

data class ItineraryActivity(val time: String, val title: String, val location: String, val description: String, val estimatedCost: String, val transport: String)
data class ItineraryDay(val day: Int, val date: String, val locations: List<String>, val activities: List<ItineraryActivity>, val notes: String)
data class Itinerary(val id: String, val tripId: String, val summary: String, val currency: String, val days: List<ItineraryDay>, val travelTips: List<String>)
