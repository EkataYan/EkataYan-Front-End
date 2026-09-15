package com.ekatayan.app.ui.grouphub

import com.ekatayan.app.viewmodel.GroupHubViewModel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow
import com.ekatayan.app.data.model.WishlistItem

const val GROUP_HUB_ROUTE = "group_hub"
const val GROUP_CHAT_ROUTE = "group_hub/chat/{groupId}"
const val GROUP_INFO_ROUTE = "group_hub/info/{groupId}"
private const val GROUP_ID = "groupId"

fun groupChatRoute(groupId: String) = "group_hub/chat/$groupId"
fun groupInfoRoute(groupId: String) = "group_hub/info/$groupId"

fun NavGraphBuilder.groupHubScreens(
    viewModel: GroupHubViewModel,
    onGroupClick: (String) -> Unit,
    onInfoClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRemoved: () -> Unit,
    onSharedPlaceClick: (WishlistItem) -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
) {
    composable(GROUP_HUB_ROUTE) { GroupHubRoute(viewModel, onGroupClick, onHomeClick, onTripsClick, onPlannerClick, onExpensesClick, onProfileClick, onSettingsClick, onNotificationClick, notificationsUiState) }
    composable(GROUP_CHAT_ROUTE, arguments = listOf(navArgument(GROUP_ID) { type = NavType.StringType })) { entry ->
        GroupChatRoute(requireNotNull(entry.arguments?.getString(GROUP_ID)), viewModel, onBackClick, onInfoClick, onRemoved, onSharedPlaceClick)
    }
    composable(GROUP_INFO_ROUTE, arguments = listOf(navArgument(GROUP_ID) { type = NavType.StringType })) { entry ->
        GroupInfoRoute(requireNotNull(entry.arguments?.getString(GROUP_ID)), viewModel, onBackClick, onRemoved)
    }
}
