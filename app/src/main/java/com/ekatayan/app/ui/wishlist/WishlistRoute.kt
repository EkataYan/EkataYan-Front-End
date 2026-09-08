package com.ekatayan.app.ui.wishlist

import com.ekatayan.app.viewmodel.WishlistViewModel

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun WishlistRoute(
    viewModel: WishlistViewModel,
    onGroupClick: (Int) -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    WishlistScreen(
        uiState = uiState,
        onGroupClick = onGroupClick,
        onCreateGroup = viewModel::createGroup,
        onRenameGroup = viewModel::renameGroup,
        onDeleteGroup = viewModel::deleteGroup,
        onUpdateGroupCoverFromDevice = viewModel::updateGroupCoverFromDevice,
        onUpdateGroupCoverFromPlace = viewModel::updateGroupCoverFromPlace,
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onNotificationClick = onNotificationClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}

@Composable
fun WishlistGroupDetailsRoute(
    groupId: Int,
    viewModel: WishlistViewModel,
    onBackClick: () -> Unit,
    onPlanWithAiClick: (Int) -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    val group = uiState.groups.find { it.id == groupId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    WishlistGroupDetailsScreen(
        group = group,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRenameGroup = { viewModel.renameGroup(groupId, it) },
        onPlanWithAiClick = { onPlanWithAiClick(groupId) },
        onSearchDestinations = { query -> viewModel.searchAvailableDestinations(groupId, query) },
        hasDestinationMatch = viewModel::hasDestinationMatch,
        onAddPlace = { item ->
            val added = viewModel.addPlaceToGroup(groupId, item)
            if (!added) scope.launch { snackbarHostState.showSnackbar("Already in this wishlist") }
            added
        },
        onRemovePlace = { item ->
            viewModel.removePlaceFromGroup(groupId, item.id)
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Removed from ${group?.name.orEmpty()}",
                    actionLabel = "UNDO",
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.addPlaceToGroup(groupId, item)
            }
        },
        onHomeClick = onHomeClick,
        onTripsClick = onTripsClick,
        onPlannerClick = onPlannerClick,
        onExpensesClick = onExpensesClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onNotificationClick = onNotificationClick,
        hasUnreadNotifications = notificationState.hasUnreadNotifications,
    )
}
