package com.ekatayan.app.ui.home

import com.ekatayan.app.viewmodel.HomeViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import com.ekatayan.app.viewmodel.WishlistViewModel
import kotlinx.coroutines.flow.StateFlow

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen(
    onDestinationClick: (Int) -> Unit,
    onGroupHubClick: () -> Unit,
    onPartnershipClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onTripsClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBookingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
    wishlistViewModel: WishlistViewModel,
) {
    composable(HOME_ROUTE) {
        HomeRoute(
            onDestinationClick = onDestinationClick,
            onWishlistClick = onWishlistClick,
            onGroupHubClick = onGroupHubClick,
            onPartnershipClick = onPartnershipClick,
            onPlannerClick = onPlannerClick,
            onTripsClick = onTripsClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onBookingClick = onBookingClick,
            onSettingsClick = onSettingsClick,
            onNotificationClick = onNotificationClick,
            notificationsUiState = notificationsUiState,
            wishlistViewModel = wishlistViewModel,
        )
    }
}
