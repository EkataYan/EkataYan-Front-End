package com.ekatayan.app.data.repository

import com.ekatayan.app.data.remote.api.EkataYanApiService
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TripsRepositoryPersistenceTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: TripsRepository

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EkataYanApiService::class.java)
        repository = TripsRepository(api)
    }

    @After fun tearDown() = server.shutdown()

    @Test fun createThenReloadRemainsPersisted() = runTest {
        server.enqueue(jsonResponse(envelope(TRIP_JSON)))
        server.enqueue(jsonResponse(envelope("[$TRIP_JSON]")))

        repository.createManualTrip("Weekend", "Kandy", LocalDate.parse("2026-09-25"), LocalDate.parse("2026-09-26"), "1000", "")
        repository.refreshTrips()

        assertEquals(listOf(REMOTE_ID), repository.trips.value.map { it.remoteId })
        assertEquals("POST", server.takeRequest().method)
        assertEquals("GET", server.takeRequest().method)
    }

    @Test fun deleteThenReloadStaysDeleted() = runTest {
        server.enqueue(jsonResponse(envelope("[$TRIP_JSON]")))
        server.enqueue(jsonResponse(envelope("{\"deleted\":true}")))
        server.enqueue(jsonResponse(envelope("[]")))
        repository.refreshTrips()
        val localId = repository.trips.value.single().id

        repository.deleteTrip(localId)
        repository.refreshTrips()

        assertTrue(repository.trips.value.isEmpty())
        assertEquals("DELETE", server.takeRequestAfterSkippingOne().method)
    }

    @Test fun failedDeleteKeepsConfirmedTripVisible() = runTest {
        server.enqueue(jsonResponse(envelope("[$TRIP_JSON]")))
        server.enqueue(MockResponse().setResponseCode(503).setBody("{\"success\":false}"))
        repository.refreshTrips()
        val localId = repository.trips.value.single().id

        val failure = runCatching { repository.deleteTrip(localId) }.exceptionOrNull()

        assertTrue(failure is com.ekatayan.app.data.remote.ApiCallException)
        assertEquals(REMOTE_ID, repository.trips.value.single().remoteId)
    }

    private fun MockWebServer.takeRequestAfterSkippingOne(): okhttp3.mockwebserver.RecordedRequest {
        takeRequest()
        return takeRequest()
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun envelope(data: String) = "{\"success\":true,\"data\":$data}"

    private companion object {
        const val REMOTE_ID = "11111111-1111-4111-8111-111111111111"
        const val TRIP_JSON = """{"id":"$REMOTE_ID","name":"Weekend","destinations":["Kandy"],"start_date":"2026-09-25","end_date":"2026-09-26","budget":"1000.00","currency":"LKR","travelers":1,"interests":[],"preferred_activities":[],"travel_style":"balanced","accommodation_preference":"any","transportation_preference":"any","additional_requirements":"","created_by":"22222222-2222-4222-8222-222222222222","source":"manual"}"""
    }
}
