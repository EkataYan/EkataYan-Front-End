package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.DestinationGuide
import com.ekatayan.app.data.model.Trip

import com.ekatayan.app.data.local.initialTrips
import com.ekatayan.app.data.local.destinationGuideFor
import com.ekatayan.app.R
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.*

@Singleton
class TripsRepository @Inject constructor() {
    private val initialTrips = initialTrips(LocalDate.now())
    private val _trips = MutableStateFlow(initialTrips)
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    fun addTrip(name: String, destination: String, startDate: LocalDate, endDate: LocalDate, budget: String, notes: String) {
        val nextId = (_trips.value.maxOfOrNull { it.id } ?: 0) + 1
        _trips.update {
            it + Trip(
                nextId, 0, 0, R.string.trip_status_upcoming, startDate, endDate,
                destinationImage(destination), name.trim(), destination.trim(),
                budget.trim().ifBlank { null }, notes.trim().ifBlank { null },
            )
        }
    }

    fun guideFor(destination: String): DestinationGuide = destinationGuideFor(destination)

    fun deleteTrip(tripId: Int) {
        _trips.update { trips -> trips.filterNot { it.id == tripId } }
    }
}

private fun destinationImage(destination: String): Int = when {
    destination.contains("kandy", ignoreCase = true) -> R.drawable.kandy
    destination.contains("galle", ignoreCase = true) || destination.contains("mirissa", ignoreCase = true) -> R.drawable.galle
    destination.contains("colombo", ignoreCase = true) -> R.drawable.colombo
    destination.contains("sigiriya", ignoreCase = true) -> R.drawable.sigiriya
    destination.contains("ella", ignoreCase = true) || destination.contains("nuwara", ignoreCase = true) -> R.drawable.nine_arch_bridge
    else -> R.drawable.home_header
}

