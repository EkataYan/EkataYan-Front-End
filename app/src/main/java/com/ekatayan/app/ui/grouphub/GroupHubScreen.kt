package com.ekatayan.app.ui.grouphub

import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.ChatGroup
import com.ekatayan.app.data.model.ChatMessage
import com.ekatayan.app.data.model.ChatUser
import com.ekatayan.app.data.model.GroupFilter
import com.ekatayan.app.data.model.MessageType
import com.ekatayan.app.viewmodel.GroupHubUiState

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.theme.*
import java.time.format.DateTimeFormatter

private val PopupBorder = Color(0xFFAEDCFA)

@Composable
fun GroupHubScreen(state: GroupHubUiState, onGroupClick: (String) -> Unit, onCreateGroup: (String, String, Set<String>, String?) -> Boolean, onHomeClick: () -> Unit, onTripsClick: () -> Unit, onPlannerClick: () -> Unit, onExpensesClick: () -> Unit, onProfileClick: () -> Unit, onSettingsClick: () -> Unit, onNotificationClick: () -> Unit, hasUnreadNotifications: Boolean, onQueryChange: (String) -> Unit, onFilterChange: (GroupFilter) -> Unit, modifier: Modifier = Modifier) {
    val query = state.query
    val filter = state.filter
    var creating by remember { mutableStateOf(false) }
    val statusBarHeight = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
    val groups = state.filteredGroups
    Box(modifier.fillMaxSize().background(EkataBackground)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Group Hub", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                HeaderActions(onNotificationClick = onNotificationClick, onSettingsClick = onSettingsClick, hasUnreadNotifications = hasUnreadNotifications, modifier = Modifier.offset(y = HeaderActionsTopPadding - statusBarHeight))
            }
            Spacer(Modifier.height(18.dp))
            SearchField(query, onQueryChange, "Search Your Groups")
            Spacer(Modifier.height(14.dp))
            Button(onClick = { creating = true }, colors = ButtonDefaults.buttonColors(containerColor = EkataBlue), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Default.GroupAdd, null); Spacer(Modifier.width(8.dp)); Text("Create Group") }
            Text("My Groups", fontSize = 21.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp, bottom = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { GroupFilter.entries.forEach { item -> FilterChip(selected = filter == item, onClick = { onFilterChange(item) }, label = { Text(item.name) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EkataLightBlue), border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filter == item, borderColor = PopupBorder, selectedBorderColor = EkataBlue)) } }
            LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 106.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (groups.isEmpty()) item { EmptyState(if (query.isNotBlank()) "No groups found" else "No groups yet") }
                items(groups, key = ChatGroup::id) { group -> GroupRow(group, state, onGroupClick) }
            }
        }
        AppBottomNavigation(AppBottomNavItem.HOME, onHomeClick, onTripsClick, onPlannerClick, onExpensesClick, onProfileClick, Modifier.align(Alignment.BottomCenter))
    }
    if (creating) CreateGroupDialog(state.users.filterNot { it.id == CURRENT_USER_ID }, { creating = false }, { name, description, members, uri -> if (onCreateGroup(name, description, members, uri)) creating = false })
}

