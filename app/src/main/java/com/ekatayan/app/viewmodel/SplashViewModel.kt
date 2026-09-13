package com.ekatayan.app.viewmodel

import androidx.lifecycle.ViewModel
import com.ekatayan.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    suspend fun restoreSession(): Boolean = authRepository.restoreSession()
}
