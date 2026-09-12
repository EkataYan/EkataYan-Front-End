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
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val error: ProfileFailure? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val initialProfile = repository.currentProfile()
    private val mutableState = MutableStateFlow(
        ProfileUiState(
            profile = initialProfile,
            name = initialProfile?.name.orEmpty().ifBlank { authRepository.currentUserName().orEmpty() },
            email = initialProfile?.email.orEmpty().ifBlank { authRepository.currentUserEmail().orEmpty() },
            isLoading = false,
        ),
    )
    val uiState = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            repository.profile.collect { profile ->
                if (profile != null) {
                    mutableState.value = mutableState.value.withLocalIdentity(
                        profile = profile,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
        loadProfile()
    }

    fun loadProfile() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            mutableState.value = mutableState.value.withLocalIdentity(
                isLoading = false,
                error = null,
            )
            try {
                mutableState.value = mutableState.value.withLocalIdentity(
                    profile = repository.getProfile(),
                    isLoading = false,
                    error = null,
                )
            } catch (e: ProfileLoadException) {
                if (e.failure == ProfileFailure.AUTHENTICATION && authRepository.refreshSession()) {
                    try {
                        mutableState.value = mutableState.value.withLocalIdentity(
                            profile = repository.getProfile(),
                            isLoading = false,
                            error = null,
                        )
                    } catch (retry: ProfileLoadException) {
                        mutableState.value = mutableState.value.withLocalIdentity(isLoading = false, error = retry.failure)
                    }
                } else {
                    mutableState.value = mutableState.value.withLocalIdentity(isLoading = false, error = e.failure)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableState.value = mutableState.value.withLocalIdentity(isLoading = false, error = ProfileFailure.SERVER)
            }
        }
    }

    fun onRefreshErrorShown() {
        mutableState.value = mutableState.value.copy(error = null)
    }

    fun logout() {
        authRepository.clearSession()
        repository.activateCurrentUser()
    }

    private fun ProfileUiState.withLocalIdentity(
        profile: ProfileDetails? = this.profile,
        isLoading: Boolean = this.isLoading,
        error: ProfileFailure? = this.error,
    ) = copy(
        profile = profile,
        name = authRepository.currentUserName().orEmpty().ifBlank { name },
        email = authRepository.currentUserEmail().orEmpty().ifBlank { email },
        isLoading = isLoading,
        error = error,
    )
}