@Composable private fun GroupRow(group: ChatGroup, state: GroupHubUiState, onClick: (String) -> Unit) {
    val last = state.messagesByGroup[group.id].orEmpty().lastOrNull()
    Card(Modifier.fillMaxWidth().clickable { onClick(group.id) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(group, 56)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(group.name, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(messagePreview(last, state), color = EkataTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                last?.let { Text(it.timestamp.format(DateTimeFormatter.ofPattern("h:mm a")), color = EkataTextSecondary, fontSize = 10.sp) }
                if (group.unreadCount > 0) Box(Modifier.padding(top = 7.dp).size(22.dp).background(EkataBlue, CircleShape), contentAlignment = Alignment.Center) { Text(group.unreadCount.toString(), color = Color.White, fontSize = 11.sp) }
            }
        }
    }
}

private fun messagePreview(message: ChatMessage?, state: GroupHubUiState): String {
    if (message == null) return "No messages yet"
    val body = when (message.type) { MessageType.Text -> message.text.orEmpty(); MessageType.Image -> "📷 Photo"; MessageType.File -> "📎 ${message.attachmentName ?: "File"}"; MessageType.Place -> "📍 Shared a place"; MessageType.Voice -> "🎤 Voice note"; MessageType.System -> message.text.orEmpty() }
    return if (message.senderId == CURRENT_USER_ID) "You: $body" else "${state.users.find { it.id == message.senderId }?.name ?: "Member"}: $body"
}

@Composable internal fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Row(Modifier.fillMaxWidth().height(52.dp).background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, PopupBorder, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, null, tint = EkataBlue); Spacer(Modifier.width(10.dp))
        BasicTextField(value, onValueChange, Modifier.weight(1f), singleLine = true, textStyle = TextStyle(color = EkataTextPrimary, fontSize = 14.sp), decorationBox = { inner -> Box { if (value.isEmpty()) Text(placeholder, color = EkataTextSecondary, fontSize = 14.sp); inner() } })
    }
}

@Composable internal fun GroupAvatar(group: ChatGroup, size: Int) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(EkataLightBlue), contentAlignment = Alignment.Center) {
        when { group.imageUri != null -> UriImage(group.imageUri, group.name, Modifier.fillMaxSize()); group.imageRes != null -> Image(painterResource(group.imageRes), group.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop); else -> Icon(Icons.Default.Groups, null, tint = EkataBlue, modifier = Modifier.size((size / 2).dp)) }
    }
}

@Composable internal fun UriImage(uri: String, description: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, uri) { value = runCatching { context.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() } }.getOrNull() }
    bitmap?.let { Image(it, description, modifier, contentScale = ContentScale.Crop) }
}

@Composable private fun CreateGroupDialog(users: List<ChatUser>, onDismiss: () -> Unit, onCreate: (String, String, Set<String>, String?) -> Unit) {
    var name by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var search by remember { mutableStateOf("") }; var selected by remember { mutableStateOf(setOf<String>()) }; var imageUri by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { imageUri = it?.toString() }
    StyledDialog(onDismiss) {
        Text("Create Group", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp)); DialogInput(name, { name = it }, "Group name *"); Spacer(Modifier.height(8.dp)); DialogInput(description, { description = it }, "Description (optional)")
        TextButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Icon(Icons.Default.AddPhotoAlternate, null); Text(if (imageUri == null) " Add group photo" else " Photo selected") }
        SearchField(search, { search = it }, "Search members")
        Column(Modifier.heightIn(max = 190.dp)) { users.filter { it.name.contains(search, true) }.forEach { user -> Row(Modifier.fillMaxWidth().clickable { selected = if (user.id in selected) selected - user.id else selected + user.id }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(user.id in selected, { checked -> selected = if (checked) selected + user.id else selected - user.id }); Text(user.name) } }; if (users.none { it.name.contains(search, true) }) Text("No users found", color = EkataTextSecondary, modifier = Modifier.padding(12.dp)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("Cancel", color = EkataTextPrimary) }; Button(onClick = { onCreate(name, description, selected, imageUri) }, enabled = name.isNotBlank()) { Text("Create") } }
    }
}

@Composable internal fun StyledDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) { Dialog(onDismissRequest = onDismiss) { Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).border(1.dp, PopupBorder, RoundedCornerShape(24.dp)).padding(20.dp), content = content) } }
@Composable internal fun DialogInput(value: String, onChange: (String) -> Unit, label: String) { OutlinedTextField(value, onChange, label = { Text(label) }, textStyle = TextStyle(color = EkataTextPrimary), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EkataBlue, unfocusedBorderColor = PopupBorder, focusedTextColor = EkataTextPrimary, unfocusedTextColor = EkataTextPrimary), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) }
@Composable internal fun EmptyState(text: String) { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text(text, color = EkataTextSecondary) } }
