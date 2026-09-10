package com.ekatayan.app.ui.wishlist

import com.ekatayan.app.data.model.WishlistCover
import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataRadius
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val WishlistPopupBorder = Color(0xFFAEDCFA)
private val EmptyCoverColor = Color(0xFFE9EBEF)
private val PopupShape = RoundedCornerShape(20.dp)

@Composable
fun WishlistGroupCard(
    group: WishlistGroup,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onChangeCoverClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val coverPlace = (group.cover as? WishlistCover.FromPlace)?.let { cover -> group.items.find { it.id == cover.placeId } }
    val deviceCover = (group.cover as? WishlistCover.FromDevice)?.uri
    val hasCover = coverPlace != null || deviceCover != null
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(EkataRadius.large),
        color = EmptyCoverColor,
        shadowElevation = EkataElevation.medium,
    ) {
        Box(Modifier.fillMaxWidth().height(190.dp)) {
            when {
                deviceCover != null -> UriCoverImage(deviceCover, group.name)
                coverPlace != null -> Image(
                    painter = painterResource(coverPlace.imageRes),
                    contentDescription = group.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            if (hasCover) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x16000000), Color(0xD9000000)),
                            startY = 65f,
                        ),
                    ),
                )
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(EkataSpacing.md),
            ) {
                Text(
                    text = group.name,
                    color = if (hasCover) Color.White else EkataTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(R.plurals.wishlist_places, group.items.size, group.items.size),
                    color = if (hasCover) Color.White.copy(alpha = 0.9f) else EkataTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Box(Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, "Wishlist options", tint = if (hasCover) Color.White else EkataTextPrimary)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.border(1.dp, WishlistPopupBorder, RoundedCornerShape(12.dp)),
                    containerColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    DropdownMenuItem(text = { Text("Rename", color = EkataTextPrimary) }, onClick = { menuExpanded = false; onRenameClick() })
                    DropdownMenuItem(text = { Text("Change Cover Photo", color = EkataTextPrimary) }, onClick = { menuExpanded = false; onChangeCoverClick() })
                    DropdownMenuItem(text = { Text("Delete", color = EkataTextPrimary) }, onClick = { menuExpanded = false; onDeleteClick() })
                }
            }
        }
    }
}

@Composable
private fun UriCoverImage(uriString: String, contentDescription: String) {
    val context = LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, uriString) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            }.getOrNull()
        }
    }
    bitmap?.let { Image(it, contentDescription, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
}

@Composable
fun CreateWishlistButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        containerColor = Color(0xFFD6ECFA),
        contentColor = Color.Black,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = EkataElevation.medium),
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = stringResource(R.string.wishlist_create_content_description),
            tint = Color.Black,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun EmptyWishlistState(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 70.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No wishlists yet", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text("Create your first wishlist to save the places you love.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
    }
}

@Composable
fun WishlistPopupSurface(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).border(1.dp, WishlistPopupBorder, PopupShape),
            shape = PopupShape,
            color = Color.White,
            contentColor = EkataTextPrimary,
            shadowElevation = 8.dp,
        ) { content() }
    }
}

@Composable
fun WishlistNameDialog(title: String, confirmLabel: String, initialName: String, onDismiss: () -> Unit, onConfirm: (String) -> Boolean) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var hasError by remember { mutableStateOf(false) }
    WishlistPopupSurface(onDismiss) {
        Column(Modifier.padding(22.dp)) {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; hasError = false },
                label = { Text("Wishlist name") },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                singleLine = true,
                isError = hasError,
                supportingText = if (hasError) ({ Text("Name can't be blank") }) else null,
                colors = wishlistTextFieldColors(),
            )
            Row(Modifier.align(Alignment.End).padding(top = 8.dp)) {
                TextButton(onDismiss) { Text("Cancel", color = EkataTextPrimary) }
                TextButton(onClick = { hasError = !onConfirm(name) }) { Text(confirmLabel) }
            }
        }
    }
}

@Composable
fun DeleteWishlistDialog(groupName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    WishlistPopupSurface(onDismiss) {
        Column(Modifier.padding(22.dp)) {
            Text("Delete \"$groupName\"?", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text("This wishlist and its saved places will be removed.", Modifier.padding(top = 12.dp))
            Row(Modifier.align(Alignment.End).padding(top = 12.dp)) {
                TextButton(onDismiss) { Text("Cancel", color = EkataTextPrimary) }
                TextButton(onConfirm) { Text("Delete", color = Color(0xFFD6284D)) }
            }
        }
    }
}

@Composable
fun ChangeCoverPhotoDialog(onDismiss: () -> Unit, onUseSavedPlace: () -> Unit, onChooseFromDevice: () -> Unit) {
    WishlistPopupSurface(onDismiss) {
        Column(Modifier.padding(22.dp)) {
            Text("Change Cover Photo", color = EkataTextPrimary, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Button(
                onClick = onUseSavedPlace,
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EkataLightBlue, contentColor = EkataTextPrimary),
            ) { Text("Use a saved place image") }
            Button(
                onClick = onChooseFromDevice,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = EkataTextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, WishlistPopupBorder),
            ) { Text("Choose from device") }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Cancel", color = EkataTextPrimary) }
        }
    }
}

@Composable
fun SavedPlaceCoverDialog(
    items: List<WishlistItem>,
    onDismiss: () -> Unit,
    onPlaceSelected: (Int) -> Unit,
    onChooseFromDevice: () -> Unit,
) {
    WishlistPopupSurface(onDismiss) {
        Column(Modifier.padding(22.dp)) {
            Text("Use a saved place image", color = EkataTextPrimary, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            if (items.isEmpty()) {
                Text("No saved places available", color = EkataTextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 20.dp))
                Text("Add places to this wishlist first, or choose an image from your device.", color = EkataTextPrimary, modifier = Modifier.padding(top = 8.dp))
                Button(
                    onClick = onChooseFromDevice,
                    colors = ButtonDefaults.buttonColors(containerColor = EkataLightBlue, contentColor = EkataTextPrimary),
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Choose from device") }
            } else {
                LazyColumn(Modifier.fillMaxWidth().height(330.dp).padding(top = 12.dp)) {
                    items(items, key = WishlistItem::id) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPlaceSelected(item.id) }.padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(painterResource(item.imageRes), null, Modifier.size(60.dp).background(EmptyCoverColor, RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                            Text(item.name, color = EkataTextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close", color = EkataTextPrimary) }
        }
    }
}

@Composable
fun wishlistTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = EkataTextPrimary,
    unfocusedTextColor = EkataTextPrimary,
    disabledTextColor = EkataTextPrimary,
    cursorColor = EkataTextPrimary,
    focusedLabelColor = EkataTextPrimary,
    unfocusedLabelColor = EkataTextPrimary,
    focusedBorderColor = WishlistPopupBorder,
    unfocusedBorderColor = WishlistPopupBorder,
    focusedPlaceholderColor = Color.Gray,
    unfocusedPlaceholderColor = Color.Gray,
)
