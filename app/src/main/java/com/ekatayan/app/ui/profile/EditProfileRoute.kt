package com.ekatayan.app.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.EditProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun EditProfileRoute(onBackClick: () -> Unit, onSaved: () -> Unit, viewModel: EditProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            delay(750)
            onSaved()
        }
    }
    EditProfileScreen(
        state = state, onBackClick = onBackClick, onRetry = viewModel::retry, onSave = viewModel::save,
        onNameChange = viewModel::updateName, onBioChange = viewModel::updateBio,
        onHomeCityChange = viewModel::updateHomeCity, onLanguageChange = viewModel::updateLanguage,
        onInterestsChange = viewModel::updateInterests, onPhoneChange = viewModel::updatePhone,
    )
}
