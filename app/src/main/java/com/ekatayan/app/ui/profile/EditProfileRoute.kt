package com.ekatayan.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.updateAvatar(it.toString()) }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            delay(750)
            onSaved()
        }
    }
    EditProfileScreen(
        state = state, onBackClick = onBackClick, onRetry = viewModel::retry, onSave = viewModel::save,
        onNameChange = viewModel::updateName, onUsernameChange = viewModel::updateUsername, onBioChange = viewModel::updateBio,
        onHomeCityChange = viewModel::updateHomeCity, onLanguageChange = viewModel::updateLanguage,
        onInterestsChange = viewModel::updateInterests, onPhoneChange = viewModel::updatePhone,
        onChangePhoto = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    )
}
