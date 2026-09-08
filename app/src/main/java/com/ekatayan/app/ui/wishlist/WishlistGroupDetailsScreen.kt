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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavigation(
                selectedItem = AppBottomNavItem.HOME,
                onHomeClick = onHomeClick,
                onTripsClick = onTripsClick,
                onPlannerClick = onPlannerClick,
                onExpensesClick = onExpensesClick,
                onProfileClick = onProfileClick,
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 45.dp, bottom = scaffoldPadding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    GroupDetailsHeader(group.name, onBackClick, onNotificationClick, onSettingsClick, hasUnreadNotifications)
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
private fun GroupDetailsHeader(title: String, onBackClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, hasUnreadNotifications: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowLeft, "Back", modifier = Modifier.size(28.dp)) }
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        HeaderActions(onNotificationClick, onSettingsClick, hasUnreadNotifications, Modifier.offset(y = HeaderActionsTopPadding - 45.dp))
    }
}

@Composable
private fun GroupActions(onEditClick: () -> Unit, onPlanWithAiClick: () -> Unit, onAddPlaceClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactAction("Edit", Icons.Default.Edit, onEditClick)
        CompactAction("Plan your trip with AI", Icons.Default.AutoAwesome, onPlanWithAiClick, Modifier.weight(1f))
        IconButton(onClick = onAddPlaceClick, modifier = Modifier.size(44.dp).background(EkataLightBlue, RoundedCornerShape(10.dp))) {
            Icon(Icons.Default.Add, "Add Place")
        }
    }
}

@Composable
private fun CompactAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.height(44.dp), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = EkataLightBlue, contentColor = EkataTextPrimary)) {
        Icon(icon, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(5.dp)); Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun WishlistPlaceCard(item: WishlistItem, onHeartClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), shadowElevation = 3.dp) {
        Box(Modifier.fillMaxWidth().height(190.dp)) {
            Image(painterResource(item.imageRes), item.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x22000000), Color(0xD9000000)))))
            Text(item.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.TopStart).padding(13.dp))
            IconButton(onClick = onHeartClick, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Default.Favorite, "Remove ${item.name}", tint = Color(0xFFFF2851), modifier = Modifier.size(27.dp)) }
            Column(Modifier.align(Alignment.BottomStart).padding(13.dp).padding(end = 8.dp)) {
                item.location?.let { Text(it, color = Color.White.copy(.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                Text(item.description, color = Color.White, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
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
            Text("Add Place", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                placeholder = { Text("Search destinations...") },
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
