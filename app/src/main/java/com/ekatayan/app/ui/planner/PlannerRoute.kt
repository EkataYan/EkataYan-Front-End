package com.ekatayan.app.ui.planner

import com.ekatayan.app.viewmodel.PlannerUiState
import com.ekatayan.app.viewmodel.PlannerViewModel
import com.ekatayan.app.viewmodel.PlannerGenerationViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlannerRoute(onHomeClick: () -> Unit, onTripsClick: () -> Unit, onExpensesClick: () -> Unit, onProfileClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, notificationsUiState: StateFlow<NotificationsUiState>, viewModel: PlannerViewModel = hiltViewModel(), generationViewModel: PlannerGenerationViewModel = hiltViewModel()) {
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState
    val generationState by generationViewModel.state.collectAsStateWithLifecycle()
    PlannerScreen(
        uiState = uiState,
        onDestinationChange = viewModel::updateDestination,
        onAddDestination = viewModel::addDestination,
        onAdditionalDestinationChange = viewModel::updateAdditionalDestination,
        onRemoveAdditionalDestination = viewModel::removeAdditionalDestination,
        onMoveDestination = viewModel::moveDestination,
        onTravellerTypeSelected = viewModel::updateTravellerType,
        onPeopleCountChange = viewModel::updateCustomPeopleCount,
        onStartDateSelected = viewModel::updateStartDate,
        onEndDateSelected = viewModel::updateEndDate,
        onDateValidationError = viewModel::setError,
        onToggleAiDestinations = viewModel::toggleAiDestinations,
        onToggleSuggestedPlaces = viewModel::toggleSuggestedPlaces,
        onTogglePersonalization = viewModel::togglePersonalization,
        onToggleTransport = viewModel::toggleTransport,
        onAccommodationSelected = viewModel::updateAccommodation,
        onTravelStyleSelected = viewModel::updateTravelStyle,
        onToggleInterest = viewModel::toggleInterest,
        onPaceSelected = viewModel::updatePace,
        onSpecialRequestsChange = viewModel::updateSpecialRequests,
        generationState = generationState,
        onAskAiClick = { if (viewModel.validate()) generationViewModel.generate(viewModel.uiState.value) },
        onRetry = generationViewModel::retry,
        onEditPreferences = generationViewModel::editPreferences,
        onRemoveActivity = generationViewModel::removeActivity,
        onMoveActivity = generationViewModel::moveActivity,
        onMakeDayRelaxed = generationViewModel::makeDayRelaxed,
        onSaveTrip = generationViewModel::save,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onNotificationClick = onNotificationClick,
        onSettingsClick = onSettingsClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}
