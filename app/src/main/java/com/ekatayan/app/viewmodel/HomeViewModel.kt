package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.model.WishlistItem
import com.ekatayan.app.data.repository.HomeRepository
import com.ekatayan.app.data.repository.TripsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class HomeUiState(
    val user: User,
    val recommendedDestinations: List<WishlistItem>,
    val upcomingTrip: UpcomingTrip?,
    val weather: WeatherInfo?,
    val popularDestinations: List<WishlistItem>,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isWeatherLoading: Boolean = false,
    val weatherError: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tripsRepository: TripsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            user = homeRepository.getUser().let { user ->
                user.copy(name = session.currentUserName().orEmpty().ifBlank { user.name })
            },
            recommendedDestinations = homeRepository.getRecommendedDestinations(),
            upcomingTrip = homeRepository.getUpcomingTrip(),
            weather = null,
            popularDestinations = homeRepository.getPopularDestinations(),
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tripsRepository.trips.collect { trips ->
                val next = trips.filter { !it.endDate.isBefore(java.time.LocalDate.now()) }.minByOrNull { it.startDate }
                _uiState.update { state -> state.copy(upcomingTrip = next?.let { trip ->
                    UpcomingTrip(
                        trip.id,
                        trip.customLocation ?: context.getString(trip.locationRes),
                        trip.startDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)),
                        "${ChronoUnit.DAYS.between(trip.startDate, trip.endDate) + 1} Days",
                        trip.imageRes,
                    )
                }) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchSubmit() = Unit
}
