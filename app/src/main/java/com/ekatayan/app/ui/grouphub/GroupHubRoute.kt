package com.ekatayan.app.ui.grouphub

import com.ekatayan.app.viewmodel.GroupHubViewModel
import com.ekatayan.app.data.model.WishlistItem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GroupHubRoute(viewModel: GroupHubViewModel, onGroupClick: (String) -> Unit, onHomeClick: () -> Unit, onTripsClick: () -> Unit, onPlannerClick: () -> Unit, onExpensesClick: () -> Unit, onProfileClick: () -> Unit, onSettingsClick: () -> Unit, onNotificationClick: () -> Unit, notificationsUiState: StateFlow<NotificationsUiState>) {
    val state by viewModel.uiState.collectAsState()
    val notificationState by notificationsUiState.collectAsStateWithLifecycle()
    GroupHubScreen(state, onGroupClick, viewModel::createGroup, onHomeClick, onTripsClick, onPlannerClick, onExpensesClick, onProfileClick, onSettingsClick, onNotificationClick, notificationState.hasUnreadNotifications, viewModel::searchGroups, viewModel::filterGroups)
}

@Composable
fun GroupChatRoute(groupId: String, viewModel: GroupHubViewModel, onBackClick: () -> Unit, onInfoClick: (String) -> Unit, onRemoved: () -> Unit, onSharedPlaceClick: (WishlistItem) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(groupId) { viewModel.markRead(groupId) }
    GroupChatScreen(groupId, state, onBackClick, { onInfoClick(groupId) }, viewModel::sendText, viewModel::sendAttachment, viewModel::react, viewModel::deleteMessage, viewModel::updateTheme, viewModel::updateBackground, { viewModel.leaveOrDelete(groupId); onRemoved() }, onFileSelected = { viewModel.sendFile(groupId, it) }, onSharedPlaceClick = onSharedPlaceClick)
}

@Composable
fun GroupInfoRoute(groupId: String, viewModel: GroupHubViewModel, onBackClick: () -> Unit, onRemoved: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    GroupInfoScreen(groupId, state, onBackClick, viewModel::rename, viewModel::updateDescription, viewModel::updatePhoto, viewModel::addMembers, viewModel::removeMember, viewModel::toggleFavourite, viewModel::updateTheme, viewModel::updateBackground, { viewModel.leaveOrDelete(groupId); onRemoved() })
}
