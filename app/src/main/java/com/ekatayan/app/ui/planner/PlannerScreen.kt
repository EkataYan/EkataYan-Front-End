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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextAlign
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
import com.ekatayan.app.viewmodel.PlannerTravelStyle
import com.ekatayan.app.viewmodel.TravelPace
import com.ekatayan.app.viewmodel.PlannerGenerationState
import com.ekatayan.app.viewmodel.PlannerPhase
import com.ekatayan.app.viewmodel.PlannerFailedAction
import com.ekatayan.app.viewmodel.displayLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val plannerDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val PlannerFieldBorder = Color(0xFFD9E0EA)
private val PlannerPopupBorder = Color(0xFFAEDCFA)
private val PlannerDatePickerBackground = Color(0xFFF2F8FC)
private val PlannerFieldShape = RoundedCornerShape(16.dp)
private val PlannerPopupShape = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    uiState: PlannerUiState,
    onDestinationChange: (String) -> Unit,
    onAddDestination: () -> Unit,
    onAdditionalDestinationChange: (Int, String) -> Unit,
    onRemoveAdditionalDestination: (Int) -> Unit,
    onMoveDestination: (Int, Int) -> Unit,
    onTravellerTypeSelected: (TravellerType) -> Unit,
    onPeopleCountChange: (String) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onDateValidationError: (String) -> Unit,
    onToggleAiDestinations: () -> Unit,
    onToggleSuggestedPlaces: () -> Unit,
    onTogglePersonalization: () -> Unit,
    onToggleTransport: (String) -> Unit,
    onAccommodationSelected: (String) -> Unit,
    onTravelStyleSelected: (PlannerTravelStyle) -> Unit,
    onToggleInterest: (String) -> Unit,
    onPaceSelected: (TravelPace) -> Unit,
    onSpecialRequestsChange: (String) -> Unit,
    generationState: PlannerGenerationState,
    onAskAiClick: () -> Unit,
    onRetry: () -> Unit,
    onEditPreferences: () -> Unit,
    onRemoveActivity: (Int, Int) -> Unit,
    onMoveActivity: (Int, Int, Int) -> Unit,
    onMakeDayRelaxed: (Int) -> Unit,
    onSaveTrip: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasUnreadNotifications: Boolean,
    modifier: Modifier = Modifier,
) {
    if (generationState.phase != PlannerPhase.EDITING) {
        PlannerGenerationContent(
            generationState = generationState,
            onRetry = onRetry,
            onEditPreferences = onEditPreferences,
            onRemoveActivity = onRemoveActivity,
            onMoveActivity = onMoveActivity,
            onMakeDayRelaxed = onMakeDayRelaxed,
            onSaveTrip = onSaveTrip,
            onTripsClick = onTripsClick,
            modifier = modifier,
        )
        return
    }
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
                    Text(stringResource(R.string.ui_destination_route), style = MaterialTheme.typography.titleMedium, color = EkataTextPrimary)
                    Spacer(Modifier.height(8.dp))
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

                    uiState.additionalDestinations.forEachIndexed { index, destination ->
                        Text(
                            text = "${index + 2}. ${stringResource(R.string.planner_additional_place)}",
                            color = EkataTextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
                        )
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { onAdditionalDestinationChange(index, it) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp),
                            placeholder = { Text(stringResource(R.string.planner_additional_place)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { onMoveDestination(index + 1, -1) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.ArrowUpward, stringResource(R.string.move_destination_earlier), Modifier.size(20.dp)) }
                                    IconButton(onClick = { onMoveDestination(index + 1, 1) }, enabled = index < uiState.additionalDestinations.lastIndex, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.ArrowDownward, stringResource(R.string.move_destination_later), Modifier.size(20.dp)) }
                                    IconButton(onClick = { onRemoveAdditionalDestination(index) }, modifier = Modifier.size(48.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.planner_remove_place), tint = EkataTextSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = PlannerFieldShape,
                            colors = fieldColors,
                        )
                    }

                    TextButton(
                        onClick = onAddDestination,
                        modifier = Modifier.padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = EkataLightBlue,
                            contentColor = EkataTextPrimary,
                        ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "+ Add another destination",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlannerToggleAction("✨ Suggest places", uiState.suggestAdditionalPlaces, onToggleSuggestedPlaces, Modifier.weight(1f))
                        PlannerToggleAction("✨ Let AI choose", uiState.letAiChooseDestinations, onToggleAiDestinations, Modifier.weight(1f))
                    }
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
                            text = "Number of travellers",
                            color = EkataTextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IconButton(
                                onClick = {
                                    val current = uiState.customPeopleCount.toIntOrNull() ?: 1
                                    onPeopleCountChange((current - 1).coerceAtLeast(uiState.minimumPartySize).toString())
                                },
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.ui_decrease_travellers))
                            }
                            OutlinedTextField(
                                value = uiState.customPeopleCount,
                                onValueChange = onPeopleCountChange,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text(stringResource(R.string.planner_number_people_hint), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                                singleLine = true,
                                shape = PlannerFieldShape,
                                colors = fieldColors,
                            )
                            IconButton(
                                onClick = {
                                    val current = uiState.customPeopleCount.toIntOrNull() ?: (uiState.minimumPartySize - 1)
                                    onPeopleCountChange((current + 1).coerceAtMost(100).toString())
                                },
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.ui_increase_travellers))
                            }
                        }
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
                    if (uiState.startDate != null && uiState.endDate != null) {
                        val days = java.time.temporal.ChronoUnit.DAYS.between(uiState.startDate, uiState.endDate) + 1
                        Text("$days days • ${uiState.startDate.format(plannerDateFormat)} – ${uiState.endDate.format(plannerDateFormat)}", style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                PersonalizationSection(
                    state = uiState,
                    onToggleExpanded = onTogglePersonalization,
                    onToggleTransport = onToggleTransport,
                    onAccommodationSelected = onAccommodationSelected,
                    onTravelStyleSelected = onTravelStyleSelected,
                    onToggleInterest = onToggleInterest,
                    onPaceSelected = onPaceSelected,
                    onSpecialRequestsChange = onSpecialRequestsChange,
                    fieldColors = fieldColors,
                )

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
private fun PlannerToggleAction(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 2) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EkataLightBlue),
    )
}

