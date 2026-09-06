package com.ekatayan.app.feature.trips

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CreateTripRoute(onBackClick: () -> Unit, destination: String? = null, start: String? = null, end: String? = null, budget: String? = null, preferences: String? = null, viewModel: CreateTripViewModel = hiltViewModel()) {
    CreateTripScreen(onBackClick = onBackClick, initialDestination = destination ?: "", initialStart = start ?: "", initialEnd = end ?: "", initialBudget = budget ?: "", initialNotes = preferences?.let { "AI preferences: $it" } ?: "", onSave = { name, destinationValue, startDate, endDate, budgetValue, notes -> viewModel.save(name, destinationValue, startDate, endDate, budgetValue, notes); onBackClick() })
}
