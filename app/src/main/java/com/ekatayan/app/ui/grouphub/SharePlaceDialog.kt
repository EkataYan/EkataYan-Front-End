package com.ekatayan.app.ui.grouphub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.ChatGroup
import com.ekatayan.app.data.model.ChatUser
import com.ekatayan.app.data.model.WishlistItem
import com.ekatayan.app.ui.wishlist.WishlistPopupBorder
import com.ekatayan.app.ui.wishlist.WishlistPopupSurface
import com.ekatayan.app.viewmodel.GroupHubUiState

private enum class ShareRecipientTab { Groups, People }

@Composable
fun SharePlaceDialog(
    item: WishlistItem,
    state: GroupHubUiState,
    onDismiss: () -> Unit,
    onSend: (groupIds: Set<String>, personIds: Set<String>, item: WishlistItem) -> Boolean,
) {
    var selectedTab by remember(item.id) { mutableStateOf(ShareRecipientTab.Groups) }
    var groupQuery by remember(item.id) { mutableStateOf("") }
    var peopleQuery by remember(item.id) { mutableStateOf("") }
    var selectedGroups by remember(item.id) { mutableStateOf(emptySet<String>()) }
    var selectedPeople by remember(item.id) { mutableStateOf(emptySet<String>()) }
    val groups = state.groups.filterNot { it.id.startsWith("direct-") }
        .filter { it.name.contains(groupQuery.trim(), ignoreCase = true) }
    val people = state.users.filterNot { it.id == CURRENT_USER_ID }
        .filter { it.name.contains(peopleQuery.trim(), ignoreCase = true) }
    val hasSelection = selectedGroups.isNotEmpty() || selectedPeople.isNotEmpty()

    WishlistPopupSurface(onDismiss) {
        BoxWithConstraints {
            val contentHeight = (maxHeight * 0.88f).coerceAtMost(640.dp)
            Column(
                Modifier.fillMaxWidth().height(contentHeight).padding(20.dp),
            ) {
                Text(
                    stringResource(R.string.share_place_title),
                    color = EkataTextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
                SharedPlacePreview(item, Modifier.padding(top = 12.dp))
                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ShareTab(
                        label = stringResource(R.string.share_place_groups),
                        selected = selectedTab == ShareRecipientTab.Groups,
                        onClick = { selectedTab = ShareRecipientTab.Groups },
                        modifier = Modifier.weight(1f),
                    )
                    ShareTab(
                        label = stringResource(R.string.share_place_people),
                        selected = selectedTab == ShareRecipientTab.People,
                        onClick = { selectedTab = ShareRecipientTab.People },
                        modifier = Modifier.weight(1f),
                    )
                }
                ShareSearchField(
                    value = if (selectedTab == ShareRecipientTab.Groups) groupQuery else peopleQuery,
                    onValueChange = { value ->
                        if (selectedTab == ShareRecipientTab.Groups) groupQuery = value else peopleQuery = value
                    },
                    placeholder = stringResource(
                        if (selectedTab == ShareRecipientTab.Groups) R.string.share_place_search_groups
                        else R.string.share_place_search_people,
                    ),
                    modifier = Modifier.padding(top = 12.dp),
                )
                LazyColumn(
                    Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (selectedTab == ShareRecipientTab.Groups) {
                        if (groups.isEmpty()) {
                            item { EmptyGroupRecipients(groupQuery.isBlank()) }
                        }
                        items(groups, key = ChatGroup::id) { group ->
                            RecipientRow(
                                selected = group.id in selectedGroups,
                                onClick = {
                                    selectedGroups = selectedGroups.toggle(group.id)
                                },
                                avatar = { GroupAvatar(group, 46) },
                                name = group.name,
                                supportingText = group.description.ifBlank {
                                    stringResource(R.string.share_place_group_members, group.memberIds.size)
                                },
                            )
                        }
                    } else {
                        if (people.isEmpty()) {
                            item { EmptyPeopleRecipients() }
                        }
                        items(people, key = ChatUser::id) { person ->
                            RecipientRow(
                                selected = person.id in selectedPeople,
                                onClick = {
                                    selectedPeople = selectedPeople.toggle(person.id)
                                },
                                avatar = { PersonAvatar(person) },
                                name = person.name,
                                supportingText = stringResource(R.string.share_place_direct_chat),
                            )
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.share_place_cancel), color = EkataTextPrimary)
                    }
                    Button(
                        onClick = {
                            if (onSend(selectedGroups, selectedPeople, item)) onDismiss()
                        },
                        enabled = hasSelection,
                        colors = ButtonDefaults.buttonColors(containerColor = EkataBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(stringResource(R.string.share_place_send), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedPlacePreview(item: WishlistItem, modifier: Modifier = Modifier) {
    Surface(
        modifier.fillMaxWidth(),
        color = Color(0xFFF1F7FD),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WishlistPopupBorder),
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(item.imageRes), item.name,
                Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                Text(item.name, color = EkataTextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.location.orEmpty(), color = EkataTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.description, color = EkataTextSecondary, fontSize = 12.sp, lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun ShareTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        color = if (selected) EkataLightBlue else Color.White,
        contentColor = EkataTextPrimary,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (selected) EkataBlue else WishlistPopupBorder),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        }
    }
}

@Composable
private fun ShareSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().height(48.dp).background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, WishlistPopupBorder, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, null, tint = EkataBlue, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = EkataTextPrimary),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, color = EkataTextSecondary)
                    inner()
                }
            },
        )
    }
}

@Composable
private fun RecipientRow(
    selected: Boolean,
    onClick: () -> Unit,
    avatar: @Composable () -> Unit,
    name: String,
    supportingText: String,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        avatar()
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(name, color = EkataTextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(supportingText, color = EkataTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Surface(
            modifier = Modifier.size(26.dp),
            shape = CircleShape,
            color = if (selected) EkataBlue else Color.White,
            border = BorderStroke(1.dp, if (selected) EkataBlue else WishlistPopupBorder),
        ) {
            if (selected) Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun PersonAvatar(person: ChatUser) {
    Box(Modifier.size(46.dp).background(EkataLightBlue, CircleShape), contentAlignment = Alignment.Center) {
        Text(person.name.take(1).uppercase(), color = EkataTextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyGroupRecipients(noQuery: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.share_place_no_groups), color = EkataTextPrimary, fontWeight = FontWeight.SemiBold)
        if (noQuery) Text(
            stringResource(R.string.share_place_no_groups_message),
            color = EkataTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}

@Composable
private fun EmptyPeopleRecipients() {
    Text(
        stringResource(R.string.share_place_no_people),
        color = EkataTextSecondary,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
    )
}

private fun Set<String>.toggle(value: String): Set<String> = if (value in this) this - value else this + value
