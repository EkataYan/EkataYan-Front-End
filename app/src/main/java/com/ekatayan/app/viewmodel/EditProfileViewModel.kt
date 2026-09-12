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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EditProfileValidationError {
    NAME_REQUIRED,
    NAME_TOO_LONG,
    BIO_TOO_LONG,
    CITY_TOO_LONG,
    LANGUAGE_INVALID,
    INTERESTS_INVALID,
    PHONE_INVALID,
}

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val homeCity: String = "",
    val language: String = "en",
    val interests: String = "",
    val phone: String = "",
    val avatarPath: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val error: ProfileFailure? = null,
    val validationError: EditProfileValidationError? = null,
    val saved: Boolean = false,
    val syncFailed: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        EditProfileUiState(email = authRepository.currentUserEmail().orEmpty()),
    )
    val uiState = mutableState.asStateFlow()
    private var originalProfile: ProfileDetails? = null

    init {
        val local = repository.currentProfile() ?: ProfileDetails(
            name = authRepository.currentUserName().orEmpty(),
            location = "",
            email = authRepository.currentUserEmail().orEmpty(),
            language = "en",
        )
        showProfile(local, repository.hasPendingChanges())
    }

    fun updateName(value: String) = edit { copy(name = value) }
    fun updateBio(value: String) = edit { copy(bio = value) }
    fun updateHomeCity(value: String) = edit { copy(homeCity = value) }
    fun updateLanguage(value: String) = edit { copy(language = value) }
    fun updateInterests(value: String) = edit { copy(interests = value) }
    fun updatePhone(value: String) = edit { copy(phone = value) }

    fun retry() = save()

    fun save() {
        val current = mutableState.value
        if (current.isSaving) return
        val validationError = validate(current)
        if (validationError != null) {
            mutableState.value = current.copy(validationError = validationError, error = null)
            return
        }

        val update = current.toProfileDetails()
        if (update.editableContentEquals(originalProfile) && !repository.hasPendingChanges()) {
            mutableState.value = current.copy(isDirty = false, validationError = null)
            return
        }

        viewModelScope.launch {
            mutableState.value = current.copy(
                isSaving = true, error = null, validationError = null, syncFailed = false,
            )
            try {
                val updated = repository.updateProfile(update)
                originalProfile = updated
                mutableState.value = mutableState.value.copy(
                    name = updated.name,
                    bio = updated.bio,
                    homeCity = updated.location,
                    language = updated.language,
                    interests = updated.interests.joinToString(", "),
                    phone = updated.phone,
                    avatarPath = updated.avatarPath,
                    isSaving = false,
                    isDirty = false,
                    saved = true,
                    syncFailed = false,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: ProfileLoadException) {
                mutableState.value = mutableState.value.copy(
                    isSaving = false,
                    isDirty = true,
                    error = e.failure,
                    syncFailed = true,
                )
            }
        }
    }

    private fun showProfile(profile: ProfileDetails, pendingSync: Boolean = false) {
        originalProfile = profile
        mutableState.value = EditProfileUiState(
            name = profile.name.ifBlank { authRepository.currentUserName().orEmpty() },
            email = authRepository.currentUserEmail().orEmpty().ifBlank { profile.email },
            bio = profile.bio,
            homeCity = profile.location,
            language = profile.language.ifBlank { "en" },
            interests = profile.interests.joinToString(", "),
            phone = profile.phone,
            avatarPath = profile.avatarPath,
            isLoading = false,
            isDirty = pendingSync,
            syncFailed = pendingSync,
        )
    }

    private inline fun edit(transform: EditProfileUiState.() -> EditProfileUiState) {
        val edited = mutableState.value.transform().copy(
            error = null,
            validationError = null,
            saved = false,
            syncFailed = false,
        )
        mutableState.value = edited.copy(isDirty = !edited.toProfileDetails().editableContentEquals(originalProfile))
    }

    private fun validate(state: EditProfileUiState): EditProfileValidationError? {
        val interests = state.parsedInterests()
        return when {
            state.name.trim().isEmpty() -> EditProfileValidationError.NAME_REQUIRED
            state.name.trim().length > 160 -> EditProfileValidationError.NAME_TOO_LONG
            state.bio.trim().length > 1_000 -> EditProfileValidationError.BIO_TOO_LONG
            state.homeCity.trim().length > 160 -> EditProfileValidationError.CITY_TOO_LONG
            state.language.trim() !in SUPPORTED_LANGUAGES -> EditProfileValidationError.LANGUAGE_INVALID
            interests.size > 30 || interests.any { it.length > 160 } -> EditProfileValidationError.INTERESTS_INVALID
            state.phone.isNotBlank() && !PHONE_PATTERN.matches(state.phone.trim()) -> EditProfileValidationError.PHONE_INVALID
            else -> null
        }
    }

    private fun EditProfileUiState.toProfileDetails() = ProfileDetails(
        name = name.trim(),
        location = homeCity.trim(),
        email = email,
        phone = phone.trim(),
        bio = bio.trim(),
        language = language.trim(),
        interests = parsedInterests(),
        avatarPath = avatarPath,
    )

    private fun EditProfileUiState.parsedInterests() = interests.split(',')
        .map(String::trim)
        .filter(String::isNotEmpty)

    private fun ProfileDetails.editableContentEquals(other: ProfileDetails?) = other != null &&
        name == other.name && location == other.location && phone == other.phone && bio == other.bio &&
        language == other.language && interests == other.interests

    private companion object {
        val SUPPORTED_LANGUAGES = setOf("en", "si", "ta")
        val PHONE_PATTERN = Regex("\\+?[0-9 ()-]{7,32}")
    }
}
