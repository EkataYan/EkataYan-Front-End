package com.ekatayan.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.remote.api.PublicTripMemberDto
import com.ekatayan.app.data.remote.api.PublicUserDto
import com.ekatayan.app.data.repository.TripMembersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripMembersUiState(val members: List<PublicTripMemberDto> = emptyList(), val results: List<PublicUserDto> = emptyList(),
    val query: String = "", val loading: Boolean = true, val searching: Boolean = false,
    val invitingUserIds: Set<String> = emptySet(), val error: String? = null) {
    val canInvite get() = members.any { it.isCurrentUser && it.role == "owner" }
    val canManageMembers get() = members.any { it.isCurrentUser && it.role in setOf("owner", "admin") }
}

@HiltViewModel class TripMembersViewModel @Inject constructor(private val repository: TripMembersRepository,
    savedState: SavedStateHandle) : ViewModel() {
    val tripId: String = savedState["tripId"] ?: ""
    private val mutable = MutableStateFlow(TripMembersUiState())
    val state = mutable.asStateFlow(); private var searchJob: Job? = null
    init { refresh() }
    fun refresh() = viewModelScope.launch { mutable.value = mutable.value.copy(loading=true,error=null); runCatching { repository.members(tripId) }.onSuccess { mutable.value=mutable.value.copy(members=it,loading=false) }.onFailure { mutable.value=mutable.value.copy(loading=false,error=it.message) } }
    fun search(query: String) { mutable.value=mutable.value.copy(query=query); searchJob?.cancel(); if(query.trim().isEmpty()){ mutable.value=mutable.value.copy(results=emptyList()); return }; searchJob=viewModelScope.launch { delay(300); mutable.value=mutable.value.copy(searching=true); runCatching { repository.search(tripId,query) }.onSuccess { mutable.value=mutable.value.copy(results=it,searching=false) }.onFailure { mutable.value=mutable.value.copy(searching=false,error=it.message) } } }
    fun invite(user: PublicUserDto) = viewModelScope.launch {
        if (user.relationship != "invite" || user.id in mutable.value.invitingUserIds) return@launch
        mutable.value = mutable.value.copy(
            invitingUserIds = mutable.value.invitingUserIds + user.id,
            error = null,
        )
        runCatching { repository.invite(tripId, user.id) }
            .onSuccess {
                mutable.value = mutable.value.copy(
                    results = mutable.value.results.map { result ->
                        if (result.id == user.id) result.copy(relationship = "invited") else result
                    },
                    invitingUserIds = mutable.value.invitingUserIds - user.id,
                )
            }
            .onFailure {
                mutable.value = mutable.value.copy(
                    invitingUserIds = mutable.value.invitingUserIds - user.id,
                    error = it.message,
                )
            }
    }
    fun remove(userId:String)=viewModelScope.launch { runCatching { repository.remove(tripId,userId) }.onSuccess { refresh() }.onFailure { mutable.value=mutable.value.copy(error=it.message) } }
}
