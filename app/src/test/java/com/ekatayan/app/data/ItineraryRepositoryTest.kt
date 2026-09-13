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

    @Test fun previewUsesCurrentTypedPlannerContract() = runTest {
        server.enqueue(json("""{"success":true,"data":{"trip":{"title":"Kandy Escape","summary":"A relaxed Kandy day.","route":["Kandy"],"start_date":"2026-10-01","end_date":"2026-10-01","duration_days":1,"traveller_type":"Couple","traveller_count":2,"travel_style":"Comfort","travel_pace":"Balanced"},"days":[{"day_number":1,"date":"2026-10-01","destination":"Kandy","title":"Kandy highlights","summary":"A realistic day.","activities":[{"id":"kandy-1","name":"Temple visit","category":"Culture","location":{"name":"Temple of the Tooth","latitude":null,"longitude":null},"start_time":"09:00","end_time":"10:30","duration_minutes":90,"description":"Visit the temple.","estimated_cost_lkr":4000,"transport_from_previous":"Tuk-tuk","travel_time_minutes":15}],"day_estimated_cost_lkr":{"min":12000,"max":18000}}],"cost_estimate":{"currency":"LKR","accommodation":{"min":8000,"max":12000},"transport":{"min":2000,"max":3000},"food":{"min":3000,"max":5000},"activities":{"min":4000,"max":5000},"total":{"min":17000,"max":25000},"disclaimer":"AI-generated estimate only. Actual prices may vary."},"recommendations":[]}}"""))
        val input = ItineraryPlanInput(listOf("Kandy"), "Couple", LocalDate.parse("2026-10-01"),
            LocalDate.parse("2026-10-01"), 2, "Hotel", listOf("Tuk-tuk"), "Comfort",
            listOf("Culture"), "Balanced", null)

        val result = repository.preview(input)
        val request = server.takeRequest()

        assertEquals("Kandy Escape", result.trip.title)
        assertEquals(25000L, result.costEstimate.total.max)
        assertEquals("/api/itineraries/preview", request.path)
        assert(request.body.readUtf8().contains("\"allow_ai_destination_suggestions\":false"))
    }

    private fun json(body: String) = MockResponse().setResponseCode(200)
        .setHeader("Content-Type", "application/json").setBody(body)
}
