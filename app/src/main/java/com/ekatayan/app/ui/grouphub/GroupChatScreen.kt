package com.ekatayan.app.ui.grouphub

import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.ChatGroup
import com.ekatayan.app.data.model.ChatMessage
import com.ekatayan.app.data.model.ChatTheme
import com.ekatayan.app.data.model.MessageType
import com.ekatayan.app.viewmodel.GroupHubUiState

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.data.model.WishlistItem
import java.time.format.DateTimeFormatter

@Composable
fun GroupChatScreen(groupId: String, state: GroupHubUiState, onBackClick: () -> Unit, onInfoClick: () -> Unit, onSendText: (String, String, String?) -> Unit, onSendAttachment: (String, MessageType, String?, String?, Int?) -> Unit, onReact: (String, String) -> Unit, onDeleteMessage: (String, String) -> Unit, onTheme: (String, ChatTheme) -> Unit, onBackground: (String, String?) -> Unit, onLeave: () -> Unit, onFileSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val group = state.groups.find { it.id == groupId } ?: return EmptyState("Group unavailable")
    val messages = state.messagesByGroup[groupId].orEmpty(); val listState = rememberLazyListState(); var input by rememberSaveable(groupId) { mutableStateOf("") }; var menu by remember { mutableStateOf(false) }; var attachmentMenu by remember { mutableStateOf(false) }; var placePicker by remember { mutableStateOf(false) }; var searchMode by remember { mutableStateOf(false) }; var search by remember { mutableStateOf("") }; var selectedMessage by remember { mutableStateOf<ChatMessage?>(null) }; var replyTo by remember { mutableStateOf<ChatMessage?>(null) }; var confirmLeave by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let {
        runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        onSendAttachment(groupId, MessageType.Image, it.toString(), null, null)
    } }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let { onFileSelected(it.toString()) } }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }
    val outgoing = themeColor(group.theme)
    Column(modifier.fillMaxSize().background(themeTint(group.theme)).imePadding()) {
        Surface(color = Color.White, shadowElevation = 3.dp) { Row(Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBackClick) { Icon(Icons.Default.ArrowBack, "Back") }; GroupAvatar(group, 46); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(group.name, fontWeight = FontWeight.Bold, fontSize = 17.sp); Text(memberSummary(group, state), color = EkataTextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            Box { IconButton({ menu = true }) { Icon(Icons.Default.MoreVert, "Group options") }; DropdownMenu(menu, { menu = false }, containerColor = Color.White, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAEDCFA))) { listOf("Group Info", "Search Messages", "Change Chat Theme", "Change Chat Background", "Leave Group").forEach { label -> DropdownMenuItem({ Text(label, color = EkataTextPrimary) }, onClick = { menu = false; when (label) { "Group Info" -> onInfoClick(); "Search Messages" -> searchMode = true; "Change Chat Theme" -> onTheme(groupId, ChatTheme.entries[(group.theme.ordinal + 1) % ChatTheme.entries.size]); "Change Chat Background" -> onBackground(groupId, null); "Leave Group" -> confirmLeave = true } }) } } }
        } }
        if (searchMode) Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.weight(1f)) { SearchField(search, { search = it }, "Search messages") }; IconButton({ searchMode = false; search = "" }) { Icon(Icons.Default.Close, null) } }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            group.backgroundUri?.let { UriImage(it, null, Modifier.fillMaxSize()) }
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = if (group.backgroundUri == null) .38f else .72f)))
            val shown = messages.filter { search.isBlank() || it.text?.contains(search, true) == true || it.attachmentName?.contains(search, true) == true }
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (shown.isEmpty()) item { EmptyState(if (search.isNotBlank()) "No messages found" else "No messages yet.\nStart the conversation!") }
                items(shown, key = ChatMessage::id) { message -> MessageBubble(message, state, messages, outgoing, { selectedMessage = message }) }
                state.typingUserIds[groupId].orEmpty().firstOrNull()?.let { id -> item { Text("${state.users.find { it.id == id }?.name ?: "Someone"}  •••", color = EkataTextSecondary, modifier = Modifier.background(Color.White, RoundedCornerShape(18.dp)).padding(12.dp)) } }
            }
        }
        replyTo?.let { Row(Modifier.fillMaxWidth().background(EkataLightBlue).padding(horizontal = 14.dp, vertical = 6.dp)) { Text("Replying to: ${it.text ?: it.type.name}", Modifier.weight(1f), maxLines = 1); Icon(Icons.Default.Close, null, Modifier.clickable { replyTo = null }) } }
        Row(Modifier.fillMaxWidth().background(Color.White).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ input += "😊" }) { Icon(Icons.Default.EmojiEmotions, "Emoji") }
            BasicTextField(input, { input = it }, Modifier.weight(1f).background(EkataBackground, RoundedCornerShape(22.dp)).padding(horizontal = 15.dp, vertical = 12.dp), textStyle = TextStyle(color = EkataTextPrimary, fontSize = 15.sp), decorationBox = { inner -> Box { if (input.isEmpty()) Text("Message", color = EkataTextSecondary); inner() } })
            Box { IconButton({ attachmentMenu = true }) { Icon(Icons.Default.AttachFile, "Attach") }; DropdownMenu(attachmentMenu, { attachmentMenu = false }, containerColor = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAEDCFA)), shape = RoundedCornerShape(16.dp)) { listOf("Photo", "File", "Place", "Mock voice note").forEach { label -> DropdownMenuItem({ Text(label, color = EkataTextPrimary) }, onClick = { attachmentMenu = false; when(label) { "Photo" -> photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)); "File" -> filePicker.launch(arrayOf("*/*")); "Place" -> placePicker = true; else -> onSendAttachment(groupId, MessageType.Voice, null, "Frontend demo voice note", null) } }) } } }
            IconButton({ if (input.isNotBlank()) { onSendText(groupId, input, replyTo?.id); input = ""; replyTo = null } }) { Box(Modifier.size(42.dp).background(outgoing, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp)) } }
        }
    }
    if (placePicker) PlacePickerDialog(state.availableDestinations, { placePicker = false }) { onSendAttachment(groupId, MessageType.Place, null, null, it); placePicker = false }
    selectedMessage?.let { message -> StyledDialog({ selectedMessage = null }) { Text("Message options", fontWeight = FontWeight.Bold); TextButton({ replyTo = message; selectedMessage = null }) { Text("Reply", color = EkataTextPrimary) }; TextButton({ onReact(groupId, message.id); selectedMessage = null }) { Text("React ❤️", color = EkataTextPrimary) }; if (message.senderId == CURRENT_USER_ID) TextButton({ onDeleteMessage(groupId, message.id); selectedMessage = null }) { Text("Delete for me", color = MaterialTheme.colorScheme.error) } } }
    if (confirmLeave) ConfirmDialog("Leave group?", "Are you sure you want to leave \"${group.name}\"?", "Leave", { confirmLeave = false }, onLeave)
}

