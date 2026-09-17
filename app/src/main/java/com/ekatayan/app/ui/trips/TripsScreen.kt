package com.ekatayan.app.ui.trips

import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.utils.calendarMonthGrid
import com.ekatayan.app.utils.statusFor
import com.ekatayan.app.viewmodel.TripsUiState

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.component.EkataSectionHeading
import com.ekatayan.app.core.designsystem.component.EkataEmptyState
import com.ekatayan.app.core.designsystem.component.EkataErrorState
import com.ekatayan.app.core.designsystem.component.EkataLoadingState
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataBlueDark
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataRadius
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TripsScreen(
    uiState: TripsUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddTripClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onDeleteTrip: (Int) -> Unit = {},
    onTripClick: (Trip) -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var pendingDeleteTrip by remember { mutableStateOf<Trip?>(null) }
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = EkataSpacing.xxl * 2 + EkataSpacing.xl)) {
            item { TripsHeader(onAddTripClick, onNotificationClick, onSettingsClick, hasUnreadNotifications) }
            item {
                MonthCalendar(
                    month = uiState.displayedMonth,
                    today = uiState.today,
                    trips = uiState.trips,
                    onPreviousMonthClick = onPreviousMonthClick,
                    onNextMonthClick = onNextMonthClick,
                    selectedDate = uiState.selectedDate,
                    onDateClick = onDateClick,
                    modifier = Modifier.padding(horizontal = EkataSpacing.md, vertical = EkataSpacing.sm),
                )
            }
            item { TimelineHeader() }
            if (uiState.isLoading && uiState.trips.isEmpty()) {
                item { EkataLoadingState("Loading your trips") }
            } else if (uiState.errorMessage != null && uiState.trips.isEmpty()) {
                item { EkataErrorState("Couldn't load trips", uiState.errorMessage, actionLabel = "Retry", onAction = onRetry) }
            } else if (uiState.trips.isEmpty()) {
                item {
                    EkataEmptyState(
                        title = "No trips yet",
                        message = "Start planning your first Sri Lankan adventure.",
                    )
                }
            }
            listOf(R.string.trip_status_ongoing, R.string.trip_status_upcoming, R.string.trip_status_past).forEach { status ->
                val statusTrips = uiState.trips.filter { it.statusFor(uiState.today) == status }
                if (statusTrips.isNotEmpty()) {
                    item { TimelineStatusHeader(status) }
                    items(statusTrips, key = { it.id }) { trip ->
                        TripTimelineCard(trip, { onTripClick(trip) }, uiState.today, Modifier.padding(horizontal = EkataSpacing.md, vertical = EkataSpacing.xs / 2), { pendingDeleteTrip = trip })
                    }
                }
            }
        }
        AppBottomNavigation(
            selectedItem = AppBottomNavItem.TRIPS,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter),
            compact = true,
        )
    }
    pendingDeleteTrip?.let { trip ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTrip = null },
            title = { Text(stringResource(if (trip.canDelete) R.string.trips_delete_title else R.string.trips_leave_title)) },
            text = { Text(stringResource(if (trip.canDelete) R.string.trips_delete_message else R.string.trips_leave_message)) },
            confirmButton = { TextButton(onClick = { onDeleteTrip(trip.id); pendingDeleteTrip = null }) { Text(stringResource(if (trip.canDelete) R.string.trips_delete_action else R.string.trips_leave_action)) } },
            dismissButton = { TextButton(onClick = { pendingDeleteTrip = null }) { Text(stringResource(R.string.create_trip_cancel)) } },
        )
    }
}

@Composable
private fun TimelineStatusHeader(@StringRes status: Int) {
    val locale = LocalConfiguration.current.locales[0]
    Text(
        stringResource(status).uppercase(locale),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = EkataSpacing.md, top = EkataSpacing.sm, bottom = EkataSpacing.xxs),
    )
}

@Composable
private fun TripsHeader(onAddTripClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, hasUnreadNotifications: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = EkataSpacing.md, top = HeaderActionsTopPadding, end = EkataSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.trips_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(EkataSpacing.xs))
        Surface(
            onClick = onAddTripClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = EkataElevation.medium,
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, stringResource(R.string.trips_add), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(25.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        HeaderActions(onNotificationClick, onSettingsClick, hasUnreadNotifications)
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    today: LocalDate,
    trips: List<Trip>,
    selectedDate: LocalDate?,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(EkataLightBlue).padding(horizontal = EkataSpacing.sm, vertical = 10.dp),
            ) {
                CalendarMonthButton(Icons.Default.ChevronLeft, R.string.trips_previous_month, onPreviousMonthClick)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                }
                CalendarMonthButton(Icons.Default.ChevronRight, R.string.trips_next_month, onNextMonthClick)
            }
            Column(Modifier.padding(horizontal = EkataSpacing.sm, vertical = EkataSpacing.sm)) {
                CalendarWeekdayRow()
                Spacer(Modifier.height(EkataSpacing.xxs))
                CalendarDays(month, today, selectedDate, trips, onDateClick)
                Spacer(Modifier.height(EkataSpacing.sm))
                CalendarLegend()
            }
        }
    }
}

