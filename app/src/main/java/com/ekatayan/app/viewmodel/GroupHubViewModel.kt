package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.ChatGroup
import com.ekatayan.app.data.model.ChatMessage
import com.ekatayan.app.data.model.ChatTheme
import com.ekatayan.app.data.model.GroupFilter
import com.ekatayan.app.data.model.GroupHubData
import com.ekatayan.app.data.model.MessageType
import com.ekatayan.app.data.model.WishlistItem

import com.ekatayan.app.data.repository.GroupHubRepository
import com.ekatayan.app.data.repository.DocumentRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ekatayan.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class GroupHubViewModel @Inject constructor(private val repository: GroupHubRepository, private val documents: DocumentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(repository.state.value.toUiState())
    val uiState: StateFlow<GroupHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.state.collect { data ->
                _uiState.update { data.toUiState(it.query, it.filter) }
            }
        }
    }

    private fun GroupHubData.toUiState(query: String = "", filter: GroupFilter = GroupFilter.All) =
        GroupHubUiState(groups, (users + repository.directoryUsers).distinctBy { it.id }, messagesByGroup, typingUserIds, repository.destinations, query, filter)

    fun searchGroups(value: String) { _uiState.update { it.copy(query = value) } }
    fun filterGroups(value: GroupFilter) { _uiState.update { it.copy(filter = value) } }
    fun sendFile(groupId: String, uri: String) {
        viewModelScope.launch {
            val name = documents.persistAndReadName(uri)
            sendAttachment(groupId, MessageType.File, uri, name)
        }
    }

    fun markRead(groupId: String) = updateGroup(groupId) { it.copy(unreadCount = 0) }
    fun toggleFavourite(groupId: String) = updateGroup(groupId) { it.copy(isFavourite = !it.isFavourite) }
    fun rename(groupId: String, value: String) = value.trim().takeIf(String::isNotEmpty)?.let { name -> updateGroup(groupId) { it.copy(name = name) } }
    fun updateDescription(groupId: String, value: String) = updateGroup(groupId) { it.copy(description = value.trim()) }
    fun updatePhoto(groupId: String, uri: String) = updateGroup(groupId) { it.copy(imageUri = uri) }
    fun updateTheme(groupId: String, theme: ChatTheme) = updateGroup(groupId) { it.copy(theme = theme) }
    fun updateBackground(groupId: String, uri: String?) = updateGroup(groupId) { it.copy(backgroundUri = uri) }
    fun addMembers(groupId: String, ids: Set<String>) = updateGroup(groupId) { it.copy(memberIds = (it.memberIds + ids).distinct()) }
    fun removeMember(groupId: String, userId: String) { if (userId != CURRENT_USER_ID) updateGroup(groupId) { it.copy(memberIds = it.memberIds - userId) } }

    fun createGroup(name: String, description: String, memberIds: Set<String>, imageUri: String?): Boolean {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return false
        val id = UUID.randomUUID().toString()
        val group = ChatGroup(id, cleanName, description.trim(), imageUri = imageUri, memberIds = (listOf(CURRENT_USER_ID) + memberIds).distinct())
        repository.update { state ->
            val members = repository.directoryUsers.filter { it.id in group.memberIds }
            state.copy(
                users = (state.users + members).distinctBy { it.id },
                groups = listOf(group) + state.groups,
                messagesByGroup = state.messagesByGroup + (id to emptyList()),
            )
        }
        return true
    }

    fun sharePlace(groupIds: Set<String>, personIds: Set<String>, item: WishlistItem): Boolean {
        if (groupIds.isEmpty() && personIds.isEmpty()) return false
        val selectedPeople = uiState.value.users.filter { it.id in personIds && it.id != CURRENT_USER_ID }
        val validGroupIds = uiState.value.groups.filter { it.id in groupIds }.mapTo(linkedSetOf()) { it.id }
        if (validGroupIds.isEmpty() && selectedPeople.isEmpty()) return false
        repository.update { state ->
            val users = (state.users + repository.directoryUsers.filter { it.id == CURRENT_USER_ID } + selectedPeople)
                .distinctBy { it.id }
            var groups = state.groups
            val directGroupIds = selectedPeople.map { person ->
                val id = "direct-${person.id}"
                if (groups.none { it.id == id }) {
                    groups = listOf(
                        ChatGroup(
                            id = id,
                            name = person.name,
                            description = "",
                            memberIds = listOf(CURRENT_USER_ID, person.id),
                        ),
                    ) + groups
                }
                id
            }
            val targetIds = (validGroupIds + directGroupIds).distinct()
            var messages = state.messagesByGroup
            val sentAt = LocalDateTime.now()
            targetIds.forEachIndexed { index, groupId ->
                val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    groupId = groupId,
                    senderId = CURRENT_USER_ID,
                    type = MessageType.SharedPlace,
                    timestamp = sentAt.plusNanos(index.toLong()),
                    placeId = item.id,
                )
                messages = messages + (groupId to (messages[groupId].orEmpty() + message))
            }
            groups = groups.map { group -> if (group.id in targetIds) group.copy(unreadCount = 0) else group }
            state.copy(users = users, groups = groups, messagesByGroup = messages)
        }
        return true
    }

    fun sendText(groupId: String, text: String, replyTo: String? = null) {
        val clean = text.trim()
        if (clean.isNotEmpty()) addMessage(ChatMessage(UUID.randomUUID().toString(), groupId, CURRENT_USER_ID, MessageType.Text, clean, LocalDateTime.now(), replyToMessageId = replyTo))
    }

    fun sendAttachment(groupId: String, type: MessageType, uri: String? = null, name: String? = null, placeId: Int? = null) =
        addMessage(ChatMessage(UUID.randomUUID().toString(), groupId, CURRENT_USER_ID, type, timestamp = LocalDateTime.now(), attachmentUri = uri, attachmentName = name, placeId = placeId, voiceDurationSeconds = if (type == MessageType.Voice) 8 else null))

    fun react(groupId: String, messageId: String) {
        repository.update { state -> state.copy(messagesByGroup = state.messagesByGroup + (groupId to state.messagesByGroup[groupId].orEmpty().map { if (it.id == messageId) it.copy(reactions = if ("❤️" in it.reactions) it.reactions - "❤️" else it.reactions + "❤️") else it })) }
    }

    fun deleteMessage(groupId: String, messageId: String) {
        repository.update { it.copy(messagesByGroup = it.messagesByGroup + (groupId to it.messagesByGroup[groupId].orEmpty().filterNot { message -> message.id == messageId })) }
    }

    fun leaveOrDelete(groupId: String) { repository.update { it.copy(groups = it.groups.filterNot { group -> group.id == groupId }, messagesByGroup = it.messagesByGroup - groupId) } }

    private fun addMessage(message: ChatMessage) {
        repository.update { state ->
            val group = state.groups.firstOrNull { it.id == message.groupId }
            state.copy(
                groups = if (group == null) state.groups else listOf(group.copy(unreadCount = 0)) + state.groups.filterNot { it.id == message.groupId },
                messagesByGroup = state.messagesByGroup + (message.groupId to (state.messagesByGroup[message.groupId].orEmpty() + message)),
            )
        }
    }

    private fun updateGroup(id: String, block: (ChatGroup) -> ChatGroup) { repository.update { it.copy(groups = it.groups.map { group -> if (group.id == id) block(group) else group }) } }
}

