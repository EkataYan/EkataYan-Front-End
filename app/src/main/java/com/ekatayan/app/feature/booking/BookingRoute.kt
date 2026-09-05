package com.ekatayan.app.feature.booking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun BookingRoute(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    BookingScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDestinationSelected = viewModel::onDestinationSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onClearDestination = viewModel::clearDestination,
        onClearSearch = viewModel::clearSearch,
        onResetFilters = viewModel::resetFilters,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
    )
}
