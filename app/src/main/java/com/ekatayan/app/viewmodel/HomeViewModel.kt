package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import com.ekatayan.app.data.model.PopularDestination
import com.ekatayan.app.data.model.RecommendedDestination
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.repository.HomeRepository
import com.ekatayan.app.data.remote.UserSessionProvider
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User,
    val recommendedDestinations: List<RecommendedDestination>,
    val upcomingTrip: UpcomingTrip?,
    val weather: WeatherInfo?,
    val popularDestinations: List<PopularDestination>,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val session: UserSessionProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            user = homeRepository.getUser().let { user ->
                user.copy(name = session.currentUserName().orEmpty().ifBlank { user.name })
            },
            recommendedDestinations = homeRepository.getRecommendedDestinations(),
            upcomingTrip = homeRepository.getUpcomingTrip(),
            weather = homeRepository.getWeather(),
            popularDestinations = homeRepository.getPopularDestinations(),
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            session.userName.collect { name ->
                if (!name.isNullOrBlank()) _uiState.update { it.copy(user = it.user.copy(name = name)) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onSearchSubmit() = Unit
}
