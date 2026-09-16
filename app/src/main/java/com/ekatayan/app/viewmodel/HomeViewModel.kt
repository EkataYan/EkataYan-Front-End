package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.model.WishlistItem
import com.ekatayan.app.data.repository.HomeRepository
import com.ekatayan.app.data.repository.LocationRepository
import com.ekatayan.app.data.repository.LocationPermissionDeniedException
import com.ekatayan.app.data.repository.DeviceLocationUnavailableException
import com.ekatayan.app.data.repository.SystemLocationDisabledException
import com.ekatayan.app.data.repository.TripsRepository
import com.ekatayan.app.data.repository.WeatherRepository
import com.ekatayan.app.data.remote.UserSessionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.ekatayan.app.utils.runSuspendCatching
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider

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
    val weatherErrorKind: WeatherErrorKind? = null,
    val weatherUpdatedAtMillis: Long? = null,
)

enum class WeatherErrorKind { PERMISSION, LOCATION_DISABLED, LOCATION, SERVER }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val tripsRepository: TripsRepository,
    private val session: UserSessionProvider,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    @ApplicationContext private val context: Context,
    private val strings: StringResourceProvider,
) : ViewModel() {
    private var weatherJob: kotlinx.coroutines.Job? = null

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
            session.userName.collect { name ->
                _uiState.update { it.copy(user = it.user.copy(name = name.orEmpty())) }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runSuspendCatching { tripsRepository.refreshTrips() }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, errorMessage = strings[R.string.error_trips_refresh]) } }
        }
        viewModelScope.launch {
            tripsRepository.trips.collect { trips ->
                val today = LocalDate.now()
                val next = trips
                    .asSequence()
                    .filter { !it.startDate.isBefore(today) }
                    .minByOrNull { it.startDate }
                _uiState.update { state -> state.copy(upcomingTrip = next?.let { trip ->
                    UpcomingTrip(
                        id = trip.id,
                        tripKey = trip.remoteId ?: trip.id.toString(),
                        name = trip.customName
                            ?: trip.nameRes.takeIf { it != 0 }?.let(context::getString)
                            ?: trip.customLocation.orEmpty(),
                        date = trip.startDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())),
                        duration = strings[R.string.duration_days, ChronoUnit.DAYS.between(trip.startDate, trip.endDate) + 1],
                        imageRes = trip.imageRes,
                    )
                }) }
            }
        }
        refreshWeather()
    }

    fun refreshWeather() {
        if (weatherJob?.isActive == true) return
        weatherJob = viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true, weatherError = null, weatherErrorKind = null) }
            try {
                val location = locationRepository.currentOrLastLocation()
                val weather = weatherRepository.forecast(location.latitude, location.longitude)
                _uiState.update { it.copy(weather = weather, isWeatherLoading = false, weatherError = null, weatherErrorKind = null, weatherUpdatedAtMillis = System.currentTimeMillis()) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        weather = if (error is LocationPermissionDeniedException) null else it.weather,
                        isWeatherLoading = false,
                        weatherError = when(error) {
                            is LocationPermissionDeniedException -> strings[R.string.weather_enable_location]
                            is SystemLocationDisabledException -> strings[R.string.weather_enable_device_location]
                            is DeviceLocationUnavailableException -> strings[R.string.weather_location_unavailable]
                            else -> strings[R.string.weather_temporarily_unavailable]
                        },
                        weatherErrorKind = when(error) {
                            is LocationPermissionDeniedException -> WeatherErrorKind.PERMISSION
                            is SystemLocationDisabledException -> WeatherErrorKind.LOCATION_DISABLED
                            is DeviceLocationUnavailableException -> WeatherErrorKind.LOCATION
                            else -> WeatherErrorKind.SERVER
                        },
                    )
                }
            }
        }
    }

    fun onLocationPermissionChanged() = refreshWeather()

    fun onHomeResumed() {
        val state = _uiState.value
        val needsLocationRetry = state.weather == null || state.weatherErrorKind == WeatherErrorKind.LOCATION_DISABLED || state.weatherErrorKind == WeatherErrorKind.LOCATION
        if (needsLocationRetry && locationRepository.hasForegroundPermission() && locationRepository.isSystemLocationEnabled()) {
            refreshWeather()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchSubmit() = Unit
}
