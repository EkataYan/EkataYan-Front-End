package com.ekatayan.app.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class TripsViewModel @Inject constructor(private val repository: TripsRepository) : ViewModel() {
    private val today = LocalDate.now()
    private val _uiState = MutableStateFlow(createTripsUiState(today).copy(trips = repository.trips.value))
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                val today = LocalDate.now()
                _uiState.update { it.copy(today = today, trips = repository.trips.value) }
                val nextDay = today.plusDays(1).atStartOfDay()
                delay(Duration.between(java.time.LocalDateTime.now(), nextDay).toMillis().coerceAtLeast(1L))
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.trips.collect { trips ->
                _uiState.update { state ->
                    val newTrip = trips.firstOrNull { trip -> state.trips.none { it.id == trip.id } }
                    state.copy(
                        trips = trips.sortedBy { it.startDate },
                        displayedMonth = newTrip?.let { YearMonth.from(it.startDate) } ?: state.displayedMonth,
                        selectedDate = newTrip?.startDate ?: state.selectedDate,
                    )
                }
            }
        }
    }

    fun showPreviousMonth() {
        _uiState.update { it.copy(displayedMonth = it.displayedMonth.minusMonths(1)) }
    }

    fun showNextMonth() {
        _uiState.update { it.copy(displayedMonth = it.displayedMonth.plusMonths(1)) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun deleteTrip(tripId: Int) {
        repository.deleteTrip(tripId)
    }
}

data class TripsUiState(
    val displayedMonth: YearMonth,
    val today: LocalDate,
    val trips: List<Trip>,
    val selectedDate: LocalDate? = today,
)

data class Trip(
    val id: Int,
    val nameRes: Int,
    val locationRes: Int,
    val statusRes: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val imageRes: Int,
    val customName: String? = null,
    val customLocation: String? = null,
    val budget: String? = null,
    val notes: String? = null,
)

@Singleton
class TripsRepository @Inject constructor() {
    private val initialTrips = createTripsUiState(LocalDate.now()).trips
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

private fun createTripsUiState(today: LocalDate) = TripsUiState(
    displayedMonth = YearMonth.from(today),
    today = today,
    trips = listOf(
        Trip(1, R.string.trip_galle_name, R.string.trip_galle_location, R.string.trip_status_upcoming, today.plusDays(3), today.plusDays(5), R.drawable.galle),
        Trip(2, R.string.trip_kandy_name, R.string.trip_kandy_location, R.string.trip_status_planned, today.plusDays(12), today.plusDays(15), R.drawable.kandy),
        Trip(3, R.string.trip_sigiriya_name, R.string.trip_sigiriya_location, R.string.trip_status_planned, today.plusDays(25), today.plusDays(27), R.drawable.sigiriya),
        Trip(4, R.string.trip_nuwara_eliya_name, R.string.trip_nuwara_eliya_location, R.string.trip_status_planned, today.plusDays(40), today.plusDays(43), R.drawable.nine_arch_bridge),
    ),
)

fun Trip.statusFor(today: LocalDate): Int = when {
    today.isBefore(startDate) -> R.string.trip_status_upcoming
    today.isAfter(endDate) -> R.string.trip_status_past
    else -> R.string.trip_status_ongoing
}

internal fun calendarMonthGrid(month: YearMonth): List<LocalDate?> {
    val leadingEmptyDays = month.atDay(1).dayOfWeek.value - 1
    return List(42) { index ->
        val day = index - leadingEmptyDays + 1
        if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
    }
}
