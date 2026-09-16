package com.ekatayan.app.ui.trips

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.core.designsystem.component.EkataSecondaryButton
import com.ekatayan.app.data.model.*
import com.ekatayan.app.data.remote.api.PlannerPreviewRequest
import com.ekatayan.app.data.repository.SavedAiTripDetails
import com.ekatayan.app.utils.statusFor
import com.ekatayan.app.viewmodel.TripsViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun TripDetailsRoute(tripKey: String?, onBackClick: () -> Unit, onMembersClick: (String) -> Unit = {}, onExpensesClick: (String) -> Unit = {}, viewModel: TripsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val trip = state.trips.firstOrNull { it.remoteId == tripKey || it.id.toString() == tripKey }
    LaunchedEffect(trip?.remoteId) { trip?.let(viewModel::loadTripDetails) }
    val guide = if (trip?.source == "ai") null else trip?.let { viewModel.guideFor(it.customLocation ?: stringResource(it.locationRes)) }
    TripDetailsScreen(trip, state.today, guide, state.aiDetails, state.detailsLoading, state.detailsError, { trip?.let(viewModel::loadTripDetails) }, onBackClick, onMembersClick, onExpensesClick)
}

@Composable fun TripDetailsScreen(trip: Trip?, today: LocalDate, guide: DestinationGuide?, aiDetails: SavedAiTripDetails? = null,
    loading: Boolean = false, error: String? = null, onRetry: () -> Unit = {}, onBackClick: () -> Unit, onMembersClick: (String) -> Unit = {}, onExpensesClick: (String) -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(EkataBackground).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.create_trip_back)) }; Text(stringResource(R.string.trip_details_title), style = MaterialTheme.typography.headlineSmall) }
        if (trip == null) Text(stringResource(R.string.trip_details_missing), modifier = Modifier.padding(top = 24.dp)) else {
            Hero(trip, today)
            trip.remoteId?.let { id ->
                EkataSecondaryButton(stringResource(R.string.trip_members_title), { onMembersClick(id) }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
                EkataSecondaryButton(stringResource(R.string.notifications_filter_expenses), { onExpensesClick(id) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
            if (trip.source == "ai") when {
                aiDetails != null -> {
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
                    AiContent(trip, aiDetails)
                }
                loading -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> ErrorCard(error, onRetry)
                else -> ErrorCard(stringResource(R.string.saved_itinerary_unavailable), onRetry)
            }
            else ManualContent(trip, guide)
        }
    }
}

@Composable private fun Hero(trip: Trip, today: LocalDate) {
    val name = trip.customName ?: if (trip.nameRes != 0) stringResource(trip.nameRes) else stringResource(R.string.trip_details_title)
    val route = trip.route.takeIf(List<String>::isNotEmpty)?.joinToString(" → ") ?: trip.customLocation ?: if (trip.locationRes != 0) stringResource(trip.locationRes) else ""
    Spacer(Modifier.height(14.dp)); Image(painterResource(trip.imageRes), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(22.dp)))
    Spacer(Modifier.height(18.dp)); Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); if (route.isNotBlank()) Text(route, color = EkataTextSecondary, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp)); TripStatus(trip.statusFor(today))
    if (trip.source == "ai") { val days=trip.startDate.until(trip.endDate).days+1;val travellers=trip.travellerCount?:1;Spacer(Modifier.height(8.dp));Text(stringResource(R.string.trip_details_summary,dateRange(trip),pluralStringResource(R.plurals.days_count,days,days),trip.travellerType.orEmpty(),pluralStringResource(R.plurals.travellers_count,travellers,travellers)),color=EkataTextSecondary);Text(listOfNotNull(trip.travelStyle,trip.travelPace?.let{stringResource(R.string.pace_value,it)}).joinToString(" • "),color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold) }
}

@Composable private fun AiContent(trip: Trip, details: SavedAiTripDetails) {
    val itinerary = details.itinerary; val planner = details.planner
    if (itinerary.trip.summary.isNotBlank()) { Spacer(Modifier.height(18.dp)); Text(itinerary.trip.summary, style = MaterialTheme.typography.bodyLarge) }
    Section(stringResource(R.string.trip_overview)) { Line(stringResource(R.string.trip_details_dates), dateRange(trip)); Line(stringResource(R.string.duration), pluralStringResource(R.plurals.days_count,itinerary.trip.durationDays,itinerary.trip.durationDays)); Optional(stringResource(R.string.travellers), stringResource(R.string.traveller_summary,itinerary.trip.travellerType,itinerary.trip.travellerCount)); Optional(stringResource(R.string.travel_style), itinerary.trip.travelStyle); Optional(stringResource(R.string.pace), itinerary.trip.travelPace); Optional(stringResource(R.string.transport), planner?.transportPreferences?.joinToString(" • ")); Optional(stringResource(R.string.stay), planner?.accommodationPreference); Optional(stringResource(R.string.interests), planner?.interests?.joinToString(" • ")) }
    if (itinerary.trip.route.isNotEmpty()) Section(stringResource(R.string.your_route)) { itinerary.trip.route.forEachIndexed { index, value -> Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); if (index < itinerary.trip.route.lastIndex) Text("↓", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) } }
    Spacer(Modifier.height(22.dp)); Text(stringResource(R.string.ui_day_by_day_itinerary), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    itinerary.days.forEachIndexed { index, day -> Day(day, index == 0) }
    Costs(itinerary.costEstimate, itinerary.trip.travellerCount); Preferences(planner, itinerary)
    if (itinerary.recommendations.isNotEmpty()) Section(stringResource(R.string.trip_details_recommendations)) { itinerary.recommendations.forEach { Text("•  $it", modifier = Modifier.padding(vertical = 4.dp)) } }
}

