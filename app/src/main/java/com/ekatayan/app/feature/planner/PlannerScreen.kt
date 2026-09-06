package com.ekatayan.app.feature.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val plannerDateFormat =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    uiState: PlannerUiState,
    onDestinationChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onStartDateCleared: () -> Unit,
    onEndDateCleared: () -> Unit,
    onDateValidationError: (String) -> Unit,
    onPreferenceSelected: (PreferenceKind, String) -> Unit,
    onAskAiClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasUnreadNotifications: Boolean,
    modifier: Modifier = Modifier
) {
    var pickerForStart by rememberSaveable { mutableStateOf(true) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var startDateText by rememberSaveable { mutableStateOf(uiState.startDate?.format(plannerDateFormat).orEmpty()) }
    var endDateText by rememberSaveable { mutableStateOf(uiState.endDate?.format(plannerDateFormat).orEmpty()) }

    LaunchedEffect(uiState.startDate, uiState.endDate) {
        startDateText = uiState.startDate?.format(plannerDateFormat).orEmpty()
        endDateText = uiState.endDate?.format(plannerDateFormat).orEmpty()
    }

    var preferenceDialog by remember {
        mutableStateOf<PreferenceKind?>(null)
    }

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedTextColor = EkataTextPrimary,
        unfocusedTextColor = EkataTextPrimary,
        focusedPlaceholderColor = EkataTextSecondary,
        unfocusedPlaceholderColor = EkataTextSecondary,
        focusedBorderColor = EkataBlue,
        unfocusedBorderColor = Color(0xFFD9E0EA),
        focusedLabelColor = EkataBlue,
        unfocusedLabelColor = EkataTextSecondary,
        cursorColor = EkataBlue
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EkataBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 105.dp)
        ) {

            // ---------------------------------------------------------
            // HERO HEADER
            // ---------------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ai_planner_header),
                    contentDescription = "AI Planner travel illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.CenterStart
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 22.dp, end = 12.dp)
                        .widthIn(max = 232.dp)
                ) {
                    Text(
                        text = "✨ AI Planner",
                        color = Color(0xFF15202B),
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tell us your preferences\nand let EkataYan Create\nthe perfect trip for you.",
                        color = Color(0xFF26323D),
                        fontSize = 17.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                HeaderActions(
                    onNotificationClick = onNotificationClick,
                    onSettingsClick = onSettingsClick,
                    hasUnreadNotifications = hasUnreadNotifications,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 14.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------------------------------------------------
            // DESTINATION
            // ---------------------------------------------------------

            PlannerField(
                label = "Where to go?",
                value = uiState.destination,
                placeholder = "Search destination",
                icon = Icons.Default.Search,
                trailingIcon = Icons.Default.LocationOn,
                onChange = onDestinationChange,
                colors = fieldColors
            )

            Text(
                text = "Example: Ella, Sri Lanka",
                color = EkataTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 5.dp,
                    bottom = 12.dp
                )
            )

            // ---------------------------------------------------------
            // DATES
            // ---------------------------------------------------------

            TravelDatesFields(
                startDateText = startDateText,
                endDateText = endDateText,
                onStartDateTextChange = { value ->
                    startDateText = value
                    if (value.isBlank()) onStartDateCleared()
                    else parsePlannerDate(value)?.let(onStartDateSelected)
                },
                onEndDateTextChange = { value ->
                    endDateText = value
                    if (value.isBlank()) onEndDateCleared()
                    else parsePlannerDate(value)?.let(onEndDateSelected)
                },
                onStartClick = { pickerForStart = true; showPicker = true },
                onEndClick = { pickerForStart = false; showPicker = true },
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // BUDGET
            // ---------------------------------------------------------

            PlannerField(
                label = "Budget (LKR)",
                value = uiState.budget,
                placeholder = "Enter your budget",
                icon = null,
                onChange = onBudgetChange,
                colors = fieldColors,
                keyboard = KeyboardType.Number,
                prefix = "LKR "
            )

            Text(
                text = "Example: LKR 50,000",
                color = EkataTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 5.dp,
                    bottom = 18.dp
                )
            )

            // ---------------------------------------------------------
            // MORE PREFERENCES
            // ---------------------------------------------------------

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "More Preferences (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EkataTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand preferences",
                    tint = EkataTextSecondary
                )
            }

            if (expanded) {

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    PreferenceRow(
                        label = "Number of Travelers",
                        value = uiState.travelers
                    ) {
                        preferenceDialog = PreferenceKind.TRAVELERS
                    }

                    PreferenceRow(
                        label = "Accommodation Preference",
                        value = uiState.accommodation
                    ) {
                        preferenceDialog = PreferenceKind.ACCOMMODATION
                    }

                    PreferenceRow(
                        label = "Transport Preference",
                        value = uiState.transport
                    ) {
                        preferenceDialog = PreferenceKind.TRANSPORT
                    }

                    PreferenceRow(
                        label = "Trip Type",
                        value = uiState.tripType
                    ) {
                        preferenceDialog = PreferenceKind.TRIP_TYPE
                    }

                    PreferenceRow(
                        label = "Interests",
                        value = uiState.interests
                    ) {
                        preferenceDialog = PreferenceKind.INTERESTS
                    }
                }
            }

            // ---------------------------------------------------------
            // ERROR
            // ---------------------------------------------------------

            uiState.error?.let { errorMessage ->

                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------------------------------------------------
            // ASK AI BUTTON
            // ---------------------------------------------------------

            Button(
                onClick = {
                    val startDate = parsePlannerDate(startDateText)
                    val endDate = parsePlannerDate(endDateText)
                    when {
                        startDateText.isNotBlank() && startDate == null ->
                            onDateValidationError("Enter a valid start date (e.g. 25 Sep 2026).")
                        endDateText.isNotBlank() && endDate == null ->
                            onDateValidationError("Enter a valid end date (e.g. 25 Sep 2026).")
                        startDate != null && endDate != null && endDate.isBefore(startDate) ->
                            onDateValidationError("End date cannot be before the start date.")
                        else -> onAskAiClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(62.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EkataBlue
                )
            ) {

                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {

                    Text(
                        text = "Ask AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Generate my perfect itinerary",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // -------------------------------------------------------------
        // EXISTING BOTTOM NAVIGATION
        // -------------------------------------------------------------

        AppBottomNavigation(
            selectedItem = AppBottomNavItem.PLANNER,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = {},
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // =================================================================
    // DATE PICKER
    // =================================================================

    if (showPicker) {

        val currentDate = if (pickerForStart) uiState.startDate else uiState.endDate
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate?.toPickerMillis()
        )

        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = EkataBlue,
                surface = Color.White,
                onSurface = EkataTextPrimary
            )
        ) {
            DatePickerDialog(
                onDismissRequest = {
                    showPicker = false
                },

                confirmButton = {

                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val selectedDate = dateFromPickerMillis(selectedMillis)
                            if (!pickerForStart && uiState.startDate != null && selectedDate.isBefore(uiState.startDate)) {
                                onDateValidationError("End date cannot be before the start date.")
                            } else {
                                if (pickerForStart) {
                                    onStartDateSelected(selectedDate)
                                    startDateText = selectedDate.format(plannerDateFormat)
                                } else {
                                    onEndDateSelected(selectedDate)
                                    endDateText = selectedDate.format(plannerDateFormat)
                                }
                                showPicker = false
                            }
                        }
                    }
                ) {
                    Text("OK")
                }
            },

                dismissButton = {

                TextButton(
                    onClick = {
                        showPicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
            ) {
                DatePicker(datePickerState)
            }
        }
    }

    // =================================================================
    // PREFERENCE DIALOG
    // =================================================================

    preferenceDialog?.let { kind ->

        val options = when (kind) {

            PreferenceKind.TRAVELERS -> listOf(
                "1 traveler",
                "2 travelers",
                "3 travelers",
                "4+ travelers"
            )

            PreferenceKind.ACCOMMODATION -> listOf(
                "Hotels",
                "Guest Houses",
                "Villa",
                "Camping",
                "Resorts"
            )

            PreferenceKind.TRANSPORT -> listOf(
                "Public Bus",
                "Train",
                "Highway",
                "Rental Vehicles",
                "Private Driver"
            )

            PreferenceKind.TRIP_TYPE -> listOf(
                "A balanced Trip",
                "Romantic Trip",
                "Solo Trip",
                "Group Trip",
                "Relaxing Getaway",
                "Family",
                "Cultural Escape"
            )

            PreferenceKind.INTERESTS -> listOf(
                "Nature Trip",
                "Culture Trip",
                "Beaches",
                "Food & cafés",
                "Adventure",
                "Hiking",
                "Wildlife"
            )

            else -> emptyList()
        }

        val dialogTitle = when (kind) {

            PreferenceKind.TRAVELERS ->
                "Choose number of travelers"

            PreferenceKind.ACCOMMODATION ->
                "Choose accommodation"

            PreferenceKind.TRANSPORT ->
                "Choose transport"

            PreferenceKind.TRIP_TYPE ->
                "Choose trip type"

            PreferenceKind.INTERESTS ->
                "Choose interests"

            else ->
                "Choose preference"
        }

        var customValue by rememberSaveable(kind) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                preferenceDialog = null
            },

            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.border(
                BorderStroke(1.dp, Color(0xFFE3E8EF)),
                RoundedCornerShape(24.dp)
            ),

            title = {
                Text(dialogTitle)
            },

            text = {

                Column {

                    options.forEach { option ->

                        Text(
                            text = option,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    onPreferenceSelected(
                                        kind,
                                        option
                                    )

                                    preferenceDialog = null
                                }
                                .padding(vertical = 13.dp),
                            color = EkataTextPrimary
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFE3E8EF)
                    )

                    Text(
                        text = "Other preference",
                        color = EkataTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customValue,
                        onValueChange = { customValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your own preference") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = EkataBlue,
                            unfocusedBorderColor = Color(0xFFD9E0EA),
                            focusedTextColor = EkataTextPrimary,
                            unfocusedTextColor = EkataTextPrimary,
                            cursorColor = EkataBlue
                        )
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val value = customValue.trim()
                        if (value.isNotEmpty()) {
                            onPreferenceSelected(kind, value)
                            preferenceDialog = null
                        }
                    },
                    enabled = customValue.isNotBlank()
                ) {
                    Text("Save")
                }
            },

            dismissButton = {
                TextButton(onClick = { preferenceDialog = null }) {
                    Text("Cancel")
                }
            },

        )
    }
}

