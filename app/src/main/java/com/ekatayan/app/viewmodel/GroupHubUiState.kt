package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.ChatGroup
import com.ekatayan.app.data.model.ChatMessage
import com.ekatayan.app.data.model.ChatUser
import com.ekatayan.app.data.model.GroupFilter

import com.ekatayan.app.data.model.WishlistItem

data class GroupHubUiState(
    val groups: List<ChatGroup> = emptyList(),
    val users: List<ChatUser> = emptyList(),
    val messagesByGroup: Map<String, List<ChatMessage>> = emptyMap(),
    val typingUserIds: Map<String, Set<String>> = emptyMap(),
    val availableDestinations: List<WishlistItem> = emptyList(),
    val query: String = "",
    val filter: GroupFilter = GroupFilter.All,
) {
    val filteredGroups: List<ChatGroup>
        get() = groups.filter { it.name.contains(query.trim(), true) }.filter {
            filter == GroupFilter.All || filter == GroupFilter.Unread && it.unreadCount > 0 ||
                filter == GroupFilter.Favourites && it.isFavourite
        }
}
