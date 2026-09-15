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
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val groups = state.filteredGroups
    Box(modifier.fillMaxSize().background(EkataBackground)) {
        Column(Modifier.fillMaxSize().padding(horizontal = EkataSpacing.pageHorizontal)) {
            Row(Modifier.fillMaxWidth().height(HeaderActionsTopPadding), verticalAlignment = Alignment.CenterVertically) {
                Text("Group Hub", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                HeaderActions(onNotificationClick = onNotificationClick, onSettingsClick = onSettingsClick, hasUnreadNotifications = hasUnreadNotifications)
            }
            Spacer(Modifier.height(18.dp))
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Text("Preview • Groups and messages are saved on this device only.", modifier = Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(10.dp))
            SearchField(query, onQueryChange, "Search Your Groups")
            Spacer(Modifier.height(14.dp))
            Button(onClick = { creating = true }, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().height(EkataComponentSize.buttonHeight)) { Icon(Icons.Default.GroupAdd, null); Spacer(Modifier.width(EkataSpacing.xs)); Text("Create Group", style = MaterialTheme.typography.labelLarge) }
            Text("My Groups", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = EkataSpacing.lg, bottom = EkataSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { GroupFilter.entries.forEach { item -> FilterChip(selected = filter == item, onClick = { onFilterChange(item) }, label = { Text(item.name) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EkataLightBlue), border = FilterChipDefaults.filterChipBorder(enabled = true, selected = filter == item, borderColor = PopupBorder, selectedBorderColor = EkataBlue)) } }
            LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 106.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (groups.isEmpty()) item {
                    EmptyState(
                        if (query.isNotBlank()) "No groups found"
                        else "No groups yet\nCreate a trip or join your friends.",
                    )
                }
                items(groups, key = ChatGroup::id) { group -> GroupRow(group, state, onGroupClick) }
            }
        }
        AppBottomNavigation(AppBottomNavItem.HOME, onHomeClick, onTripsClick, onPlannerClick, onExpensesClick, onProfileClick, Modifier.align(Alignment.BottomCenter))
    }
    if (creating) CreateGroupDialog(state.users.filterNot { it.id == CURRENT_USER_ID }, { creating = false }, { name, description, members, uri -> if (onCreateGroup(name, description, members, uri)) creating = false })
}

@Composable private fun GroupRow(group: ChatGroup, state: GroupHubUiState, onClick: (String) -> Unit) {
    val last = state.messagesByGroup[group.id].orEmpty().lastOrNull()
    Card(Modifier.fillMaxWidth().clickable { onClick(group.id) }, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(EkataElevation.low)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(group, 56)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(group.name, style = MaterialTheme.typography.titleSmall)
                Text(messagePreview(last, state), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                last?.let { Text(it.timestamp.format(DateTimeFormatter.ofPattern("h:mm a")), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
                if (group.unreadCount > 0) Box(Modifier.padding(top = 7.dp).size(22.dp).background(EkataBlue, CircleShape), contentAlignment = Alignment.Center) { Text(group.unreadCount.toString(), color = Color.White, fontSize = 11.sp) }
            }
        }
    }
}

private fun messagePreview(message: ChatMessage?, state: GroupHubUiState): String {
    if (message == null) return "No messages yet"
    val body = when (message.type) { MessageType.Text -> message.text.orEmpty(); MessageType.Image -> "📷 Photo"; MessageType.File -> "📎 ${message.attachmentName ?: "File"}"; MessageType.Place, MessageType.SharedPlace -> "📍 Shared a place"; MessageType.Voice -> "🎤 Voice note"; MessageType.System -> message.text.orEmpty() }
    return if (message.senderId == CURRENT_USER_ID) "You: $body" else "${state.users.find { it.id == message.senderId }?.name ?: "Member"}: $body"
}

@Composable internal fun SearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Row(Modifier.fillMaxWidth().height(EkataComponentSize.inputMinHeight).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium).border(EkataStroke.thin, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium).padding(horizontal = EkataSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, null, tint = EkataBlue); Spacer(Modifier.width(10.dp))
        BasicTextField(value, onValueChange, Modifier.weight(1f), singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface), decorationBox = { inner -> Box { if (value.isEmpty()) Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium); inner() } })
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
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        imageUri = uri?.toString()
    }
    StyledDialog(onDismiss) {
        Text("Create Group", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp)); DialogInput(name, { name = it }, "Group name *"); Spacer(Modifier.height(8.dp)); DialogInput(description, { description = it }, "Description (optional)")
        TextButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Icon(Icons.Default.AddPhotoAlternate, null); Text(if (imageUri == null) " Add group photo" else " Photo selected") }
        SearchField(search, { search = it }, "Search members")
        Column(Modifier.heightIn(max = 190.dp)) { users.filter { it.name.contains(search, true) }.forEach { user -> Row(Modifier.fillMaxWidth().clickable { selected = if (user.id in selected) selected - user.id else selected + user.id }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(user.id in selected, { checked -> selected = if (checked) selected + user.id else selected - user.id }); Text(user.name) } }; if (users.none { it.name.contains(search, true) }) Text("No users found", color = EkataTextSecondary, modifier = Modifier.padding(12.dp)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text("Cancel", color = EkataTextPrimary) }; Button(onClick = { onCreate(name, description, selected, imageUri) }, enabled = name.isNotBlank()) { Text("Create") } }
    }
}

@Composable internal fun StyledDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) { Dialog(onDismissRequest = onDismiss) { Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large).border(EkataStroke.thin, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.large).padding(EkataSpacing.lg), content = content) } }
@Composable internal fun DialogInput(value: String, onChange: (String) -> Unit, label: String) { OutlinedTextField(value, onChange, label = { Text(label) }, textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().heightIn(min = EkataComponentSize.inputMinHeight)) }
@Composable internal fun EmptyState(text: String) { Box(Modifier.fillMaxWidth().padding(EkataSpacing.xl), contentAlignment = Alignment.Center) { Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) } }
