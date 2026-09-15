package com.ekatayan.app.data.repository

import com.ekatayan.app.data.remote.api.*
import javax.inject.Inject
import javax.inject.Singleton
import com.ekatayan.app.data.remote.apiCall

@Singleton
class TripMembersRepository @Inject constructor(private val api: EkataYanApiService) {
    suspend fun members(tripId: String) = apiCall("We couldn't load trip members.") { api.members(tripId) }.requireData("We couldn't load trip members.")
    suspend fun search(tripId: String, query: String) = apiCall("We couldn't search for travellers.") { api.searchUsers(query, tripId) }.requireData("We couldn't search for travellers.")
    suspend fun invite(tripId: String, userId: String) {
        apiCall("The invitation could not be sent.") { api.inviteUser(tripId, TripInviteRequest(userId)) }.requireData("The invitation could not be sent.")
    }
    suspend fun remove(tripId: String, userId: String) {
        val response = apiCall("The member could not be removed.") { api.removeMember(tripId, userId) }
        if (!response.success || response.data?.get("deleted") != true) {
            error(response.error?.message ?: "The member could not be removed.")
        }
    }
    suspend fun leave(tripId: String) {
        val response = apiCall("You could not leave this trip.") { api.leaveTrip(tripId) }
        if (!response.success || response.data?.get("left") != true) {
            error(response.error?.message ?: "You could not leave this trip.")
        }
    }

    private fun <T> ApiEnvelope<T>.requireData(fallback: String): T =
        data.takeIf { success } ?: error(error?.message ?: fallback)
}
