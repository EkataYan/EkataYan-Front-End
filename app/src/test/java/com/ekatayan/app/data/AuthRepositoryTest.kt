package com.ekatayan.app.data

import com.ekatayan.app.data.remote.SessionStore
import com.ekatayan.app.data.remote.StoredSession
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.RefreshTokenRequest
import com.ekatayan.app.data.remote.api.SupabaseAuthApiService
import com.ekatayan.app.data.remote.dto.PasswordSignInRequest
import com.ekatayan.app.data.remote.dto.PasswordSignUpMetadata
import com.ekatayan.app.data.remote.dto.PasswordSignUpRequest
import com.ekatayan.app.data.remote.dto.SupabaseSessionDto
import com.ekatayan.app.data.remote.dto.SupabaseUserDto
import com.ekatayan.app.data.repository.SupabaseAuthRepository
import com.ekatayan.app.data.repository.SignUpResult
import com.google.gson.Gson
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {
    @Test fun signUpRequestUsesProfileProvisioningMetadataContract() {
        val json = Gson().toJson(
            PasswordSignUpRequest(
                email = "new@example.com",
                password = "password",
                data = PasswordSignUpMetadata(fullName = "New Traveler", phone = "+94771234567"),
            ),
        )

        assertTrue(json.contains("\"full_name\":\"New Traveler\""))
        assertTrue(json.contains("\"phone\":\"+94771234567\""))
    }

    @Test fun storesCurrentAndRestorableSessionAfterSignIn() = runTest {
        val store = AuthMemorySessionStore()
        val session = UserSessionProvider(store)
        val api = FakeSupabaseAuthApi()
        val repository = SupabaseAuthRepository(Lazy { api }, session)
        repository.signIn("traveler@example.com", "password")
        assertEquals(PasswordSignInRequest("traveler@example.com", "password"), api.request)
        assertEquals("access", session.currentAccessToken())
        assertEquals("refresh", session.refreshToken())
        assertEquals("traveler@example.com", session.currentUserEmail())
        assertEquals("access", UserSessionProvider(store).currentAccessToken())
        assertEquals("traveler@example.com", UserSessionProvider(store).currentUserEmail())
    }

    @Test fun refreshReplacesPersistedSessionAndClearRemovesIt() = runTest {
        val store = AuthMemorySessionStore()
        val session = UserSessionProvider(store)
        session.setSession("old", "refresh", 1)
        val repository = SupabaseAuthRepository(Lazy { FakeSupabaseAuthApi() }, session)
        assertTrue(repository.refreshSession())
        assertEquals("access", session.currentAccessToken())
        repository.clearSession()
        assertNull(UserSessionProvider(store).refreshToken())
    }

    @Test fun restoreUsesPersistedAccessTokenWithoutNetworkRequest() = runTest {
        val store = AuthMemorySessionStore()
        UserSessionProvider(store).setSession("access", "refresh", Long.MAX_VALUE, "traveler@example.com")
        val api = FakeSupabaseAuthApi()
        val repository = SupabaseAuthRepository(Lazy { api }, UserSessionProvider(store))

        assertTrue(repository.restoreSession())
        assertEquals(0, api.refreshCalls)
    }

    @Test fun restoreRefreshesAnOlderSessionToPopulateEmail() = runTest {
        val store = AuthMemorySessionStore()
        UserSessionProvider(store).setSession("access", "refresh", Long.MAX_VALUE)
        val api = FakeSupabaseAuthApi()
        val session = UserSessionProvider(store)
        val repository = SupabaseAuthRepository(Lazy { api }, session)

        assertTrue(repository.restoreSession())
        assertEquals(1, api.refreshCalls)
        assertEquals("traveler@example.com", repository.currentUserEmail())
    }

    @Test fun authenticatedSignUpStoresTheSharedSession() = runTest {
        val store = AuthMemorySessionStore()
        val api = FakeSupabaseAuthApi()
        val session = UserSessionProvider(store)

        assertEquals(SignUpResult.AUTHENTICATED, SupabaseAuthRepository(Lazy { api }, session).signUp("New Traveler", "new@example.com", "+94771234567", "password"))
        assertEquals(
            PasswordSignUpRequest("new@example.com", "password", PasswordSignUpMetadata("New Traveler", "+94771234567")),
            api.signUpRequest,
        )
        assertEquals("access", session.currentAccessToken())
    }

    @Test fun confirmationRequiredSignUpDoesNotStoreASession() = runTest {
        val store = AuthMemorySessionStore()
        val api = FakeSupabaseAuthApi(signUpResponse = SupabaseSessionDto(null, null, null, null))
        val session = UserSessionProvider(store)

        assertEquals(SignUpResult.EMAIL_CONFIRMATION_REQUIRED, SupabaseAuthRepository(Lazy { api }, session).signUp("New Traveler", "new@example.com", "+94771234567", "password"))
        assertNull(session.currentAccessToken())
    }
}

private class AuthMemorySessionStore : SessionStore {
    private var value: StoredSession? = null
    override fun read() = value
    override fun save(session: StoredSession) { value = session }
    override fun clear() { value = null }
}

private class FakeSupabaseAuthApi(
    private val signUpResponse: SupabaseSessionDto = session(),
) : SupabaseAuthApiService {
    var request: PasswordSignInRequest? = null
    var signUpRequest: PasswordSignUpRequest? = null
    var refreshCalls = 0
    override suspend fun signInWithPassword(grantType: String, request: PasswordSignInRequest): SupabaseSessionDto {
        this.request = request
        return session()
    }
    override suspend fun signUpWithPassword(request: PasswordSignUpRequest): SupabaseSessionDto {
        signUpRequest = request
        return signUpResponse
    }
    override suspend fun refreshSession(grantType: String, request: RefreshTokenRequest): SupabaseSessionDto {
        refreshCalls++
        return session()
    }
    private companion object {
        fun session() = SupabaseSessionDto(
            "access",
            "refresh",
            3600,
            null,
            SupabaseUserDto("user-id", "traveler@example.com"),
        )
    }
}
