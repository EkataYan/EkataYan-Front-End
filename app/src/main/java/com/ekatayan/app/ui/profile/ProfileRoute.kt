package com.ekatayan.app.ui.profile

import com.ekatayan.app.viewmodel.ProfileViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileRoute(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profileState by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = profileState,
        onRefreshErrorShown = viewModel::onRefreshErrorShown,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onWishlistClick = onWishlistClick,
        onGroupsClick = onGroupsClick,
        onEditProfileClick = onEditProfileClick,
        onSettingsClick = onSettingsClick,
    )
}
