package com.ekatayan.app.ui.wishlist

import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.viewmodel.WishlistUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.R

@Composable
fun WishlistScreen(
    uiState: WishlistUiState,
    onGroupClick: (Int) -> Unit,
    onCreateGroup: (String) -> Boolean,
    onRenameGroup: (Int, String) -> Boolean,
    onDeleteGroup: (Int) -> Unit,
    onUpdateGroupCoverFromDevice: (Int, String) -> Unit,
    onUpdateGroupCoverFromPlace: (Int, Int) -> Boolean,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var createDialogVisible by remember { mutableStateOf(false) }
    var renameGroup by remember { mutableStateOf<WishlistGroup?>(null) }
    var deleteGroup by remember { mutableStateOf<WishlistGroup?>(null) }
    var coverGroup by remember { mutableStateOf<WishlistGroup?>(null) }
    var coverChoicesVisible by remember { mutableStateOf(false) }
    var savedPlaceSelectorVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var coverGroupId by remember { mutableStateOf<Int?>(null) }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val selectedGroupId = coverGroupId
        if (uri != null && selectedGroupId != null) onUpdateGroupCoverFromDevice(selectedGroupId, uri.toString())
        coverGroupId = null
    }

    Box(modifier.fillMaxSize().background(EkataBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 47.dp, bottom = 176.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                WishlistHeader(onNotificationClick, onSettingsClick, hasUnreadNotifications)
            }
            if (uiState.groups.isEmpty()) {
                item { EmptyWishlistState() }
            } else {
                items(uiState.groups, key = WishlistGroup::id) { group ->
                    WishlistGroupCard(
                        group = group,
                        onClick = { onGroupClick(group.id) },
                        onRenameClick = { renameGroup = group },
                        onChangeCoverClick = {
                            coverGroup = group
                            coverChoicesVisible = true
                        },
                        onDeleteClick = { deleteGroup = group },
                    )
                }
            }
        }
        CreateWishlistButton(
            onClick = { createDialogVisible = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 92.dp),
        )
        AppBottomNavigation(
            selectedItem = AppBottomNavItem.HOME,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter),
            compact = true,
        )
    }

    if (createDialogVisible) {
        WishlistNameDialog(
            title = "Create New Wishlist",
            confirmLabel = "Create",
            initialName = "",
            onDismiss = { createDialogVisible = false },
            onConfirm = { name -> onCreateGroup(name).also { if (it) createDialogVisible = false } },
        )
    }
    renameGroup?.let { group ->
        WishlistNameDialog(
            title = "Rename Wishlist",
            confirmLabel = "Save",
            initialName = group.name,
            onDismiss = { renameGroup = null },
            onConfirm = { name -> onRenameGroup(group.id, name).also { if (it) renameGroup = null } },
        )
    }
    deleteGroup?.let { group ->
        DeleteWishlistDialog(
            groupName = group.name,
            onDismiss = { deleteGroup = null },
            onConfirm = { onDeleteGroup(group.id); deleteGroup = null },
        )
    }
    coverGroup?.let { group ->
        if (coverChoicesVisible) {
            ChangeCoverPhotoDialog(
                onDismiss = { coverChoicesVisible = false; coverGroup = null },
                onUseSavedPlace = { coverChoicesVisible = false; savedPlaceSelectorVisible = true },
                onChooseFromDevice = {
                    coverChoicesVisible = false
                    coverGroupId = group.id
                    coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    coverGroup = null
                },
            )
        }
        if (savedPlaceSelectorVisible) {
            SavedPlaceCoverDialog(
                items = group.items,
                onDismiss = { savedPlaceSelectorVisible = false; coverGroup = null },
                onPlaceSelected = { placeId ->
                    if (onUpdateGroupCoverFromPlace(group.id, placeId)) {
                        savedPlaceSelectorVisible = false
                        coverGroup = null
                    }
                },
                onChooseFromDevice = {
                    savedPlaceSelectorVisible = false
                    coverGroupId = group.id
                    coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    coverGroup = null
                },
            )
        }
    }
}

@Composable
private fun WishlistHeader(onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, hasUnreadNotifications: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.wishlist_title),
            color = EkataTextPrimary,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        HeaderActions(onNotificationClick, onSettingsClick, hasUnreadNotifications)
    }
}
