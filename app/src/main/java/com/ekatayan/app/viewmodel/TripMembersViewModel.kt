package com.ekatayan.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.remote.api.PublicTripMemberDto
import com.ekatayan.app.data.remote.api.PublicUserDto
import com.ekatayan.app.data.repository.TripMembersRepository
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ekatayan.app.utils.runSuspendCatching

data class TripMembersUiState(val members: List<PublicTripMemberDto> = emptyList(), val results: List<PublicUserDto> = emptyList(),
    val query: String = "", val loading: Boolean = true, val searching: Boolean = false,
    val invitingUserIds: Set<String> = emptySet(), val removingUserIds: Set<String> = emptySet(), val error: String? = null) {
    val canInvite get() = members.any { it.isCurrentUser && it.role == "owner" }
    val canManageMembers get() = members.any { it.isCurrentUser && it.role in setOf("owner", "admin") }
}

@HiltViewModel class TripMembersViewModel @Inject constructor(private val repository: TripMembersRepository,
    savedState: SavedStateHandle, private val strings: StringResourceProvider) : ViewModel() {
    val tripId: String = savedState["tripId"] ?: ""
    private val mutable = MutableStateFlow(TripMembersUiState())
    val state = mutable.asStateFlow(); private var searchJob: Job? = null
    init { refresh() }
    fun refresh() = viewModelScope.launch { mutable.value = mutable.value.copy(loading=true,error=null); runSuspendCatching { repository.members(tripId) }.onSuccess { mutable.value=mutable.value.copy(members=it,loading=false) }.onFailure { mutable.value=mutable.value.copy(loading=false,error=strings[R.string.trip_members_load_error]) } }
    fun search(query: String) { mutable.value=mutable.value.copy(query=query); searchJob?.cancel(); if(query.trim().isEmpty()){ mutable.value=mutable.value.copy(results=emptyList(),searching=false); return }; searchJob=viewModelScope.launch { delay(300); mutable.value=mutable.value.copy(searching=true,error=null); try { val results=repository.search(tripId,query.trim()); if (mutable.value.query == query) mutable.value=mutable.value.copy(results=results,searching=false) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Throwable) { if (mutable.value.query == query) mutable.value=mutable.value.copy(searching=false,error=strings[R.string.trip_members_search_error]) } } }
    fun invite(user: PublicUserDto) = viewModelScope.launch {
        if (user.relationship != "invite" || user.id in mutable.value.invitingUserIds) return@launch
        mutable.value = mutable.value.copy(
            invitingUserIds = mutable.value.invitingUserIds + user.id,
            error = null,
        )
        runSuspendCatching { repository.invite(tripId, user.id) }
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
                    error = strings[R.string.trip_members_invite_error],
                )
            }
    }
    fun remove(userId:String)=viewModelScope.launch {
        if (userId in mutable.value.removingUserIds) return@launch
        mutable.value=mutable.value.copy(removingUserIds=mutable.value.removingUserIds+userId,error=null)
        runSuspendCatching { repository.remove(tripId,userId) }
            .onSuccess {
                mutable.value=mutable.value.copy(
                    members=mutable.value.members.filterNot { it.userId == userId },
                    removingUserIds=mutable.value.removingUserIds-userId,
                )
            }
            .onFailure { mutable.value=mutable.value.copy(removingUserIds=mutable.value.removingUserIds-userId,error=strings[R.string.trip_members_remove_error]) }
    }
}
