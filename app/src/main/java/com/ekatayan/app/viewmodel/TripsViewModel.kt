package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.data.repository.TripsRepository
import com.ekatayan.app.data.repository.SavedAiTripDetails
import com.ekatayan.app.data.repository.TripDetailsException
import com.ekatayan.app.data.repository.TripDetailsFailure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider
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
import com.ekatayan.app.utils.runSuspendCatching

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repository: TripsRepository,
    private val strings: StringResourceProvider,
) : ViewModel() {
    private val today = LocalDate.now()
    private val _uiState = MutableStateFlow(TripsUiState(YearMonth.from(today), today, repository.trips.value, isLoading = true))
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        refreshTrips()
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
                    state.copy(
                        trips = trips.sortedBy { it.startDate },
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

    fun showToday() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                displayedMonth = YearMonth.from(today),
                today = today,
                selectedDate = today,
            )
        }
    }

    fun guideFor(destination: String) = repository.guideFor(destination)

    fun deleteTrip(tripId: Int) {
        if (tripId in _uiState.value.deletingTripIds) return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingTripIds = it.deletingTripIds + tripId, errorMessage = null) }
            runSuspendCatching { repository.deleteTrip(tripId) }
                .onFailure { failure ->
                    _uiState.update { it.copy(errorMessage = strings[R.string.trip_error_delete]) }
                }
            _uiState.update { it.copy(deletingTripIds = it.deletingTripIds - tripId) }
        }
    }

    fun refreshTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runSuspendCatching { repository.refreshTrips() }
                .onSuccess { _uiState.update { it.copy(isLoading = false, errorMessage = null) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = strings[R.string.trips_error_load]) }
                }
        }
    }

    fun loadTripDetails(trip: Trip) {
        if (trip.source != "ai" || trip.remoteId == null) return
        _uiState.update { it.copy(detailsLoading = true, detailsError = null) }
        viewModelScope.launch {
            runSuspendCatching { repository.loadAiTripDetails(trip) }
                .onSuccess { details -> _uiState.update { it.copy(detailsLoading = false, aiDetails = details, detailsFailure = null) } }
                .onFailure { error -> _uiState.update { it.copy(detailsLoading = false, detailsError = strings[R.string.trip_error_load], detailsFailure = (error as? TripDetailsException)?.failure ?: TripDetailsFailure.SERVER) } }
        }
    }
}

data class TripsUiState(
    val displayedMonth: YearMonth,
    val today: LocalDate,
    val trips: List<Trip>,
    val selectedDate: LocalDate? = today,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val detailsLoading: Boolean = false,
    val detailsError: String? = null,
    val detailsFailure: TripDetailsFailure? = null,
    val aiDetails: SavedAiTripDetails? = null,
    val deletingTripIds: Set<Int> = emptySet(),
)

