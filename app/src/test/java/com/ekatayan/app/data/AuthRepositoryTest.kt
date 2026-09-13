package com.ekatayan.app.data

import com.ekatayan.app.data.remote.SessionStore
import com.ekatayan.app.data.remote.StoredSession
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.RefreshTokenRequest
import com.ekatayan.app.data.remote.api.GoogleIdTokenRequest
import com.ekatayan.app.data.remote.api.GoogleUserMetadataRequest
import com.ekatayan.app.data.remote.api.SupabaseAuthApiService
import com.ekatayan.app.data.remote.dto.PasswordSignInRequest
import com.ekatayan.app.data.remote.dto.PasswordSignUpMetadata
import com.ekatayan.app.data.remote.dto.PasswordSignUpRequest
import com.ekatayan.app.data.remote.dto.SupabaseSessionDto
import com.ekatayan.app.data.remote.dto.SupabaseUserDto
import com.ekatayan.app.data.remote.dto.SupabaseUserMetadataDto
import com.ekatayan.app.data.repository.SupabaseAuthRepository
import com.ekatayan.app.data.repository.SignUpResult
import com.google.gson.Gson
import dagger.Lazy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {
    @Test fun googleIdTokenRequestUsesSupabaseNativeSignInContract() {
        val json = Gson().toJson(
            GoogleIdTokenRequest(
                idToken = "google-id-token",
                nonce = "raw-nonce",
                data = GoogleUserMetadataRequest(
                    fullName = "Google Traveler",
                    avatarUrl = "https://example.com/avatar",
                ),
            ),
        )

        assertTrue(json.contains("\"provider\":\"google\""))
        assertTrue(json.contains("\"id_token\":\"google-id-token\""))
        assertTrue(json.contains("\"nonce\":\"raw-nonce\""))
        assertTrue(json.contains("\"full_name\":\"Google Traveler\""))
        assertTrue(json.contains("\"avatar_url\":\"https://example.com/avatar\""))
    }

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
        assertEquals("Traveler", session.currentUserName())
        assertEquals("access", UserSessionProvider(store).currentAccessToken())
        assertEquals("traveler@example.com", UserSessionProvider(store).currentUserEmail())
        assertEquals("Traveler", UserSessionProvider(store).currentUserName())
    }

    @Test fun googleIdTokenUsesTheSamePersistedSessionPath() = runTest {
        val store = AuthMemorySessionStore()
        val session = UserSessionProvider(store)
        val api = FakeSupabaseAuthApi()
        val repository = SupabaseAuthRepository(Lazy { api }, session)

        repository.signInWithGoogle("google-id-token", "raw-nonce", "Google Traveler", "https://example.com/avatar")

        assertEquals(GoogleIdTokenRequest("google", "google-id-token", "raw-nonce"), api.googleRequest)
        assertEquals("access", session.currentAccessToken())
        assertEquals("traveler@example.com", session.currentUserEmail())
        assertEquals("Google Traveler", session.currentUserName())
        assertEquals("access", UserSessionProvider(store).currentAccessToken())
    }

    @Test fun refreshReplacesPersistedSessionAndClearRemovesIt() = runTest {
        val store = AuthMemorySessionStore()
        val session = UserSessionProvider(store)
        session.setSession("old", "refresh", 1)
        val repository = SupabaseAuthRepository(Lazy { FakeSupabaseAuthApi() }, session)
        assertTrue(repository.refreshSession())
        assertEquals("access", session.currentAccessToken())
        assertEquals("Traveler", session.currentUserName())
        repository.clearSession()
        assertNull(UserSessionProvider(store).refreshToken())
    }

    @Test fun restoreUsesPersistedAccessTokenWithoutNetworkRequest() = runTest {
        val store = AuthMemorySessionStore()
        UserSessionProvider(store).setSession("access", "refresh", Long.MAX_VALUE, "traveler@example.com", "Traveler")
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
        assertEquals("Traveler", repository.currentUserName())
    }

    @Test fun authenticatedSignUpStoresTheSharedSession() = runTest {
        val store = AuthMemorySessionStore()
        val api = FakeSupabaseAuthApi(
            signUpResponse = SupabaseSessionDto(
                "access",
                "refresh",
                3600,
                null,
                SupabaseUserDto("user-id", "new@example.com"),
            ),
        )
        val session = UserSessionProvider(store)

        assertEquals(SignUpResult.AUTHENTICATED, SupabaseAuthRepository(Lazy { api }, session).signUp("New Traveler", "new@example.com", "+94771234567", "password"))
        assertEquals(
            PasswordSignUpRequest("new@example.com", "password", PasswordSignUpMetadata("New Traveler", "+94771234567")),
            api.signUpRequest,
        )
        assertEquals("access", session.currentAccessToken())
        assertEquals("New Traveler", session.currentUserName())
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
    override suspend fun updatePassword(authorization: String, request: com.ekatayan.app.data.remote.api.PasswordUpdateRequest): Map<String, Any?> = emptyMap()
    var request: PasswordSignInRequest? = null
    var signUpRequest: PasswordSignUpRequest? = null
    var googleRequest: GoogleIdTokenRequest? = null
    var refreshCalls = 0
    override suspend fun signInWithPassword(grantType: String, request: PasswordSignInRequest): SupabaseSessionDto {
        this.request = request
        return session()
    }
    override suspend fun signUpWithPassword(request: PasswordSignUpRequest): SupabaseSessionDto {
        signUpRequest = request
        return signUpResponse
    }
    override suspend fun signInWithIdToken(grantType: String, request: GoogleIdTokenRequest): SupabaseSessionDto {
        googleRequest = request.copy(data = null)
        return session()
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
            SupabaseUserDto("user-id", "traveler@example.com", SupabaseUserMetadataDto("Traveler")),
        )
    }
}
