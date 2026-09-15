package com.ekatayan.app.data.model

import androidx.annotation.DrawableRes
import java.time.LocalDateTime

const val CURRENT_USER_ID = "current-user"

data class ChatUser(val id: String, val name: String, val role: GroupRole = GroupRole.Member)
enum class GroupRole { Owner, Admin, Member }
enum class ChatTheme { DefaultBlue, Sky, Mint, Lavender, Warm }
enum class MessageType { Text, Image, File, Place, SharedPlace, Voice, System }

data class ChatGroup(
    val id: String,
    val name: String,
    val description: String = "",
    @DrawableRes val imageRes: Int? = null,
    val imageUri: String? = null,
    val memberIds: List<String> = emptyList(),
    val ownerId: String = CURRENT_USER_ID,
    val isFavourite: Boolean = false,
    val unreadCount: Int = 0,
    val theme: ChatTheme = ChatTheme.DefaultBlue,
    val backgroundUri: String? = null,
)

data class ChatMessage(
    val id: String,
    val groupId: String,
    val senderId: String,
    val type: MessageType,
    val text: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val placeId: Int? = null,
    val replyToMessageId: String? = null,
    val reactions: List<String> = emptyList(),
    val voiceDurationSeconds: Int? = null,
)

enum class GroupFilter { All, Unread, Favourites }

data class GroupHubData(
    val groups: List<ChatGroup> = emptyList(),
    val users: List<ChatUser> = emptyList(),
    val messagesByGroup: Map<String, List<ChatMessage>> = emptyMap(),
    val typingUserIds: Map<String, Set<String>> = emptyMap(),
)
