package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.remote.AuthenticationRequiredException
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.ekatayan.app.data.remote.dto.ApiResponse
import com.ekatayan.app.data.remote.dto.ProfileDto
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
            }
            val auth = object : AuthRepository {
                override suspend fun signIn(email: String, password: String) = Unit
                override suspend fun signUp(name: String, email: String, phone: String, password: String) = error("Not used by ProfileViewModel")
                override suspend fun restoreSession() = false
                override suspend fun refreshSession() = false
                override fun currentUserEmail() = "traveler@example.com"
                override fun clearSession() = Unit
            }
            val vm = ProfileViewModel(ProfileRepository(Lazy { api }), auth)
            assertTrue(vm.uiState.value.isLoading)
            advanceUntilIdle()
            assertEquals(ProfileFailure.AUTHENTICATION, vm.uiState.value.error)
            assertNull(vm.uiState.value.profile)
            assertFalse(vm.uiState.value.isLoading)
            signedIn = true
            vm.loadProfile()
            advanceUntilIdle()
            assertNull(vm.uiState.value.error)
            assertEquals("Traveller", vm.uiState.value.profile?.name)
            assertEquals("traveler@example.com", vm.uiState.value.email)
            assertFalse(vm.uiState.value.isLoading)
        } finally { Dispatchers.resetMain() }
    }
}