// =====================================================================
// PLANNER TEXT FIELD
// =====================================================================

@Composable
private fun PlannerField(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector?,
    onChange: (String) -> Unit,
    colors: TextFieldColors,
    keyboard: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    trailingIcon: ImageVector? = null
) {

    OutlinedTextField(
        value = value,
        onValueChange = onChange,

        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),

        label = {
            Text(label)
        },

        placeholder = {
            Text(placeholder)
        },

        leadingIcon = icon?.let { imageVector ->

            {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null
                )
            }
        },

        prefix = prefix?.let { text ->

            {
                Text(
                    text = text,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },

        trailingIcon = trailingIcon?.let { imageVector ->
            {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        },

        singleLine = true,

        keyboardOptions = KeyboardOptions(
            keyboardType = keyboard
        ),

        shape = RoundedCornerShape(14.dp),

        colors = colors
    )
}

// =====================================================================
// DATE FIELD
// =====================================================================

@Composable
private fun TravelDatesFields(
    startDateText: String,
    endDateText: String,
    onStartDateTextChange: (String) -> Unit,
    onEndDateTextChange: (String) -> Unit,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    colors: TextFieldColors,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Travel Dates",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = EkataTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        PlannerDateField(
            label = "Start Date",
            value = startDateText,
            placeholder = "Select start date",
            onValueChange = onStartDateTextChange,
            onClick = onStartClick,
            colors = colors
        )
        Spacer(modifier = Modifier.height(10.dp))
        PlannerDateField(
            label = "End Date",
            value = endDateText,
            placeholder = "Select end date",
            onValueChange = onEndDateTextChange,
            onClick = onEndClick,
            colors = colors
        )
    }
    /*
    val value = if (startDate != null && endDate != null) {
        "${startDate.format(plannerDateFormat)} – ${endDate.format(plannerDateFormat)}"
    } else {
        ""
    }

    Box(modifier = modifier.clickable(onClick = onClick)) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Travel Dates") },
            placeholder = { Text("Select your travel dates") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Choose travel dates"
                )
            },
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = colors
        )
    }
    */
}

@Composable
private fun PlannerDateField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit,
    colors: TextFieldColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Choose $label"
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = colors
    )
}

private fun parsePlannerDate(value: String): LocalDate? = try {
    LocalDate.parse(value.trim(), plannerDateFormat)
} catch (_: DateTimeParseException) {
    null
}

@Composable
private fun DateRangeSelectionLabel(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF7F9FC),
        border = BorderStroke(1.dp, Color(0xFFE3E8EF))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EkataBlue
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = if (value.startsWith("Select")) {
                    EkataTextSecondary
                } else {
                    EkataTextPrimary
                }
            )
        }
    }
}

// =====================================================================
// PREFERENCE ROW
// =====================================================================

@Composable
private fun PreferenceRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            Color(0xFFE3E8EF)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = EkataTextSecondary
                )

                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = EkataTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = EkataTextSecondary
            )
        }
    }
}

// =====================================================================
// DATE HELPERS
// =====================================================================

private fun LocalDate.toPickerMillis(): Long {

    return atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}

private fun dateFromPickerMillis(value: Long): LocalDate {

    return Instant
        .ofEpochMilli(value)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}
