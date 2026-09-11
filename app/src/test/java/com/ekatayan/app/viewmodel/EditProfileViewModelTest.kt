package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.remote.SessionStore
import com.ekatayan.app.data.remote.StoredSession
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.ekatayan.app.data.remote.dto.ApiResponse
import com.ekatayan.app.data.remote.dto.ProfileDto
import com.ekatayan.app.data.remote.dto.UpdateProfileRequest
import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.ProfileRepository
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {
    @Test
    fun loadsTracksChangesValidatesAndSavesReturnedProfile() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val api = EditProfileFakeApi()
            val session = UserSessionProvider(EditProfileMemorySessionStore()).apply {
                setSession("access", "refresh", Long.MAX_VALUE, "traveler@example.com", "Old Name")
            }
            val viewModel = EditProfileViewModel(
                repository = ProfileRepository(Lazy { api }, session),
                authRepository = EditProfileAuthRepository(session),
            )

            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals("Old Name", viewModel.uiState.value.name)
            assertEquals("traveler@example.com", viewModel.uiState.value.email)
            assertFalse(viewModel.uiState.value.isDirty)

            viewModel.save()
            advanceUntilIdle()
            assertEquals(0, api.updateCalls)

            viewModel.updatePhone("invalid")
            viewModel.save()
            assertEquals(EditProfileValidationError.PHONE_INVALID, viewModel.uiState.value.validationError)
            assertEquals(0, api.updateCalls)

            viewModel.updateName("New Name")
            viewModel.updatePhone("+94 77 123 4567")
            assertTrue(viewModel.uiState.value.isDirty)
            viewModel.save()
            advanceUntilIdle()

            assertEquals(1, api.updateCalls)
            assertEquals("New Name", api.lastUpdate?.displayName)
            assertEquals(listOf("nature", "food"), api.lastUpdate?.interests)
            assertTrue(viewModel.uiState.value.saved)
            assertFalse(viewModel.uiState.value.isDirty)
            assertEquals("New Name", session.currentUserName())
            assertNull(viewModel.uiState.value.error)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class EditProfileFakeApi : ProfileApiService {
    var updateCalls = 0
    var lastUpdate: UpdateProfileRequest? = null

    override suspend fun getProfile() = ApiResponse(
        success = true,
        data = profile("Old Name", "+94770000000"),
    )

    override suspend fun updateProfile(request: UpdateProfileRequest): ApiResponse<ProfileDto> {
        updateCalls++
        lastUpdate = request
        return ApiResponse(true, profile(request.displayName, request.phone))
    }

    private fun profile(name: String, phone: String) = ProfileDto(
        id = "user-id",
        displayName = name,
        bio = "Explorer",
        homeCity = "Kandy",
        language = "en",
        interests = listOf("nature", "food"),
        avatarPath = null,
        createdAt = null,
        updatedAt = null,
        phone = phone,
    )
}

private class EditProfileAuthRepository(
    private val session: UserSessionProvider,
) : AuthRepository {
    override suspend fun signIn(email: String, password: String) = Unit
    override suspend fun signUp(name: String, email: String, phone: String, password: String) = error("Not used")
    override suspend fun restoreSession() = true
    override suspend fun refreshSession() = false
    override fun currentUserEmail() = session.currentUserEmail()
    override fun currentUserName() = session.currentUserName()
    override fun clearSession() = session.clearSession()
}

private class EditProfileMemorySessionStore : SessionStore {
    private var session: StoredSession? = null
    override fun read() = session
    override fun save(session: StoredSession) { this.session = session }
    override fun clear() { session = null }
}
