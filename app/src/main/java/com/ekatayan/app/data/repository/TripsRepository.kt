package com.ekatayan.app.data.repository

import com.ekatayan.app.R
import com.ekatayan.app.data.local.destinationGuideFor
import com.ekatayan.app.data.local.initialTrips
import com.ekatayan.app.data.local.database.TripsDao
import com.ekatayan.app.data.local.database.toEntity
import com.ekatayan.app.data.local.database.toModel
import com.ekatayan.app.data.model.DestinationGuide
import com.ekatayan.app.data.model.Trip
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class TripsRepository private constructor(private val dao: TripsDao?, testMode: Boolean) {
    @Inject constructor(dao: TripsDao) : this(dao, false)
    constructor() : this(null, true)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val seedTrips = initialTrips(LocalDate.now())
    private val mutableTrips = MutableStateFlow(if (testMode) seedTrips else emptyList())
    val trips = mutableTrips.asStateFlow()

    init {
        if (dao != null) scope.launch {
            dao.initialize(seedTrips.map(Trip::toEntity))
            dao.observeAll().collect { values -> mutableTrips.value = values.map { it.toModel() } }
        }
    }

    fun addTrip(name: String, destination: String, startDate: LocalDate, endDate: LocalDate, budget: String, notes: String) {
        val updated = synchronized(this) {
            var nextId: Int
            do nextId = UUID.randomUUID().hashCode() and Int.MAX_VALUE
            while (nextId == 0 || mutableTrips.value.any { it.id == nextId })
            (mutableTrips.value + Trip(nextId, 0, 0, R.string.trip_status_upcoming, startDate, endDate,
                destinationImage(destination), name.trim(), destination.trim(), budget.trim().ifBlank { null }, notes.trim().ifBlank { null }))
                .also { mutableTrips.value = it }
        }
        persist(updated)
    }

    fun guideFor(destination: String): DestinationGuide = destinationGuideFor(destination)

    fun deleteTrip(tripId: Int) {
        val updated = mutableTrips.value.filterNot { it.id == tripId }
        mutableTrips.value = updated
        persist(updated)
    }

    private fun persist(values: List<Trip>) {
        dao?.let { storage -> scope.launch { runCatching { storage.replace(values.map(Trip::toEntity)) } } }
    }
}

private fun destinationImage(destination: String): Int = when {
    destination.contains("kandy", true) -> R.drawable.kandy
    destination.contains("galle", true) || destination.contains("mirissa", true) -> R.drawable.galle
    destination.contains("colombo", true) -> R.drawable.colombo
    destination.contains("sigiriya", true) -> R.drawable.sigiriya
    destination.contains("ella", true) || destination.contains("nuwara", true) -> R.drawable.nine_arch_bridge
    else -> R.drawable.home_header
}