@Composable private fun Day(day: ItineraryDay, open: Boolean) { var expanded by rememberSaveable(day.dayNumber) { mutableStateOf(open) }; Card(Modifier.fillMaxWidth().padding(top = 10.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = EkataCardBackground)) { Column { Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(stringResource(R.string.day_destination,day.dayNumber,day.destination), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge); Text(day.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text(formatDate(day.date), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall) }; Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null) }; if (expanded) Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) { Optional(null, day.summary); day.activities.forEach { Activity(it) } } } } }

@Composable private fun Activity(activity: ItineraryActivity) { Row(Modifier.fillMaxWidth().padding(vertical = 9.dp)) { Text(activity.startTime, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp)); Column(Modifier.weight(1f)) { Text(activity.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold); Optional(null, activity.location.name); Optional(null, listOfNotNull(activity.transportFromPrevious.takeIf(String::isNotBlank), activity.travelTimeMinutes.takeIf { it > 0 }?.let { stringResource(R.string.minutes_short,it) }).joinToString(" • ")); if (activity.durationMinutes > 0) Text(stringResource(R.string.minutes_short,activity.durationMinutes), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall); Optional(null, activity.description) } } }

@Composable private fun Costs(cost: CostEstimate, travellers: Int) { if (cost.total.max <= 0) return; Section(stringResource(R.string.estimated_trip_cost)) { Text(stringResource(R.string.ui_estimated_total), color = EkataTextSecondary); Text(money(cost.total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(pluralStringResource(R.plurals.for_travellers,travellers,travellers), color = EkataTextSecondary); listOf(stringResource(R.string.expense_category_accommodation) to cost.accommodation, stringResource(R.string.expense_category_transport) to cost.transport, stringResource(R.string.expense_category_food) to cost.food, stringResource(R.string.expense_category_activities) to cost.activities).filter { it.second.max > 0 }.forEach { Line(it.first, money(it.second)) }; Text(cost.disclaimer.ifBlank { stringResource(R.string.ai_estimate_disclaimer) }, color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)) } }
@Composable private fun Preferences(planner: PlannerPreviewRequest?, itinerary: Itinerary) { if (planner == null) return; Section(stringResource(R.string.trip_preferences)) { Optional(stringResource(R.string.travel_style), itinerary.trip.travelStyle); Optional(stringResource(R.string.pace), itinerary.trip.travelPace); Optional(stringResource(R.string.transport), planner.transportPreferences.joinToString(" • ")); Optional(stringResource(R.string.expense_category_accommodation), planner.accommodationPreference); Optional(stringResource(R.string.interests), planner.interests.joinToString(" • ")); Optional(stringResource(R.string.special_requests), planner.specialRequests) } }
@Composable private fun ManualContent(trip: Trip, guide: DestinationGuide?) { Section("") { Line(stringResource(R.string.trip_details_dates), dateRange(trip)); Line(stringResource(R.string.trip_details_budget), trip.budget ?: stringResource(R.string.trip_details_not_provided)); Line(stringResource(R.string.trip_details_notes), trip.notes ?: stringResource(R.string.trip_details_no_notes)) }; Spacer(Modifier.height(20.dp)); Text(stringResource(R.string.trip_details_recommendations), style = MaterialTheme.typography.titleLarge); Text(guide?.description.orEmpty(), color = EkataTextSecondary); guide?.places.orEmpty().forEach { Text("•  $it", modifier = Modifier.padding(vertical = 5.dp)) } }
@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) { Spacer(Modifier.height(18.dp)); if (title.isNotBlank()) Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(7.dp)); Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = EkataCardBackground)) { Column(Modifier.padding(18.dp), content = content) } }
@Composable private fun Line(label: String, value: String) { Column(Modifier.padding(vertical = 6.dp)) { Text(label, color = EkataTextSecondary, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.bodyLarge) } }
@Composable private fun Optional(label: String?, value: String?) { if (!value.isNullOrBlank() && value !in listOf("any", "Let AI decide")) { if (label == null) Text(value, color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall) else Line(label, value) } }
@Composable private fun ErrorCard(message: String, retry: () -> Unit) { Section("") { Text(message); TextButton(onClick = retry) { Text(stringResource(R.string.profile_retry)) } } }
private fun dateRange(trip: Trip): String { val f = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()); return "${trip.startDate.format(f)} – ${trip.endDate.format(f)}" }
private fun formatDate(value: String) = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())) }.getOrDefault(value)
private fun money(range: CostRange): String { val f = NumberFormat.getIntegerInstance(); return "LKR ${f.format(range.min)} – ${f.format(range.max)}" }
