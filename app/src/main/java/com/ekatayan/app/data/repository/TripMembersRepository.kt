package com.ekatayan.app.data.repository

import com.ekatayan.app.data.remote.api.*
import javax.inject.Inject

class TripMembersRepository @Inject constructor(private val api: EkataYanApiService) {
    suspend fun members(tripId: String) = api.members(tripId).data ?: error("We couldn't load trip members.")
    suspend fun search(tripId: String, query: String) = api.searchUsers(query, tripId).data ?: emptyList()
    suspend fun invite(tripId: String, userId: String) {
        api.inviteUser(tripId, TripInviteRequest(userId)).data ?: error("The invitation could not be sent.")
    }
    suspend fun remove(tripId: String, userId: String) { api.removeMember(tripId, userId) }
    suspend fun leave(tripId: String) { api.leaveTrip(tripId) }
}