@Composable private fun MessageBubble(message: ChatMessage, state: GroupHubUiState, all: List<ChatMessage>, outgoingColor: Color, onLongClick: () -> Unit) {
    val mine = message.senderId == CURRENT_USER_ID
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Column(Modifier.widthIn(max = 292.dp)) {
            if (!mine) Text(state.users.find { it.id == message.senderId }?.name ?: "Member", color = EkataBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp, bottom = 2.dp))
            Surface(color = if (mine) outgoingColor else Color.White, shape = RoundedCornerShape(18.dp, 18.dp, if (mine) 4.dp else 18.dp, if (mine) 18.dp else 4.dp), shadowElevation = if (mine) 0.dp else 1.dp, modifier = Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick)) {
                Column(Modifier.padding(11.dp)) {
                    message.replyToMessageId?.let { id -> all.find { it.id == id }?.let { Text("↪ ${it.text ?: it.type.name}", color = if (mine) Color.White.copy(.8f) else EkataTextSecondary, fontSize = 11.sp, modifier = Modifier.background(Color.Black.copy(.08f), RoundedCornerShape(8.dp)).padding(6.dp)) } }
                    when (message.type) {
                        MessageType.Text, MessageType.System -> Text(message.text.orEmpty(), color = if (mine) Color.White else EkataTextPrimary)
                        MessageType.Image -> { message.attachmentUri?.let { UriImage(it, "Photo message", Modifier.sizeIn(maxWidth = 240.dp, maxHeight = 210.dp).aspectRatio(1.25f).clip(RoundedCornerShape(12.dp))) }; Text("Photo", color = if (mine) Color.White else EkataTextPrimary) }
                        MessageType.File -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.InsertDriveFile, null, tint = if (mine) Color.White else EkataBlue); Spacer(Modifier.width(8.dp)); Column { Text(message.attachmentName ?: "File", color = if (mine) Color.White else EkataTextPrimary); Text("Document", fontSize = 10.sp, color = if (mine) Color.White.copy(.8f) else EkataTextSecondary) } }
                        MessageType.Place -> { val place = state.availableDestinations.find { it.id == message.placeId }; place?.let { androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(it.imageRes), it.name, Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop); Text("📍 ${it.name}", fontWeight = FontWeight.Bold, color = if (mine) Color.White else EkataTextPrimary); Text(it.location.orEmpty(), fontSize = 11.sp, color = if (mine) Color.White.copy(.8f) else EkataTextSecondary) } }
                        MessageType.Voice -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.PlayArrow, null, tint = if (mine) Color.White else EkataBlue); Text(" ━━━━━  ${message.voiceDurationSeconds ?: 0}s", color = if (mine) Color.White else EkataTextPrimary) }
                    }
                    Text(message.timestamp.format(DateTimeFormatter.ofPattern("h:mm a")), fontSize = 9.sp, color = if (mine) Color.White.copy(.75f) else EkataTextSecondary, modifier = Modifier.align(Alignment.End))
                }
            }
            if (message.reactions.isNotEmpty()) Text(message.reactions.joinToString(""), fontSize = 16.sp, modifier = Modifier.align(Alignment.End).offset(y = (-5).dp))
        }
    }
}

