package com.ekatayan.app.ui.expenses

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.EkataEmptyState
import com.ekatayan.app.core.designsystem.component.EkataErrorState
import com.ekatayan.app.core.designsystem.component.EkataLoadingState
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.viewmodel.NotificationsUiState
import com.ekatayan.app.viewmodel.TripsUiState
import com.ekatayan.app.viewmodel.TripsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ExpensesTripSelectionRoute(
    onHome: () -> Unit,
    onTrips: () -> Unit,
    onPlanner: () -> Unit,
    onExpenses: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onNotifications: () -> Unit,
    notificationsUiState: StateFlow<NotificationsUiState>,
    onTripSelected: (String) -> Unit,
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications by notificationsUiState.collectAsStateWithLifecycle()
    ExpensesTripSelectionScreen(
        state = state,
        onTripSelected = onTripSelected,
        onRetry = viewModel::refreshTrips,
        onGoToTrips = onTrips,
        onHome = onHome,
        onTrips = onTrips,
        onPlanner = onPlanner,
        onExpenses = onExpenses,
        onProfile = onProfile,
        onSettings = onSettings,
        onNotifications = onNotifications,
        hasUnread = notifications.hasUnreadNotifications,
    )
}

@Composable
fun ExpensesTripSelectionScreen(
    state: TripsUiState,
    onTripSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onGoToTrips: () -> Unit,
    onHome: () -> Unit,
    onTrips: () -> Unit,
    onPlanner: () -> Unit,
    onExpenses: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onNotifications: () -> Unit,
    hasUnread: Boolean,
) {
    val trips = state.trips.filter { it.remoteId != null }
    Scaffold(
        containerColor = EkataBackground,
        bottomBar = { AppBottomNavigation(AppBottomNavItem.EXPENSES, onHome, onTrips, onPlanner, onExpenses, onProfile) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = EkataSpacing.pageHorizontal, vertical = EkataSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(EkataSpacing.sm),
        ) {
            item { ExpensesHeader(onNotifications, onSettings, hasUnread) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(EkataSpacing.xxs)) {
                    Text(stringResource(R.string.expenses_select_trip_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.expenses_select_trip_description), style = MaterialTheme.typography.bodyMedium, color = EkataTextSecondary)
                }
            }
            when {
                state.isLoading && trips.isEmpty() -> item { EkataLoadingState(stringResource(R.string.expenses_loading_trips)) }
                state.errorMessage != null -> item {
                    EkataErrorState(
                        title = stringResource(R.string.expenses_trips_load_error),
                        message = state.errorMessage,
                        actionLabel = stringResource(R.string.retry),
                        onAction = onRetry,
                    )
                }
                trips.isEmpty() -> item {
                    EkataEmptyState(
                        title = stringResource(R.string.expenses_no_trips),
                        message = stringResource(R.string.expenses_no_trips_message),
                        actionLabel = stringResource(R.string.expenses_go_to_trips),
                        onAction = onGoToTrips,
                    )
                }
                else -> items(trips, key = Trip::id) { trip ->
                    ExpensesTripCard(trip) { trip.remoteId?.let(onTripSelected) }
                }
            }
        }
    }
}

@Composable
private fun ExpensesTripCard(trip: Trip, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Row(Modifier.fillMaxWidth().padding(EkataSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(trip.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(78.dp).clip(MaterialTheme.shapes.medium),
            )
            Spacer(Modifier.width(EkataSpacing.sm))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = trip.customName ?: stringResource(R.string.trip_fallback_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                tripDestination(trip)?.let { destination ->
                    TripMetadata(Icons.Outlined.LocationOn, destination)
                }
                TripMetadata(Icons.Outlined.CalendarMonth, tripDateRange(trip.startDate, trip.endDate))
                trip.travellerCount?.takeIf { it > 0 }?.let { count ->
                    Text(
                        text = androidx.compose.ui.res.pluralStringResource(R.plurals.travellers_count, count, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = EkataTextSecondary,
                    )
                }
            }
            Spacer(Modifier.width(EkataSpacing.xs))
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                stringResource(R.string.expenses_open_trip),
                tint = EkataBlue,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun TripMetadata(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = EkataTextSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun tripDestination(trip: Trip): String? =
    trip.route.firstOrNull()?.takeIf(String::isNotBlank)
        ?: trip.customLocation?.takeIf(String::isNotBlank)

private fun tripDateRange(start: LocalDate, end: LocalDate): String {
    val locale = Locale.getDefault()
    val startPattern = if (start.year == end.year) "d MMM" else "d MMM yyyy"
    return "${start.format(DateTimeFormatter.ofPattern(startPattern, locale))} – ${end.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
}
