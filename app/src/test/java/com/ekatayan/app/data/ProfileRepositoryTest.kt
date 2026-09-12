package com.ekatayan.app.data

import com.ekatayan.app.data.remote.ProfileAuthInterceptor
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.SessionStore
import com.ekatayan.app.data.remote.StoredSession
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.ekatayan.app.data.repository.ProfileFailure
import com.ekatayan.app.data.repository.ProfileLoadException
import com.ekatayan.app.data.repository.ProfileRepository
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileRepositoryTest {
    private val server = MockWebServer()
    private val session = UserSessionProvider(MemorySessionStore())
    private lateinit var repository: ProfileRepository

    @Before fun setup() {
        server.start()
        val api = Retrofit.Builder().baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().addInterceptor(ProfileAuthInterceptor(session)).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(ProfileApiService::class.java)
        repository = ProfileRepository(Lazy { api }, session)
    }

    @After fun teardown() { server.shutdown() }

    @Test fun mapsBackendFieldsAndUsesCurrentToken() = runTest {
        session.setSession("first-test-session", "refresh", Long.MAX_VALUE)
        val json = """{"success":true,"data":{"id":"test-user","display_name":"Traveller","bio":"Hiking","home_city":"Kandy","language":"en","interests":["nature"],"avatar_path":"private/avatar.jpg"}}"""
        server.enqueue(MockResponse().setBody(json))
        val profile = repository.getProfile()
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/api/users/me", request.path)
        assertEquals("Bearer first-test-session", request.getHeader("Authorization"))
        assertEquals("Traveller", profile.name)
        assertEquals("Traveller", session.currentUserName())
        assertEquals("Kandy", profile.location)
        assertEquals("Hiking", profile.bio)
        assertEquals(listOf("nature"), profile.interests)
        assertEquals("private/avatar.jpg", profile.avatarPath)
        assertEquals("", profile.email)
        assertEquals("—", profile.averageRating)
        session.setSession("replacement-test-session", "refresh", Long.MAX_VALUE)
        server.enqueue(MockResponse().setBody(json))
        repository.getProfile()
        assertEquals("Bearer replacement-test-session", server.takeRequest().getHeader("Authorization"))
    }

    @Test fun missingOrExpiredSessionDoesNotSendRequest() = runTest {
        assertFailure(ProfileFailure.AUTHENTICATION)
        session.setSession("expired-test-session", "refresh", 1)
        assertFailure(ProfileFailure.AUTHENTICATION)
        assertEquals(0, server.requestCount)
    }

    @Test fun classifiesHttpFailures() = runTest {
        session.setSession("test-session", "refresh", Long.MAX_VALUE)
        for ((status, expected) in listOf(401 to ProfileFailure.AUTHENTICATION,
            403 to ProfileFailure.FORBIDDEN, 404 to ProfileFailure.NOT_FOUND, 500 to ProfileFailure.SERVER)) {
            server.enqueue(MockResponse().setResponseCode(status))
            assertFailure(expected)
        }
    }

    @Test fun invalidEnvelopeDoesNotBecomeEmptySuccess() = runTest {
        session.setSession("test-session", "refresh", Long.MAX_VALUE)
        server.enqueue(MockResponse().setBody("""{"success":false,"data":null}"""))
        assertFailure(ProfileFailure.INVALID_RESPONSE)
    }

    @Test fun updatesProfileAndCachesReturnedDisplayName() = runTest {
        session.setSession("test-session", "refresh", Long.MAX_VALUE, "user@example.com", "Old Name")
        val json = """{"success":true,"data":{"id":"test-user","email":"user@example.com","display_name":"New Name","bio":"Bio","home_city":"Colombo","language":"en","interests":["hiking"],"avatar_path":null,"phone":"+94771234567"}}"""
        server.enqueue(MockResponse().setBody(json))

        val updated = repository.updateProfile(
            com.ekatayan.app.data.model.ProfileDetails("New Name", "Colombo", "user@example.com", "+94771234567", bio = "Bio", language = "en", interests = listOf("hiking")),
        )

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/api/users/me", request.path)
        assertFalse(request.body.readUtf8().contains("email"))
        assertEquals("New Name", updated.name)
        assertEquals("New Name", session.currentUserName())
        assertEquals("user@example.com", session.currentUserEmail())
    }

    private suspend fun assertFailure(expected: ProfileFailure) {
        try { repository.getProfile(); fail("Expected profile failure") }
        catch (e: ProfileLoadException) { assertEquals(expected, e.failure) }
    }
}

private class MemorySessionStore : SessionStore {
    private var value: StoredSession? = null
    override fun read() = value
    override fun save(session: StoredSession) { value = session }
    override fun clear() { value = null }
}
