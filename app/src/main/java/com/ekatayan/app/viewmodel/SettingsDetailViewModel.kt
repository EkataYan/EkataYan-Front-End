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
import kotlinx.coroutines.CancellationException
import com.ekatayan.app.utils.runSuspendCatching
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider

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
    private val strings: StringResourceProvider,
) : ViewModel() {
    private val mutable = MutableStateFlow(AccountSettingsState(profile = profiles.currentProfile()))
    val state = mutable.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        mutable.value = mutable.value.copy(loading = true, error = null)
        runSuspendCatching { profiles.getProfile() }
            .onSuccess { mutable.value = AccountSettingsState(profile = it, loading = false) }
            .onFailure { mutable.value = mutable.value.copy(loading = false, error = strings[R.string.account_error_load]) }
    }

    fun saveAccount(name: String, username: String, phone: String) = viewModelScope.launch {
        if (mutable.value.saving) return@launch
        val current = mutable.value.profile ?: return@launch
        mutable.value = mutable.value.copy(saving = true, error = null, message = null)
        try {
            val updated = profiles.updateProfile(current.copy(name = name.trim(), username = username.trim().removePrefix("@").lowercase(), phone = phone.trim()))
            mutable.value = mutable.value.copy(profile = updated, saving = false, message = strings[R.string.account_updated])
        } catch (e: ProfileLoadException) {
            val message = if (e.failure == ProfileFailure.USERNAME_TAKEN) strings[R.string.account_error_username_taken] else strings[R.string.account_error_save]
            mutable.value = mutable.value.copy(saving = false, error = message)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            mutable.value = mutable.value.copy(saving = false, error = strings[R.string.account_error_save])
        }
    }

    fun setDiscoverable(enabled: Boolean) = viewModelScope.launch {
        if (mutable.value.saving) return@launch
        val current = mutable.value.profile ?: return@launch
        mutable.value = mutable.value.copy(saving = true, error = null)
        runSuspendCatching { profiles.updateProfile(current.copy(isDiscoverable = enabled)) }
            .onSuccess { mutable.value = mutable.value.copy(profile = it, saving = false, message = strings[R.string.privacy_updated]) }
            .onFailure { mutable.value = mutable.value.copy(saving = false, error = strings[R.string.privacy_error_update]) }
    }

    fun updatePassword(password: String) = viewModelScope.launch {
        if (mutable.value.saving) return@launch
        mutable.value = mutable.value.copy(saving = true, error = null, message = null)
        runSuspendCatching { auth.updatePassword(password) }
            .onSuccess { mutable.value = mutable.value.copy(saving = false, message = strings[R.string.password_updated]) }
            .onFailure { mutable.value = mutable.value.copy(saving = false, error = strings[R.string.password_error_update]) }
    }

    fun consumeMessage() { mutable.value = mutable.value.copy(message = null, error = null) }
}
