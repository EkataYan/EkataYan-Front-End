package com.ekatayan.app.feature.trips

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CreateTripRoute(onBackClick: () -> Unit, viewModel: CreateTripViewModel = hiltViewModel()) {
    CreateTripScreen(onBackClick = onBackClick, onSave = { name, destination, start, end, budget, notes -> viewModel.save(name, destination, start, end, budget, notes); onBackClick() })
}
