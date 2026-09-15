package com.ekatayan.app.ui.trips

import com.ekatayan.app.viewmodel.CreateTripViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CreateTripRoute(onBackClick: () -> Unit, viewModel: CreateTripViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saveSucceeded) {
        if (state.saveSucceeded) {
            viewModel.consumeSaveSuccess()
            onBackClick()
        }
    }
    CreateTripScreen(onBackClick, state, viewModel::updateField, viewModel::selectDate,
        onSave = { viewModel.save() })
}
