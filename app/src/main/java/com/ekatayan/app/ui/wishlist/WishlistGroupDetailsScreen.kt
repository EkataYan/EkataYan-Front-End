package com.ekatayan.app.ui.wishlist

import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataRadius
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.R

@Composable
fun WishlistGroupDetailsScreen(
    group: WishlistGroup?,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onRenameGroup: (String) -> Boolean,
    onPlanWithAiClick: () -> Unit,
    onSearchDestinations: (String) -> List<WishlistItem>,
    hasDestinationMatch: (String) -> Boolean,
    onAddPlace: (WishlistItem) -> Boolean,
    onRemovePlace: (WishlistItem) -> Unit,
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
    var renameVisible by remember { mutableStateOf(false) }
    var addPlaceVisible by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = EkataBackground,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavigation(
                selectedItem = AppBottomNavItem.HOME,
                onHomeClick = onHomeClick,
                onTripsClick = onTripsClick,
                onPlannerClick = onPlannerClick,
                onExpensesClick = onExpensesClick,
                onProfileClick = onProfileClick,
                compact = true,
            )
        },
    ) { scaffoldPadding ->
        if (group == null) {
            Column(Modifier.fillMaxSize().padding(scaffoldPadding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wishlist not found", fontSize = 22.sp)
                TextButton(onClick = onBackClick) { Text("Back to Wish List") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 47.dp, bottom = scaffoldPadding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    GroupDetailsHeader(group.name, group.items.size, onBackClick, onNotificationClick, onSettingsClick, hasUnreadNotifications)
                    GroupActions(
                        onEditClick = { renameVisible = true },
                        onPlanWithAiClick = onPlanWithAiClick,
                        onAddPlaceClick = { addPlaceVisible = true },
                    )
                }
                if (group.items.isEmpty()) {
                    item { EmptyGroupState(onAddPlaceClick = { addPlaceVisible = true }) }
                } else {
                    items(group.items, key = WishlistItem::id) { item ->
                        WishlistPlaceCard(item, onHeartClick = { onRemovePlace(item) })
                    }
                }
            }
        }
    }
    if (renameVisible && group != null) {
        WishlistNameDialog("Rename Wishlist", "Save", group.name, { renameVisible = false }) {
            onRenameGroup(it).also { renamed -> if (renamed) renameVisible = false }
        }
    }
    if (addPlaceVisible && group != null) {
        AddPlaceDialog(
            searchDestinations = onSearchDestinations,
            hasDestinationMatch = hasDestinationMatch,
            onDismiss = { addPlaceVisible = false },
            onAdd = onAddPlace,
        )
    }
}

@Composable
private fun GroupDetailsHeader(title: String, placeCount: Int, onBackClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, hasUnreadNotifications: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.wishlist_back), tint = EkataTextPrimary, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f).padding(start = 4.dp, top = 2.dp, end = 6.dp)) {
            Text(
                title,
                color = EkataTextPrimary,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                pluralStringResource(R.plurals.wishlist_saved_places, placeCount, placeCount),
                color = EkataTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        HeaderActions(onNotificationClick, onSettingsClick, hasUnreadNotifications)
    }
}

@Composable
private fun GroupActions(onEditClick: () -> Unit, onPlanWithAiClick: () -> Unit, onAddPlaceClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = EkataSpacing.md, bottom = EkataSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(EkataSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompactAction(stringResource(R.string.wishlist_edit), Icons.Default.Edit, onEditClick)
        CompactAction(stringResource(R.string.wishlist_plan_with_ai), Icons.Default.AutoAwesome, onPlanWithAiClick, Modifier.weight(1f), prominent = true)
        Surface(
            onClick = onAddPlaceClick,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(EkataRadius.large),
            color = EkataLightBlue,
            contentColor = EkataBlue,
            shadowElevation = EkataElevation.low,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, stringResource(R.string.wishlist_add_place), modifier = Modifier.size(25.dp))
            }
        }
    }
}

@Composable
private fun CompactAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(EkataRadius.large),
        color = if (prominent) EkataLightBlue else EkataLightBlue.copy(alpha = 0.58f),
        contentColor = if (prominent) EkataBlue else EkataTextPrimary,
        shadowElevation = if (prominent) EkataElevation.low else EkataElevation.none,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun WishlistPlaceCard(item: WishlistItem, onHeartClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(EkataRadius.large), shadowElevation = EkataElevation.medium) {
        Box(Modifier.fillMaxWidth().height(196.dp)) {
            Image(painterResource(item.imageRes), item.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x08000000), Color(0x20000000), Color(0xE6000000)))))
            Surface(
                onClick = onHeartClick,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(42.dp),
                shape = RoundedCornerShape(EkataRadius.large),
                color = Color.White.copy(alpha = 0.92f),
                contentColor = Color(0xFFFF2851),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Favorite,
                        stringResource(R.string.wishlist_remove_place, item.name),
                        modifier = Modifier.size(23.dp),
                    )
                }
            }
            Column(Modifier.align(Alignment.BottomStart).padding(14.dp).padding(end = 8.dp)) {
                Text(item.name, color = Color.White, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                item.location?.let { location ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Text(location, color = Color.White.copy(alpha = 0.92f), fontSize = 12.sp, modifier = Modifier.padding(start = 3.dp))
                    }
                }
                Text(item.description, color = Color.White.copy(alpha = 0.94f), fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun EmptyGroupState(onAddPlaceClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 70.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No places saved yet", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text("Add destinations to start building this wishlist.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
        Button(onClick = onAddPlaceClick, colors = ButtonDefaults.buttonColors(containerColor = EkataLightBlue, contentColor = EkataTextPrimary)) { Icon(Icons.Default.Add, null); Text("Add Place", Modifier.padding(start = 6.dp)) }
    }
}

@Composable
private fun AddPlaceDialog(
    searchDestinations: (String) -> List<WishlistItem>,
    hasDestinationMatch: (String) -> Boolean,
    onDismiss: () -> Unit,
    onAdd: (WishlistItem) -> Boolean,
) {
    var query by remember { mutableStateOf("") }
    val results = searchDestinations(query)
    WishlistPopupSurface(onDismiss) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.wishlist_add_place), fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text(stringResource(R.string.wishlist_search_destinations)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                colors = wishlistTextFieldColors(),
            )
            LazyColumn(Modifier.fillMaxWidth().height(340.dp).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (results.isEmpty()) {
                    item {
                        Text(
                            text = if (hasDestinationMatch(query)) "No new destinations available for this wishlist" else "No destinations found",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        )
                    }
                } else {
                    items(results, key = WishlistItem::id) { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Image(painterResource(item.imageRes), null, Modifier.size(48.dp).background(Color.LightGray, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(item.name, fontWeight = FontWeight.SemiBold)
                                Text(item.location.orEmpty(), color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onAdd(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = EkataLightBlue, contentColor = EkataTextPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                            ) { Text("Add") }
                        }
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close", color = EkataTextPrimary) }
        }
    }
}
