package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.model.ProfileDetails
import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.ProfileFailure
import com.ekatayan.app.data.repository.ProfileLoadException
import com.ekatayan.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountSettingsState(
    val profile: ProfileDetails? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

@HiltViewModel
class SettingsDetailViewModel @Inject constructor(
    private val profiles: ProfileRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(AccountSettingsState(profile = profiles.currentProfile()))
    val state = mutable.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        mutable.value = mutable.value.copy(loading = true, error = null)
        runCatching { profiles.getProfile() }
            .onSuccess { mutable.value = AccountSettingsState(profile = it, loading = false) }
            .onFailure { mutable.value = mutable.value.copy(loading = false, error = "We couldn't load your account information.") }
    }

    fun saveAccount(name: String, username: String, phone: String) = viewModelScope.launch {
        val current = mutable.value.profile ?: return@launch
        mutable.value = mutable.value.copy(saving = true, error = null, message = null)
        try {
            val updated = profiles.updateProfile(current.copy(name = name.trim(), username = username.trim().removePrefix("@").lowercase(), phone = phone.trim()))
            mutable.value = mutable.value.copy(profile = updated, saving = false, message = "Account information updated.")
        } catch (e: ProfileLoadException) {
            val message = if (e.failure == ProfileFailure.USERNAME_TAKEN) "That username is already taken." else "We couldn't save your changes. Please try again."
            mutable.value = mutable.value.copy(saving = false, error = message)
        }
    }

    fun setDiscoverable(enabled: Boolean) = viewModelScope.launch {
        val current = mutable.value.profile ?: return@launch
        mutable.value = mutable.value.copy(saving = true, error = null)
        runCatching { profiles.updateProfile(current.copy(isDiscoverable = enabled)) }
            .onSuccess { mutable.value = mutable.value.copy(profile = it, saving = false, message = "Privacy preference updated.") }
            .onFailure { mutable.value = mutable.value.copy(saving = false, error = "We couldn't update your privacy preference.") }
    }

    fun updatePassword(password: String) = viewModelScope.launch {
        mutable.value = mutable.value.copy(saving = true, error = null, message = null)
        runCatching { auth.updatePassword(password) }
            .onSuccess { mutable.value = mutable.value.copy(saving = false, message = "Password updated successfully.") }
            .onFailure { mutable.value = mutable.value.copy(saving = false, error = "We couldn't update your password. Please sign in again and retry.") }
    }

    fun consumeMessage() { mutable.value = mutable.value.copy(message = null, error = null) }
}
