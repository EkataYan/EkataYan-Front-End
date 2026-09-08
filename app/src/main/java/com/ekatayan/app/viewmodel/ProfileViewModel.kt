package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.ProfileRepository
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(repository: ProfileRepository) : ViewModel() {
    val uiState = repository.getProfile()
}
