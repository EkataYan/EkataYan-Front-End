package com.ekatayan.app.feature.trips

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun TripsRoute(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddTripClick: () -> Unit,
    onTripClick: (Trip) -> Unit = {},
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    TripsScreen(
        uiState = uiState,
        onPreviousMonthClick = viewModel::showPreviousMonth,
        onNextMonthClick = viewModel::showNextMonth,
        onDateClick = viewModel::selectDate,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onAddTripClick = onAddTripClick,
        onDeleteTrip = viewModel::deleteTrip,
        onTripClick = { trip ->
            viewModel.selectDate(trip.startDate)
            onTripClick(trip)
        },
    )
}
