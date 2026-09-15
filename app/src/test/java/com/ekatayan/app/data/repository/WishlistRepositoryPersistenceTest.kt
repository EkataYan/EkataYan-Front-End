package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.remote.api.EkataYanApiService
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

class WishlistRepositoryPersistenceTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: WishlistRepository

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        repository = WishlistRepository(
            Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(EkataYanApiService::class.java),
        )
    }

    @After fun tearDown() = server.shutdown()

    @Test fun createAndReloadUsesBackendAsAuthority() = runTest {
        server.enqueue(json(envelope("{\"id\":\"$WISHLIST_ID\",\"name\":\"Weekend\",\"saved_places\":[]}")))
        server.enqueue(json(envelope("[{\"id\":\"$WISHLIST_ID\",\"name\":\"Weekend\",\"saved_places\":[]}]")))

        repository.create("Weekend")
        repository.refresh()

        assertEquals(WISHLIST_ID, repository.state.value.groups.single().remoteId)
        assertEquals(listOf("POST", "GET"), requests(2))
    }

    @Test fun failedDeleteDoesNotRemoveConfirmedWishlist() = runTest {
        server.enqueue(json(envelope("[{\"id\":\"$WISHLIST_ID\",\"name\":\"Weekend\",\"saved_places\":[]}]")))
        server.enqueue(MockResponse().setResponseCode(503).setBody("{\"success\":false}"))
        repository.refresh()
        val id = repository.state.value.groups.single().id

        val failure = runCatching { repository.delete(id) }.exceptionOrNull()

        assertTrue(failure is com.ekatayan.app.data.remote.ApiCallException)
        assertEquals(WISHLIST_ID, repository.state.value.groups.single().remoteId)
    }

    @Test fun addAndRemovePlaceWaitForBackendConfirmation() = runTest {
        server.enqueue(json(envelope("[{\"id\":\"$WISHLIST_ID\",\"name\":\"Weekend\",\"saved_places\":[]}]")))
        server.enqueue(json(envelope("{\"id\":\"$PLACE_ID\",\"wishlist_id\":\"$WISHLIST_ID\",\"name\":\"Kandy\",\"external_place_id\":\"catalog:14\"}")))
        server.enqueue(json(envelope("{\"deleted\":true}")))
        repository.refresh()
        val groupId = repository.state.value.groups.single().id
        val place = WishlistDestinationCatalog.destinations.first { it.id == 14 }

        repository.addPlace(groupId, place)
        assertEquals(PLACE_ID, repository.state.value.groups.single().items.single().savedPlaceId)
        repository.removePlace(groupId, place.id)

        assertTrue(repository.state.value.groups.single().items.isEmpty())
        assertEquals(listOf("GET", "POST", "DELETE"), requests(3))
    }

    private fun requests(count: Int) = List(count) { server.takeRequest().method.orEmpty() }
    private fun json(body: String) = MockResponse().setResponseCode(200)
        .setHeader("Content-Type", "application/json").setBody(body)
    private fun envelope(data: String) = "{\"success\":true,\"data\":$data}"

    private companion object {
        const val WISHLIST_ID = "33333333-3333-4333-8333-333333333333"
        const val PLACE_ID = "44444444-4444-4444-8444-444444444444"
    }
}
