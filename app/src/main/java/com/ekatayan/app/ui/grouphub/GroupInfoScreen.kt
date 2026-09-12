package com.ekatayan.app.ui.grouphub

import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.ChatTheme
import com.ekatayan.app.data.model.ChatUser
import com.ekatayan.app.viewmodel.GroupHubUiState

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.core.designsystem.theme.*

@Composable
fun GroupInfoScreen(groupId: String, state: GroupHubUiState, onBackClick: () -> Unit, onRename: (String, String) -> Unit, onDescription: (String, String) -> Unit, onPhoto: (String, String) -> Unit, onAddMembers: (String, Set<String>) -> Unit, onRemoveMember: (String, String) -> Unit, onFavourite: (String) -> Unit, onTheme: (String, ChatTheme) -> Unit, onBackground: (String, String?) -> Unit, onRemoveGroup: () -> Unit, modifier: Modifier = Modifier) {
    val group = state.groups.find { it.id == groupId } ?: return EmptyState("Group unavailable")
    var editName by remember { mutableStateOf(false) }; var editDescription by remember { mutableStateOf(false) }; var addMembers by remember { mutableStateOf(false) }; var themeDialog by remember { mutableStateOf(false) }; var leaveDialog by remember { mutableStateOf(false) }; var deleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    fun retain(uri: android.net.Uri) {
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { uri -> retain(uri); onPhoto(groupId, uri.toString()) } }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { it?.let { uri -> retain(uri); onBackground(groupId, uri.toString()) } }
    Column(modifier.fillMaxSize().background(EkataBackground)) {
        Row(Modifier.fillMaxWidth().height(60.dp).background(Color.White), verticalAlignment = Alignment.CenterVertically) { IconButton(onBackClick) { Icon(Icons.Default.ArrowBack, "Back") }; Text("Group Info", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        LazyColumn(contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { GroupAvatar(group, 104); TextButton({ photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Change group photo") }; Text(group.name, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text(group.description.ifEmpty { "Add a group description" }, color = EkataTextSecondary); Text("${group.memberIds.size} members", color = EkataTextSecondary) }
            item { InfoAction(Icons.Default.Edit, "Edit group name") { editName = true }; InfoAction(Icons.Default.Description, "Edit description") { editDescription = true }; InfoAction(Icons.Default.Favorite, if (group.isFavourite) "Remove from favourites" else "Favourite group") { onFavourite(groupId) }; InfoAction(Icons.Default.Palette, "Change theme") { themeDialog = true }; InfoAction(Icons.Default.Wallpaper, "Choose chat background") { backgroundPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }; InfoAction(Icons.Default.GroupAdd, "Add members") { addMembers = true } }
            item { Text("Members", Modifier.fillMaxWidth().padding(top = 12.dp), fontSize = 19.sp, fontWeight = FontWeight.SemiBold) }
            items(group.memberIds, key = { it }) { id -> state.users.find { it.id == id }?.let { user -> Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(15.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(38.dp).background(EkataLightBlue, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(user.name.take(1), fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(11.dp)); Text(user.name, Modifier.weight(1f)); Text(if (id == group.ownerId) "Owner" else "Member", color = EkataTextSecondary, fontSize = 11.sp); if (id != CURRENT_USER_ID && group.ownerId == CURRENT_USER_ID) IconButton({ onRemoveMember(groupId, id) }) { Icon(Icons.Default.PersonRemove, "Remove member", tint = EkataTextSecondary) } } } }
            item { InfoAction(Icons.Default.Logout, "Leave group", MaterialTheme.colorScheme.error) { leaveDialog = true }; if (group.ownerId == CURRENT_USER_ID) InfoAction(Icons.Default.Delete, "Delete group", MaterialTheme.colorScheme.error) { deleteDialog = true } }
        }
    }
    if (editName) EditDialog("Edit group name", group.name, "Group name", { editName = false }) { onRename(groupId, it); editName = false }
    if (editDescription) EditDialog("Edit description", group.description, "Description", { editDescription = false }) { onDescription(groupId, it); editDescription = false }
    if (addMembers) MemberPickerDialog(state.users.filter { it.id !in group.memberIds }, { addMembers = false }) { onAddMembers(groupId, it); addMembers = false }
    if (themeDialog) StyledDialog({ themeDialog = false }) { Text("Change Chat Theme", fontSize = 20.sp, fontWeight = FontWeight.Bold); ChatTheme.entries.forEach { theme -> Row(Modifier.fillMaxWidth().clickable { onTheme(groupId, theme); themeDialog = false }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(24.dp).background(themeColor(theme), androidx.compose.foundation.shape.CircleShape)); Spacer(Modifier.width(12.dp)); Text(theme.name.replace(Regex("([a-z])([A-Z])"), "$1 $2")) } }; TextButton({ onBackground(groupId, null); themeDialog = false }) { Text("Reset background") } }
    if (leaveDialog) ConfirmDialog("Leave group?", "Are you sure you want to leave \"${group.name}\"?", "Leave", { leaveDialog = false }, onRemoveGroup)
    if (deleteDialog) ConfirmDialog("Delete group?", "This will remove this group from the current frontend state.", "Delete", { deleteDialog = false }, onRemoveGroup)
}

@Composable private fun InfoAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, tint: Color = EkataTextPrimary, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(15.dp)).clickable(onClick = onClick).padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint); Spacer(Modifier.width(13.dp)); Text(title, Modifier.weight(1f), color = tint); Icon(Icons.Default.ChevronRight, null, tint = EkataTextSecondary) } }
@Composable private fun EditDialog(title: String, initial: String, label: String, onDismiss: () -> Unit, onSave: (String) -> Unit) { var value by remember { mutableStateOf(initial) }; StyledDialog(onDismiss) { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp); Spacer(Modifier.height(10.dp)); DialogInput(value, { value = it }, label); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onDismiss) { Text("Cancel", color = EkataTextPrimary) }; Button({ onSave(value) }, enabled = label != "Group name" || value.isNotBlank()) { Text("Save") } } } }
@Composable private fun MemberPickerDialog(users: List<ChatUser>, onDismiss: () -> Unit, onAdd: (Set<String>) -> Unit) { var search by remember { mutableStateOf("") }; var selected by remember { mutableStateOf(setOf<String>()) }; val shown = users.filter { it.name.contains(search, true) }; StyledDialog(onDismiss) { Text("Add Members", fontWeight = FontWeight.Bold, fontSize = 20.sp); Spacer(Modifier.height(10.dp)); SearchField(search, { search = it }, "Search members"); Column(Modifier.heightIn(max = 280.dp)) { if (shown.isEmpty()) EmptyState("No users found"); shown.forEach { user -> Row(Modifier.fillMaxWidth().clickable { selected = if (user.id in selected) selected - user.id else selected + user.id }.padding(7.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(user.id in selected, { checked -> selected = if (checked) selected + user.id else selected - user.id }); Text(user.name) } } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onDismiss) { Text("Cancel", color = EkataTextPrimary) }; Button({ onAdd(selected) }, enabled = selected.isNotEmpty()) { Text("Add") } } } }