@Composable
private fun PersonalizationSection(
    state: PlannerUiState,
    onToggleExpanded: () -> Unit,
    onToggleTransport: (String) -> Unit,
    onAccommodationSelected: (String) -> Unit,
    onTravelStyleSelected: (PlannerTravelStyle) -> Unit,
    onToggleInterest: (String) -> Unit,
    onPaceSelected: (TravelPace) -> Unit,
    onSpecialRequestsChange: (String) -> Unit,
    fieldColors: TextFieldColors,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, PlannerFieldBorder)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.ui_personalize_your_trip), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.ui_add_preferences_for_a_better_itinerary), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
                }
                IconButton(onClick = onToggleExpanded) { Icon(if (state.personalizationExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, stringResource(R.string.expand_preferences)) }
            }
            if (state.personalizationExpanded) {
                PreferenceGroup("Transport", listOf("Train", "Bus", "Car", "Tuk-tuk", "Taxi / Ride-hailing", "Rental vehicle", "Own vehicle", "Let AI decide"), state.transportPreferences, onToggleTransport)
                PreferenceGroup("Stay", listOf("Hotel", "Guesthouse", "Hostel", "Resort", "Homestay", "No accommodation needed", "Let AI decide"), setOf(state.accommodationPreference), onAccommodationSelected)
                Text(stringResource(R.string.ui_what_s_your_travel_style), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
                PlannerTravelStyle.entries.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { option -> PlannerToggleAction(option.displayLabel, state.travelStyle == option, { onTravelStyleSelected(option) }, Modifier.weight(1f)) } } }
                Text(stringResource(R.string.ui_budget_affordable_stays_local_food_and_public_transpor), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
                PreferenceGroup("Interests", listOf("Nature", "Beaches", "Adventure", "Culture", "Food", "Wildlife", "Photography", "Relaxation", "Shopping", "History", "Hiking", "Scenic routes"), state.interests, onToggleInterest)
                Text(stringResource(R.string.ui_how_do_you_like_to_travel), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TravelPace.entries.forEach { option -> PlannerToggleAction(option.displayLabel, state.pace == option, { onPaceSelected(option) }, Modifier.weight(1f)) } }
                Text(stringResource(R.string.ui_relaxed_more_free_time_balanced_activities_and_rest_pa), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
                OutlinedTextField(
                    value = state.specialRequests,
                    onValueChange = onSpecialRequestsChange,
                    label = { Text(stringResource(R.string.ui_anything_else)) },
                    placeholder = { Text(stringResource(R.string.ui_i_want_to_take_the_kandy_ella_train_and_visit_nine_arc)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = PlannerFieldShape,
                    colors = fieldColors,
                )
            }
        }
    }
}

