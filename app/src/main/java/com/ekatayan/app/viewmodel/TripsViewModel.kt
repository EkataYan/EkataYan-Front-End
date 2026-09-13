package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.data.repository.TripsRepository
import com.ekatayan.app.data.repository.SavedAiTripDetails

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
    private val _uiState = MutableStateFlow(TripsUiState(YearMonth.from(today), today, repository.trips.value))
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

    fun guideFor(destination: String) = repository.guideFor(destination)

    fun deleteTrip(tripId: Int) {
        repository.deleteTrip(tripId)
    }

    fun loadTripDetails(trip: Trip) {
        if (trip.source != "ai" || trip.remoteId == null) return
        _uiState.update { it.copy(detailsLoading = true, detailsError = null, aiDetails = null) }
        viewModelScope.launch {
            runCatching { repository.loadAiTripDetails(trip) }
                .onSuccess { details -> _uiState.update { it.copy(detailsLoading = false, aiDetails = details) } }
                .onFailure { error -> _uiState.update { it.copy(detailsLoading = false, detailsError = error.message ?: "We couldn't load the itinerary.") } }
        }
    }
}

data class TripsUiState(
    val displayedMonth: YearMonth,
    val today: LocalDate,
    val trips: List<Trip>,
    val selectedDate: LocalDate? = today,
    val detailsLoading: Boolean = false,
    val detailsError: String? = null,
    val aiDetails: SavedAiTripDetails? = null,
)

