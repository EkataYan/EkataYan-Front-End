package com.ekatayan.app.viewmodel

import com.ekatayan.app.TestProfileImageStore
import com.ekatayan.app.data.remote.AuthenticationRequiredException
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.ekatayan.app.data.remote.dto.ApiResponse
import com.ekatayan.app.data.remote.dto.ProfileDto
import com.ekatayan.app.data.remote.dto.UpdateProfileRequest
import com.ekatayan.app.data.remote.SessionStore
import com.ekatayan.app.data.remote.StoredSession
import com.ekatayan.app.data.remote.UserSessionProvider
import com.ekatayan.app.data.repository.ProfileFailure
import com.ekatayan.app.data.repository.ProfileRepository
import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.AuthenticationException
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @Test fun authenticationFailureCanBeRetriedAfterSignIn() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var signedIn = false
            val api = object : ProfileApiService {
                override suspend fun getProfile(): ApiResponse<ProfileDto> {
                    if (!signedIn) throw AuthenticationRequiredException()
                    return ApiResponse(true, ProfileDto("id", "Traveller", null, "Kandy", "en",
                        emptyList(), null, null, null))
                }
                override suspend fun updateProfile(request: UpdateProfileRequest): ApiResponse<ProfileDto> = error("Not used")
                override suspend fun uploadProfilePicture(file: okhttp3.MultipartBody.Part) = error("Not used")
                override suspend fun getProfilePicture() = error("Not used")
            }
            val session = UserSessionProvider(object : SessionStore {
                override fun read(): StoredSession? = null
                override fun save(session: StoredSession) = Unit
                override fun clear() = Unit
            }).apply {
                setSession("access", "refresh", Long.MAX_VALUE, "traveler@example.com", "Local Traveler")
            }
            val auth = object : AuthRepository {
                override suspend fun signIn(email: String, password: String) = Unit
                override suspend fun signUp(name: String, email: String, phone: String, password: String) = error("Not used by ProfileViewModel")
                override suspend fun restoreSession() = false
                override suspend fun refreshSession() = false
                override fun currentUserEmail() = session.currentUserEmail()
                override fun currentUserName() = session.currentUserName()
                override fun clearSession() = Unit
            }
            val vm = ProfileViewModel(ProfileRepository(Lazy { api }, session, imageStore = TestProfileImageStore()), auth)
            assertFalse(vm.uiState.value.isLoading)
            advanceUntilIdle()
            assertEquals(ProfileFailure.AUTHENTICATION, vm.uiState.value.error)
            assertEquals("Local Traveler", vm.uiState.value.profile?.name)
            assertFalse(vm.uiState.value.isLoading)
            signedIn = true
            vm.loadProfile()
            advanceUntilIdle()
            assertNull(vm.uiState.value.error)
            assertEquals("Traveller", vm.uiState.value.profile?.name)
            assertEquals("Traveller", vm.uiState.value.name)
            assertEquals("traveler@example.com", vm.uiState.value.email)
            assertFalse(vm.uiState.value.isLoading)

            signedIn = false
            vm.loadProfile()
            advanceUntilIdle()
            assertEquals(ProfileFailure.AUTHENTICATION, vm.uiState.value.error)
            assertEquals("Traveller", vm.uiState.value.profile?.name)
            assertEquals("Traveller", vm.uiState.value.name)
            assertFalse(vm.uiState.value.isLoading)

            vm.onRefreshErrorShown()
            assertNull(vm.uiState.value.error)
        } finally { Dispatchers.resetMain() }
    }
}
