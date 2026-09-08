package com.ekatayan.app.ui.wishlist

import com.ekatayan.app.viewmodel.WishlistViewModel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val WISHLIST_ROUTE = "wishlist"
const val WISHLIST_GROUP_ROUTE = "wishlist/{groupId}"
private const val GROUP_ID_ARGUMENT = "groupId"

fun wishlistGroupRoute(groupId: Int) = "wishlist/$groupId"

fun NavGraphBuilder.wishlistScreens(
    viewModel: WishlistViewModel,
    onGroupClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: (Int?) -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
) {
    composable(WISHLIST_ROUTE) {
        WishlistRoute(
            viewModel = viewModel,
            onGroupClick = onGroupClick,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = { onPlannerClick(null) },
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
            onNotificationClick = onNotificationClick,
            notificationsUiState = notificationsUiState,
        )
    }
    composable(
        route = WISHLIST_GROUP_ROUTE,
        arguments = listOf(navArgument(GROUP_ID_ARGUMENT) { type = NavType.IntType }),
    ) { entry ->
        WishlistGroupDetailsRoute(
            groupId = requireNotNull(entry.arguments?.getInt(GROUP_ID_ARGUMENT)),
            viewModel = viewModel,
            onBackClick = onBackClick,
            onPlanWithAiClick = { onPlannerClick(it) },
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = { onPlannerClick(null) },
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
            onNotificationClick = onNotificationClick,
            notificationsUiState = notificationsUiState,
        )
    }
}
