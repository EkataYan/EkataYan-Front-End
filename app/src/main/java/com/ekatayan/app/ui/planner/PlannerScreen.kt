package com.ekatayan.app.ui.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.viewmodel.PlannerUiState
import com.ekatayan.app.viewmodel.TravellerType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val plannerDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val PlannerFieldBorder = Color(0xFFD9E0EA)
private val PlannerPopupBorder = Color(0xFFAEDCFA)
private val PlannerFieldShape = RoundedCornerShape(16.dp)
private val PlannerPopupShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    uiState: PlannerUiState,
    onDestinationChange: (String) -> Unit,
    onTravellerTypeSelected: (TravellerType) -> Unit,
    onPeopleCountChange: (String) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onDateValidationError: (String) -> Unit,
    onAskAiClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasUnreadNotifications: Boolean,
    modifier: Modifier = Modifier,
) {
    var travellerMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var pickerForStart by rememberSaveable { mutableStateOf(true) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val fieldColors = plannerFieldColors()

    Box(modifier.fillMaxSize().background(EkataBackground)) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 124.dp),
        ) {
            PlannerHeader(
                onNotificationClick = onNotificationClick,
                onSettingsClick = onSettingsClick,
                hasUnreadNotifications = hasUnreadNotifications,
            )

            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column {
                    OutlinedTextField(
                        value = uiState.destination,
                        onValueChange = onDestinationChange,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp),
                        placeholder = { Text(stringResource(R.string.planner_destination_label)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        shape = PlannerFieldShape,
                        colors = fieldColors,
                    )
                    Text(
                        text = stringResource(R.string.planner_destination_example),
                        color = EkataTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 2.dp, top = 6.dp),
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.planner_traveller_question),
                        color = EkataTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = travellerMenuExpanded,
                        onExpandedChange = { travellerMenuExpanded = !travellerMenuExpanded },
                    ) {
                        OutlinedTextField(
                            value = uiState.travellerType?.displayName().orEmpty(),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true,
                            ),
                            placeholder = { Text(stringResource(R.string.planner_select_option)) },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = travellerMenuExpanded)
                            },
                            readOnly = true,
                            singleLine = true,
                            shape = PlannerFieldShape,
                            colors = fieldColors,
                        )
                        ExposedDropdownMenu(
                            expanded = travellerMenuExpanded,
                            onDismissRequest = { travellerMenuExpanded = false },
                            modifier = Modifier.background(Color.White)
                                .border(1.dp, PlannerPopupBorder, PlannerPopupShape),
                            shape = PlannerPopupShape,
                            containerColor = Color.White,
                        ) {
                            TravellerType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(type.displayName(), color = EkataTextPrimary)
                                    },
                                    onClick = {
                                        onTravellerTypeSelected(type)
                                        travellerMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                if (uiState.requiresCustomPeopleCount) {
                    Column {
                        Text(
                            text = stringResource(R.string.planner_number_people),
                            color = EkataTextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.customPeopleCount,
                            onValueChange = onPeopleCountChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.planner_number_people_hint)) },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = PlannerFieldShape,
                            colors = fieldColors,
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.planner_travel_dates),
                        color = EkataTextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(12.dp))
                    PlannerDateField(
                        label = stringResource(R.string.planner_start_date),
                        value = uiState.startDate?.format(plannerDateFormat),
                        onClick = {
                            pickerForStart = true
                            showDatePicker = true
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    PlannerDateField(
                        label = stringResource(R.string.planner_end_date),
                        value = uiState.endDate?.format(plannerDateFormat),
                        onClick = {
                            pickerForStart = false
                            showDatePicker = true
                        },
                    )
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Button(
                    onClick = onAskAiClick,
                    modifier = Modifier.fillMaxWidth().height(78.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EkataLightBlue,
                        contentColor = Color(0xFF0C2461),
                    ),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.planner_ask_ai),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = stringResource(R.string.planner_ask_ai_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }

        AppBottomNavigation(
            selectedItem = AppBottomNavItem.PLANNER,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = {},
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter),
            compact = true,
        )
    }

    if (showDatePicker) {
        PlannerDatePickerDialog(
            initialDate = if (pickerForStart) uiState.startDate else uiState.endDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                if (!pickerForStart && uiState.startDate?.let(selectedDate::isBefore) == true) {
                    onDateValidationError("End date cannot be before the start date.")
                } else {
                    if (pickerForStart) onStartDateSelected(selectedDate) else onEndDateSelected(selectedDate)
                    showDatePicker = false
                }
            },
        )
    }
}

@Composable
private fun PlannerHeader(
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasUnreadNotifications: Boolean,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val headerHeight = (maxWidth * 0.67f).coerceIn(232.dp, 280.dp)
        Box(
            modifier = Modifier.fillMaxWidth().height(headerHeight)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.ai_planner_header),
                contentDescription = stringResource(R.string.planner_header_image_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterStart,
            )
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
                    .padding(start = 22.dp, end = 110.dp)
                    .widthIn(max = 245.dp),
            ) {
                Text(
                    text = stringResource(R.string.planner_header_title),
                    color = Color(0xFF10213C),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.planner_header_description),
                    color = Color(0xFF34445A),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            HeaderActions(
                onNotificationClick = onNotificationClick,
                onSettingsClick = onSettingsClick,
                hasUnreadNotifications = hasUnreadNotifications,
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(top = HeaderActionsTopPadding, end = 14.dp),
            )
        }
    }
}

@Composable
private fun PlannerDateField(label: String, value: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp),
        shape = PlannerFieldShape,
        color = Color.White,
        contentColor = EkataTextPrimary,
        border = BorderStroke(1.dp, PlannerFieldBorder),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = EkataTextSecondary)
            Text(
                text = value ?: label,
                color = if (value == null) EkataTextSecondary else EkataTextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = stringResource(R.string.planner_choose_date, label),
                tint = EkataTextSecondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlannerDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate?.toPickerMillis())
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = EkataBlue,
            surface = Color.White,
            onSurface = EkataTextPrimary,
        ),
    ) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(dateFromPickerMillis(it))
                        }
                    },
                ) {
                    Text(stringResource(R.string.planner_date_ok), color = EkataTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.planner_date_cancel), color = EkataTextPrimary)
                }
            },
            modifier = Modifier.border(1.dp, PlannerPopupBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = Color.White),
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun plannerFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedTextColor = EkataTextPrimary,
    unfocusedTextColor = EkataTextPrimary,
    focusedPlaceholderColor = EkataTextSecondary,
    unfocusedPlaceholderColor = EkataTextSecondary,
    focusedBorderColor = EkataBlue,
    unfocusedBorderColor = PlannerFieldBorder,
    cursorColor = EkataBlue,
)

@Composable
private fun TravellerType.displayName(): String = when (this) {
    TravellerType.SOLO -> stringResource(R.string.planner_solo)
    TravellerType.COUPLE -> stringResource(R.string.planner_couple)
    TravellerType.FAMILY -> stringResource(R.string.planner_family)
    TravellerType.FRIENDS -> stringResource(R.string.planner_friends)
}

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun dateFromPickerMillis(value: Long): LocalDate =
    Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate()
