package com.ekatayan.app.data

import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.repository.ItineraryPlanInput
import com.ekatayan.app.data.repository.ItineraryRepository
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ItineraryRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ItineraryRepository

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        val api = Retrofit.Builder().baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(EkataYanApiService::class.java)
        repository = ItineraryRepository(api)
    }

    @After fun tearDown() = server.shutdown()

    @Test fun retryAfterProviderFailureReusesCreatedTrip() = runTest {
        server.enqueue(json("""{"success":true,"data":{"id":"trip-1","name":"Kandy itinerary","destinations":["Kandy"],"start_date":"2026-10-01","end_date":"2026-10-01","budget":"10000.00","currency":"LKR","travelers":2,"interests":["Culture"],"preferred_activities":[],"travel_style":"Balanced","accommodation_preference":"Any","transportation_preference":"Public","additional_requirements":"","created_by":"user-1"}}"""))
        server.enqueue(json("""{"success":false,"error":{"code":"AI_UNAVAILABLE","message":"Try again."}}"""))
        server.enqueue(json("""{"success":true,"data":{"id":"plan-1","trip_id":"trip-1","overview":"Kandy day","currency":"LKR","recommendations":[],"itinerary_days":[{"day_number":1,"trip_date":"2026-10-01","locations":["Kandy"],"notes":"","itinerary_activities":[{"title":"Temple","location":"Kandy","suggested_time":"09:00:00","description":"Visit","estimated_cost":"2000.00","transport":"Walk","notes":""}]}]}}"""))
        val input = ItineraryPlanInput("Kandy", LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-01"), "10000", 2, "Any", "Public", "Balanced", listOf("Culture"))

        runCatching { repository.generate(input) }
        val result = repository.generate(input)

        assertEquals("plan-1", result.id)
        assertEquals("/api/trips", server.takeRequest().path)
        assertEquals("/api/itineraries/generate", server.takeRequest().path)
        assertEquals("/api/itineraries/generate", server.takeRequest().path)
        assertEquals(0, server.requestCount - 3)
    }

    private fun json(body: String) = MockResponse().setResponseCode(200)
        .setHeader("Content-Type", "application/json").setBody(body)
}
