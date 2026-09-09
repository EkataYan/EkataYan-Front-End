package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.ProfileRepository
import com.ekatayan.app.data.repository.AuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.model.ProfileDetails
import com.ekatayan.app.data.repository.ProfileFailure
import com.ekatayan.app.data.repository.ProfileLoadException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: ProfileDetails? = null,
    val email: String = "",
    val isLoading: Boolean = true,
    val error: ProfileFailure? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileUiState())
    val uiState = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init { loadProfile() }

    fun loadProfile() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            mutableState.value = ProfileUiState(email = authRepository.currentUserEmail().orEmpty())
            mutableState.value = try {
                ProfileUiState(profile = repository.getProfile(), email = authRepository.currentUserEmail().orEmpty(), isLoading = false)
            } catch (e: ProfileLoadException) {
                if (e.failure == ProfileFailure.AUTHENTICATION && authRepository.refreshSession()) {
                    try {
                        ProfileUiState(profile = repository.getProfile(), email = authRepository.currentUserEmail().orEmpty(), isLoading = false)
                    } catch (retry: ProfileLoadException) {
                        ProfileUiState(email = authRepository.currentUserEmail().orEmpty(), isLoading = false, error = retry.failure)
                    }
                } else ProfileUiState(email = authRepository.currentUserEmail().orEmpty(), isLoading = false, error = e.failure)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                ProfileUiState(email = authRepository.currentUserEmail().orEmpty(), isLoading = false, error = ProfileFailure.SERVER)
            }
        }
    }
}
