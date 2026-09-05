package com.ekatayan.app.feature.trips

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TripDetailsRoute(tripId: Int?, onBackClick: () -> Unit, viewModel: TripsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState(); val trip = state.trips.firstOrNull { it.id == tripId }
    Column(Modifier.fillMaxSize().background(EkataBackground).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.create_trip_back)) }; Text(stringResource(R.string.trip_details_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp)) }
        if (trip == null) { Text(stringResource(R.string.trip_details_missing), modifier = Modifier.padding(top = 24.dp)) } else {
            val tripName = if (trip.customName != null) trip.customName else stringResource(trip.nameRes)
            val destination = if (trip.customLocation != null) trip.customLocation else stringResource(trip.locationRes)
            Spacer(Modifier.height(18.dp)); Image(painterResource(trip.imageRes), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(22.dp)))
            Spacer(Modifier.height(18.dp)); Text(tripName ?: stringResource(R.string.trip_details_title), style = MaterialTheme.typography.headlineMedium); Text(destination ?: "", color = EkataTextSecondary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp)); TripStatus(trip.statusFor(state.today)); Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = EkataCardBackground), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) {
                DetailLine(stringResource(R.string.trip_details_dates), tripDateRangeFull(trip)); DetailLine(stringResource(R.string.trip_details_budget), trip.budget ?: stringResource(R.string.trip_details_not_provided)); DetailLine(stringResource(R.string.trip_details_notes), trip.notes ?: stringResource(R.string.trip_details_no_notes))
            } }
            Spacer(Modifier.height(24.dp)); Text(stringResource(R.string.trip_details_recommendations), style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(6.dp))
            val guide = guideFor(destination.orEmpty()); Text(guide.description, color = EkataTextSecondary); Spacer(Modifier.height(10.dp))
            guide.places.forEach { place -> Text("•  $place", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 5.dp)) }
        }
    }
}

@Composable private fun DetailLine(label: String, value: String) { Column(Modifier.padding(vertical = 7.dp)) { Text(label, color = EkataTextSecondary, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.bodyLarge) } }
private fun tripDateRangeFull(trip: Trip): String { val format = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()); return "${trip.startDate.format(format)} - ${trip.endDate.format(format)}" }
