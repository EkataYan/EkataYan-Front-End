package com.ekatayan.app.feature.trips

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataCardBackground
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
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
    modifier: Modifier = Modifier,
) {
    var pendingDeleteTrip by remember { mutableStateOf<Trip?>(null) }
    Box(modifier = modifier.fillMaxSize().background(EkataBackground)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item { TripsHeader(onAddTripClick, onNotificationClick, onSettingsClick) }
            item {
                MonthCalendar(
                    month = uiState.displayedMonth,
                    today = uiState.today,
                    trips = uiState.trips,
                    onPreviousMonthClick = onPreviousMonthClick,
                    onNextMonthClick = onNextMonthClick,
                    selectedDate = uiState.selectedDate,
                    onDateClick = onDateClick,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
            item { TimelineHeader() }
            listOf(R.string.trip_status_upcoming, R.string.trip_status_ongoing, R.string.trip_status_past).forEach { status ->
                val statusTrips = uiState.trips.filter { it.statusFor(uiState.today) == status }
                if (statusTrips.isNotEmpty()) {
                    item { TimelineStatusHeader(status) }
                    items(statusTrips, key = { it.id }) { trip ->
                        TripTimelineCard(trip, { onTripClick(trip) }, uiState.today, Modifier.padding(horizontal = 20.dp, vertical = 6.dp), { pendingDeleteTrip = trip })
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
        )
    }
    pendingDeleteTrip?.let { trip ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTrip = null },
            title = { Text(stringResource(R.string.trips_delete_title)) },
            text = { Text(stringResource(R.string.trips_delete_message)) },
            confirmButton = { TextButton(onClick = { onDeleteTrip(trip.id); pendingDeleteTrip = null }) { Text(stringResource(R.string.trips_delete_action)) } },
            dismissButton = { TextButton(onClick = { pendingDeleteTrip = null }) { Text(stringResource(R.string.create_trip_cancel)) } },
        )
    }
}

@Composable
private fun TimelineStatusHeader(@StringRes status: Int) {
    Text(stringResource(status), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = EkataTextPrimary, modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 2.dp))
}

@Composable
private fun TripsHeader(onAddTripClick: () -> Unit, onNotificationClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 21.dp, top = 47.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.trips_title), fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 27.sp, color = EkataTextPrimary)
        IconButton(onClick = onAddTripClick, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Default.Add, stringResource(R.string.trips_add), tint = EkataBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.weight(1f))
        HeaderActions(onNotificationClick, onSettingsClick)
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(containerColor = EkataLightBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onPreviousMonthClick, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.ChevronLeft, stringResource(R.string.trips_previous_month), tint = EkataTextPrimary)
                }
                Text(
                    month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNextMonthClick, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.ChevronRight, stringResource(R.string.trips_next_month), tint = EkataTextPrimary)
                }
            }
            Spacer(Modifier.height(8.dp))
            CalendarWeekdayRow()
            CalendarDays(month, today, selectedDate, trips, onDateClick)
        }
    }
}

@Composable
private fun CalendarWeekdayRow() {
    Row(Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { day ->
            Text(
                day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                color = EkataTextSecondary,
                fontSize = 11.sp,
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
    Column(Modifier.padding(top = 4.dp)) {
        calendarMonthGrid(month).chunked(7).forEach { week ->
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
        modifier = modifier.aspectRatio(1f).padding(2.dp).then(
            if (date != null) Modifier.clickable { onDateClick(date) } else Modifier
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            val background = when { isToday -> EkataBlue; isSelected -> Color.White; hasTrip -> Color.White.copy(alpha = 0.8f); else -> Color.Transparent }
            val shape = when { isStart && isEnd -> RoundedCornerShape(50); isStart -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp); isEnd -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp); hasTrip -> RoundedCornerShape(0.dp); else -> CircleShape }
            Text(
                date.dayOfMonth.toString(),
                color = if (isToday) Color.White else EkataTextPrimary,
                fontSize = 12.sp,
                fontWeight = if (isToday || hasTrip || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().background(background, shape).padding(vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun TimelineHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.trips_timeline), fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TripTimelineCard(trip: Trip, onClick: () -> Unit, today: LocalDate = LocalDate.now(), modifier: Modifier = Modifier, onDeleteClick: () -> Unit = {}) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EkataCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(Modifier.height(116.dp).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(trip.imageRes),
                trip.customName ?: if (trip.nameRes != 0) stringResource(trip.nameRes) else "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(94.dp).clip(RoundedCornerShape(14.dp)),
            )
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(tripDateRange(trip.startDate, trip.endDate), color = EkataBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(trip.customName ?: stringResource(trip.nameRes), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = EkataTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(trip.customLocation ?: stringResource(trip.locationRes), color = EkataTextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(7.dp))
                TripStatus(trip.statusFor(today))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.ArrowForwardIos, stringResource(R.string.trips_open_trip), tint = EkataTextSecondary, modifier = Modifier.size(15.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, stringResource(R.string.trips_delete_action), tint = EkataTextSecondary, modifier = Modifier.size(18.dp))
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
        color = EkataBlue,
        modifier = Modifier.background(EkataLightBlue, RoundedCornerShape(20.dp)).padding(horizontal = 9.dp, vertical = 3.dp),
    )
}

private fun tripDateRange(startDate: LocalDate, endDate: LocalDate): String {
    val locale = Locale.getDefault()
    val startPattern = if (startDate.year == endDate.year) "d MMM" else "d MMM yyyy"
    return "${startDate.format(DateTimeFormatter.ofPattern(startPattern, locale))} - ${endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
}
