package com.ekatayan.app.data.repository

import com.ekatayan.app.R
import com.ekatayan.app.data.local.destinationGuideFor
import com.ekatayan.app.data.local.database.TripsDao
import com.ekatayan.app.data.local.database.toEntity
import com.ekatayan.app.data.local.database.toModel
import com.ekatayan.app.data.model.DestinationGuide
import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.data.model.Itinerary
import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.remote.api.PlannedItinerarySaveDto
import com.ekatayan.app.data.remote.api.TripDto
import com.ekatayan.app.data.remote.api.TripRequest
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import android.util.Log
import com.google.gson.JsonParseException
import java.io.IOException
import retrofit2.HttpException
import com.ekatayan.app.data.remote.apiCall

data class SavedAiTripDetails(val itinerary: Itinerary, val planner: com.ekatayan.app.data.remote.api.PlannerPreviewRequest?)

enum class TripDetailsFailure { AUTHENTICATION, FORBIDDEN, NOT_FOUND, NETWORK, SERVER, INVALID_RESPONSE }
class TripDetailsException(val failure: TripDetailsFailure, message: String, cause: Throwable? = null) : Exception(message, cause)

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class TripsRepository private constructor(private val dao: TripsDao?, private val api: EkataYanApiService?, testOnly: Unit?) {
    @Inject constructor(dao: TripsDao, api: EkataYanApiService) : this(dao, api, null)
    constructor() : this(null, null, Unit)
    internal constructor(api: EkataYanApiService) : this(null, api, Unit)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val mutableTrips = MutableStateFlow<List<Trip>>(emptyList())
    val trips = mutableTrips.asStateFlow()
    private val mutationMutex = Mutex()

    init {
        if (dao != null) scope.launch {
            dao.initialize(emptyList())
            dao.observeAll().collect { values -> mutableTrips.value = values.map { it.toModel() } }
        }
    }

    fun guideFor(destination: String): DestinationGuide = destinationGuideFor(destination)

    suspend fun createManualTrip(name: String, destination: String, startDate: LocalDate, endDate: LocalDate, budget: String, notes: String) = mutationMutex.withLock {
        val response = apiCall("The trip could not be saved.") { requireNotNull(api).createTrip(TripRequest(name, listOf(destination), startDate.toString(), endDate.toString(), budget.ifBlank { "0" }, additionalRequirements = notes)) }
        val trip = response.data.takeIf { response.success }
            ?: throw IllegalStateException(response.error?.message ?: "The trip could not be saved.")
        storeRemoteTrip(trip)
    }

    suspend fun refreshTrips() = mutationMutex.withLock {
        val response = apiCall("Trips couldn't be loaded.") { requireNotNull(api).trips() }
        val remote = response.data.takeIf { response.success }
            ?: throw IllegalStateException(response.error?.message ?: "Trips couldn't be loaded.")
        // The authenticated backend is authoritative. Never mix account-independent
        // local/Figma rows into another user's trip collection.
        val updated = remote.map(::remoteTrip)
        replaceTrips(updated)
    }

    suspend fun addAiTrip(saved: PlannedItinerarySaveDto) = mutationMutex.withLock {
        val remote = saved.trip
        val itinerary = with(ItineraryRepository(api ?: error("API unavailable"))) { saved.structuredItinerary.toDomain() }
        val primaryDestination = itinerary.trip.route.firstOrNull().orEmpty()
        val trip = Trip(stableRemoteId(remote.id), 0, 0, R.string.trip_status_upcoming, LocalDate.parse(remote.startDate), LocalDate.parse(remote.endDate),
            destinationImage(primaryDestination), remote.name, itinerary.trip.route.joinToString(" → "), null, null, null,
            remote.id, "ai", itinerary.trip.summary, itinerary.trip.route, itinerary.trip.travellerType,
            itinerary.trip.travellerCount, itinerary.trip.travelStyle, itinerary.trip.travelPace)
        val updated = synchronized(this) { (mutableTrips.value.filterNot { it.remoteId == remote.id } + trip).also { mutableTrips.value = it } }
        replaceTrips(updated)
    }

    suspend fun loadAiTripDetails(trip: Trip): SavedAiTripDetails {
        val tripId = trip.remoteId ?: throw TripDetailsException(TripDetailsFailure.NOT_FOUND, "Trip details not found.")
        Log.d(TRIP_DETAILS_LOG, "TripDetails: tripId=$tripId endpoint=/api/trips/$tripId/details")
        try {
            val response = requireNotNull(api).tripDetails(tripId)
            val details = response.data
            if (!response.success || details == null) {
                val message = response.error?.message ?: "The server returned an invalid trip response."
                Log.e(TRIP_DETAILS_LOG, "TripDetails API: applicationError=${response.error?.code} message=$message")
                throw TripDetailsException(TripDetailsFailure.INVALID_RESPONSE, message)
            }
            val structured = details.structuredItinerary
                ?: throw TripDetailsException(TripDetailsFailure.INVALID_RESPONSE, "The saved itinerary is unavailable.")
            Log.d(TRIP_DETAILS_LOG, "TripDetails API: status=200 tripId=${details.trip.id}")
            return SavedAiTripDetails(with(ItineraryRepository(api)) { structured.toDomain() }, details.trip.plannerContext)
        } catch (error: TripDetailsException) {
            throw error
        } catch (error: HttpException) {
            val body = runCatching { error.response()?.errorBody()?.string() }.getOrNull()?.take(1000)
            Log.e(TRIP_DETAILS_LOG, "TripDetails API: status=${error.code()} body=$body", error)
            val failure = when (error.code()) {
                401 -> TripDetailsFailure.AUTHENTICATION
                403 -> TripDetailsFailure.FORBIDDEN
                404 -> TripDetailsFailure.NOT_FOUND
                else -> TripDetailsFailure.SERVER
            }
            throw TripDetailsException(failure, tripDetailsMessage(failure), error)
        } catch (error: JsonParseException) {
            Log.e(TRIP_DETAILS_LOG, "TripDetails API: response parsing failed", error)
            throw TripDetailsException(TripDetailsFailure.INVALID_RESPONSE, tripDetailsMessage(TripDetailsFailure.INVALID_RESPONSE), error)
        } catch (error: IOException) {
            Log.e(TRIP_DETAILS_LOG, "TripDetails API: network failure ${error.message}", error)
            throw TripDetailsException(TripDetailsFailure.NETWORK, tripDetailsMessage(TripDetailsFailure.NETWORK), error)
        }
    }

    private suspend fun storeRemoteTrip(value: TripDto) {
        val trip = remoteTrip(value)
        val updated = synchronized(this) {
            (mutableTrips.value.filterNot {
                it.remoteId == value.id || (it.remoteId == null && it.customName == value.name &&
                    it.startDate.toString() == value.startDate && it.endDate.toString() == value.endDate)
            } + trip).also { mutableTrips.value = it }
        }
        replaceTrips(updated)
    }

    private fun remoteTrip(value: TripDto): Trip {
        val route = value.destinations
        return Trip(stableRemoteId(value.id), 0, 0, R.string.trip_status_upcoming, LocalDate.parse(value.startDate), LocalDate.parse(value.endDate),
            destinationImage(route.firstOrNull().orEmpty()), value.name, route.joinToString(" → "), value.budget.takeUnless { it == "0" },
            value.additionalRequirements.takeIf(String::isNotBlank), null, value.id, value.source,
            route = route, travellerCount = value.travelers, travelStyle = value.travelStyle)
    }

    suspend fun deleteTrip(tripId: Int) = mutationMutex.withLock {
        val trip = mutableTrips.value.firstOrNull { it.id == tripId }
            ?: throw IllegalStateException("Trip not found.")
        val remoteId = trip.remoteId ?: error("This trip is not synchronized. Refresh your trips and try again.")
        val response = apiCall("The trip could not be deleted.") { requireNotNull(api).deleteTrip(remoteId) }
        if (!response.success || response.data?.get("deleted") != true) {
            throw IllegalStateException(response.error?.message ?: "The trip could not be deleted.")
        }
        replaceTrips(mutableTrips.value.filterNot { it.id == tripId })
    }

    private suspend fun replaceTrips(values: List<Trip>) {
        dao?.replace(values.map(Trip::toEntity))
        mutableTrips.value = values
    }

    private fun stableRemoteId(remoteId: String): Int =
        (runCatching { UUID.fromString(remoteId).hashCode() }.getOrElse { remoteId.hashCode() } and Int.MAX_VALUE)
            .takeUnless { it == 0 } ?: 1

}

private const val TRIP_DETAILS_LOG = "EkataYanTripDetails"
fun tripDetailsMessage(failure: TripDetailsFailure): String = when (failure) {
    TripDetailsFailure.AUTHENTICATION -> "Your session has expired. Please sign in again."
    TripDetailsFailure.FORBIDDEN -> "You don't have access to this trip."
    TripDetailsFailure.NOT_FOUND -> "Trip details not found."
    TripDetailsFailure.NETWORK -> "Unable to connect. Check your connection and try again."
    TripDetailsFailure.SERVER -> "The server couldn't load this trip. Please try again."
    TripDetailsFailure.INVALID_RESPONSE -> "The trip response couldn't be read. Please try again."
}

private fun destinationImage(destination: String): Int = when {
    destination.contains("kandy", true) -> R.drawable.kandy
    destination.contains("galle", true) || destination.contains("mirissa", true) -> R.drawable.galle
    destination.contains("colombo", true) -> R.drawable.colombo
    destination.contains("sigiriya", true) -> R.drawable.sigiriya
    destination.contains("ella", true) || destination.contains("nuwara", true) -> R.drawable.nine_arch_bridge
    else -> R.drawable.home_header
}