@Composable
private fun CalendarMonthButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    @StringRes contentDescription: Int,
    onClick: () -> Unit,
) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(icon, stringResource(contentDescription), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(23.dp))
        }
    }
}

@Composable
private fun CalendarWeekdayRow() {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        Modifier.fillMaxWidth().background(EkataLightBlue.copy(alpha = 0.65f), RoundedCornerShape(EkataRadius.medium)).padding(vertical = 7.dp),
    ) {
        DayOfWeek.entries.forEach { day ->
            Text(
                day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarDays(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate?,
    trips: List<Trip>,
    onDateClick: (LocalDate) -> Unit,
) {
    Column {
        calendarMonthGrid(month)
            .chunked(7)
            .dropLastWhile { week -> week.all { it == null } }
            .forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date -> CalendarDay(date, today, selectedDate, trips, onDateClick, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    today: LocalDate,
    selectedDate: LocalDate?,
    trips: List<Trip>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == today
    val trip = date?.let { day -> trips.firstOrNull { !day.isBefore(it.startDate) && !day.isAfter(it.endDate) } }
    val hasTrip = trip != null
    val isSelected = date != null && date == selectedDate
    val isStart = date != null && trip?.startDate == date
    val isEnd = date != null && trip?.endDate == date
    Box(
        modifier = modifier.height(40.dp).then(
            if (date != null) Modifier.clickable { onDateClick(date) } else Modifier
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            if (hasTrip) {
                val rangeShape = when {
                    isStart && isEnd -> RoundedCornerShape(50)
                    isStart -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                    isEnd -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                    else -> RoundedCornerShape(0.dp)
                }
                Box(Modifier.fillMaxWidth().height(30.dp).background(EkataLightBlue, rangeShape))
            }
            Box(
                modifier = Modifier.size(32.dp).then(
                    when {
                        isSelected -> Modifier.background(EkataBlue, CircleShape)
                        isToday -> Modifier.border(2.dp, EkataBlue, CircleShape)
                        else -> Modifier
                    }
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    date.dayOfMonth.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (hasTrip) EkataBlueDark else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = if (isToday || hasTrip || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
                if (isToday && !isSelected) {
                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(3.dp).background(EkataBlue, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth().background(EkataLightBlue.copy(alpha = 0.55f), RoundedCornerShape(EkataRadius.medium)).padding(horizontal = EkataSpacing.sm, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(EkataSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarLegendItem(stringResource(R.string.trips_calendar_selected), Modifier.background(EkataBlue, CircleShape), Modifier.weight(1f))
        CalendarLegendItem(stringResource(R.string.trips_calendar_trip_dates), Modifier.background(EkataLightBlue, RoundedCornerShape(50)), Modifier.weight(1f))
        CalendarLegendItem(stringResource(R.string.trips_calendar_today), Modifier.border(2.dp, EkataBlue, CircleShape), Modifier.weight(1f))
    }
}

@Composable
private fun CalendarLegendItem(label: String, markerModifier: Modifier, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(markerModifier.size(12.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TimelineHeader() {
    EkataSectionHeading(
        title = stringResource(R.string.trips_timeline),
        supportingText = stringResource(R.string.trips_timeline_subtitle),
        modifier = Modifier.padding(horizontal = EkataSpacing.md, vertical = EkataSpacing.xs),
    )
}

@Composable
fun TripTimelineCard(trip: Trip, onClick: () -> Unit, today: LocalDate = LocalDate.now(), modifier: Modifier = Modifier, onDeleteClick: () -> Unit = {}) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Row(Modifier.height(if (trip.source == "ai") 140.dp else 112.dp).padding(EkataSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(trip.imageRes),
                trip.customName ?: if (trip.nameRes != 0) stringResource(trip.nameRes) else "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(88.dp).clip(MaterialTheme.shapes.medium),
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(tripDateRange(trip.startDate, trip.endDate), color = EkataBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(trip.customName ?: stringResource(trip.nameRes), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(trip.customLocation ?: stringResource(trip.locationRes), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(7.dp))
                if (trip.source == "ai") {
                    val dayCount = trip.startDate.until(trip.endDate).days + 1
                    val travellerCount = trip.travellerCount ?: 1
                    Text(stringResource(R.string.trip_days_travellers,pluralStringResource(R.plurals.days_count,dayCount,dayCount),pluralStringResource(R.plurals.travellers_count,travellerCount,travellerCount)), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text(listOfNotNull(trip.travelStyle, trip.travelPace).joinToString(" • "), color = EkataBlue, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                }
                TripStatus(trip.statusFor(today))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, stringResource(R.string.trips_open_trip), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        if (trip.canDelete) Icons.Default.Delete else Icons.AutoMirrored.Filled.Logout,
                        stringResource(if (trip.canDelete) R.string.trips_delete_action else R.string.trips_leave_action),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun TripStatus(@StringRes statusRes: Int) {
    Text(
        stringResource(statusRes),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(horizontal = 9.dp, vertical = 3.dp),
    )
}

private fun tripDateRange(startDate: LocalDate, endDate: LocalDate): String {
    val locale = Locale.getDefault()
    val startPattern = if (startDate.year == endDate.year) "d MMM" else "d MMM yyyy"
    return "${startDate.format(DateTimeFormatter.ofPattern(startPattern, locale))} - ${endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
}