@Composable private fun PlacePickerDialog(destinations: List<WishlistItem>, onDismiss: () -> Unit, onSelect: (Int) -> Unit) { var query by remember { mutableStateOf("") }; val places = destinations.filter { it.name.contains(query, true) || it.location?.contains(query, true) == true }; StyledDialog(onDismiss) { Text("Share a place", fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); SearchField(query, { query = it }, "Search destinations..."); LazyColumn(Modifier.heightIn(max = 360.dp)) { if (places.isEmpty()) item { EmptyState("No destinations found") }; items(places, key = { it.id }) { place -> Row(Modifier.fillMaxWidth().clickable { onSelect(place.id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(place.imageRes), place.name, Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop); Spacer(Modifier.width(10.dp)); Column { Text(place.name, fontWeight = FontWeight.SemiBold); Text(place.location.orEmpty(), color = EkataTextSecondary, fontSize = 12.sp) } } } } } }

@Composable internal fun ConfirmDialog(title: String, text: String, action: String, onDismiss: () -> Unit, onConfirm: () -> Unit) { StyledDialog(onDismiss) { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text(text, color = EkataTextSecondary, modifier = Modifier.padding(vertical = 12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onDismiss) { Text("Cancel", color = EkataTextPrimary) }; Button(onClick = onConfirm) { Text(action) } } } }
private fun memberSummary(group: ChatGroup, state: GroupHubUiState) = group.memberIds.filterNot { it == CURRENT_USER_ID }.mapNotNull { id -> state.users.find { it.id == id }?.name }.joinToString().ifEmpty { "Just you" }
internal fun themeColor(theme: ChatTheme) = when(theme) { ChatTheme.DefaultBlue -> EkataBlue; ChatTheme.Sky -> Color(0xFF57A7D9); ChatTheme.Mint -> Color(0xFF43A88B); ChatTheme.Lavender -> Color(0xFF8774C7); ChatTheme.Warm -> Color(0xFFD47B63) }
private fun themeTint(theme: ChatTheme) = when(theme) { ChatTheme.DefaultBlue -> Color(0xFFEAF5FC); ChatTheme.Sky -> Color(0xFFE4F5FC); ChatTheme.Mint -> Color(0xFFE7F6F0); ChatTheme.Lavender -> Color(0xFFF1EDFA); ChatTheme.Warm -> Color(0xFFFBEFEA) }