@Composable
private fun PreferenceGroup(title: String, options: List<String>, selected: Set<String>, onSelect: (String) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
    options.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { option -> PlannerToggleAction(option, option in selected, { onSelect(option) }, Modifier.weight(1f)) }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlannerGenerationContent(
    generationState: PlannerGenerationState,
    onRetry: () -> Unit,
    onEditPreferences: () -> Unit,
    onRemoveActivity: (Int, Int) -> Unit,
    onMoveActivity: (Int, Int, Int) -> Unit,
    onMakeDayRelaxed: (Int) -> Unit,
    onSaveTrip: () -> Unit,
    onTripsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itinerary = generationState.itinerary
    Box(modifier.fillMaxSize().background(EkataBackground)) {
        when (generationState.phase) {
            PlannerPhase.GENERATING, PlannerPhase.SAVING -> Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = EkataBlue)
                Text(stringResource(if (generationState.phase == PlannerPhase.SAVING) R.string.saving_your_trip else R.string.planning_adventure), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 20.dp))
                Text(generationState.status, color = EkataTextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
            PlannerPhase.ERROR -> Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    when (generationState.failedAction) {
                        PlannerFailedAction.SAVE -> "We couldn't save your trip."
                        PlannerFailedAction.MODIFY_DAY -> "We couldn't update this day."
                        else -> "We couldn't finish your itinerary."
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(generationState.error.orEmpty(), color = EkataTextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = onRetry) { Text(stringResource(R.string.ui_try_again)) }; TextButton(onClick = onEditPreferences) { Text(stringResource(R.string.ui_edit_preferences)) } }
            }
            PlannerPhase.SAVED -> Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AutoAwesome, null, tint = EkataBlue, modifier = Modifier.size(48.dp))
                Text(stringResource(R.string.ui_trip_saved), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 12.dp))
                Text(stringResource(R.string.ui_your_itinerary_is_now_available_in_trips), color = EkataTextSecondary)
                Button(onClick = onTripsClick, modifier = Modifier.padding(top = 20.dp)) { Text(stringResource(R.string.ui_view_trips)) }
            }
            PlannerPhase.PREVIEW -> if (itinerary != null) Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                Text(itinerary.trip.title, style = MaterialTheme.typography.headlineSmall)
                Text(itinerary.trip.route.joinToString(" → "), style = MaterialTheme.typography.titleSmall, color = EkataBlue, modifier = Modifier.padding(top = 4.dp))
                Text(stringResource(R.string.itinerary_trip_summary,itinerary.trip.startDate,itinerary.trip.endDate,itinerary.trip.travellerCount,itinerary.trip.travellerType.lowercase()), color = EkataTextSecondary)
                Text(stringResource(R.string.style_and_pace,itinerary.trip.travelStyle,itinerary.trip.travelPace), style = MaterialTheme.typography.labelLarge, color = EkataBlue, modifier = Modifier.padding(top = 4.dp))
                Text(itinerary.trip.summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 16.dp))
                itinerary.days.forEachIndexed { dayIndex, day ->
                    Surface(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 1.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.day_title,day.dayNumber,day.title), style = MaterialTheme.typography.titleMedium)
                            Text(day.date, style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
                            Text(day.summary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            day.activities.forEachIndexed { activityIndex, activity ->
                                Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.Top) {
                                    Text(activity.startTime.take(5), style = MaterialTheme.typography.labelLarge, color = EkataBlue, modifier = Modifier.width(52.dp))
                                    Column(Modifier.weight(1f)) { Text(activity.name, style = MaterialTheme.typography.titleSmall); Text(activity.location.name, style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary); if (activity.transportFromPrevious.isNotBlank()) Text(stringResource(R.string.transport_minutes,activity.transportFromPrevious,activity.travelTimeMinutes), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary) }
                                    Column {
                                        IconButton(onClick = { onMoveActivity(dayIndex, activityIndex, -1) }, enabled = activityIndex > 0, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.ArrowUpward, stringResource(R.string.move_activity_earlier), Modifier.size(20.dp)) }
                                        IconButton(onClick = { onMoveActivity(dayIndex, activityIndex, 1) }, enabled = activityIndex < day.activities.lastIndex, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.ArrowDownward, stringResource(R.string.move_activity_later), Modifier.size(20.dp)) }
                                        IconButton(onClick = { onRemoveActivity(dayIndex, activityIndex) }, enabled = day.activities.size > 1, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.DeleteOutline, stringResource(R.string.remove_activity), Modifier.size(20.dp)) }
                                    }
                                }
                            }
                            TextButton(onClick = { onMakeDayRelaxed(dayIndex) }) { Text(stringResource(R.string.ui_make_this_day_more_relaxed)) }
                        }
                    }
                }
                Surface(shape = RoundedCornerShape(18.dp), color = EkataLightBlue) { Column(Modifier.fillMaxWidth().padding(16.dp)) { Text(stringResource(R.string.ui_estimated_trip_cost), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.estimated_total_range,itinerary.costEstimate.currency,"%,d".format(itinerary.costEstimate.total.min),"%,d".format(itinerary.costEstimate.total.max)), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp)); Text(itinerary.costEstimate.disclaimer, style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary) } }
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = onEditPreferences, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.ui_edit_preferences)) }; Button(onClick = onRetry, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.ui_regenerate)) } }
                Button(onClick = onSaveTrip, modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp)) { Text(stringResource(R.string.create_trip_save)) }
                Spacer(Modifier.height(24.dp))
            }
            else -> Unit
        }
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
            Text(
                text = value ?: label,
                color = if (value == null) EkataTextSecondary else EkataTextPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
            surface = PlannerDatePickerBackground,
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
            shape = RoundedCornerShape(24.dp),
            colors = DatePickerDefaults.colors(containerColor = PlannerDatePickerBackground),
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = PlannerDatePickerBackground,
                    titleContentColor = EkataTextPrimary,
                    headlineContentColor = EkataTextPrimary,
                    weekdayContentColor = EkataTextPrimary,
                    subheadContentColor = EkataTextPrimary,
                    navigationContentColor = EkataTextPrimary,
                    yearContentColor = EkataTextPrimary,
                    currentYearContentColor = EkataBlue,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = EkataBlue,
                    dayContentColor = EkataTextPrimary,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = EkataBlue,
                    todayContentColor = EkataBlue,
                    todayDateBorderColor = EkataBlue,
                ),
            )
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
    TravellerType.GROUP -> "Group"
}

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun dateFromPickerMillis(value: Long): LocalDate =
    Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC).toLocalDate()
