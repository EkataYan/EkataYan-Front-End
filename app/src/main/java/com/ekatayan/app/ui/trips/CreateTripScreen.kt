package com.ekatayan.app.ui.trips

import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.utils.dateFromPickerMillis
import com.ekatayan.app.utils.toPickerMillis
import com.ekatayan.app.utils.tripDateFormatter
import com.ekatayan.app.viewmodel.CreateTripField
import com.ekatayan.app.viewmodel.CreateTripUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import java.time.*
import java.time.format.*
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(onBackClick: () -> Unit, uiState: CreateTripUiState, onFieldChange: (CreateTripField, String) -> Unit, onDateSelected: (Boolean, LocalDate) -> Boolean, onSave: () -> Unit, modifier: Modifier = Modifier) {
    val name = uiState.name; val destination = uiState.destination
    val startText = uiState.startText; val endText = uiState.endText
    val budget = uiState.budget; val notes = uiState.notes
    var pickerForStart by rememberSaveable { mutableStateOf(true) }; var showPicker by rememberSaveable { mutableStateOf(false) }
    val error = uiState.errorRes?.let { stringResource(it) }
    val startDate = uiState.startDate; val endDate = uiState.endDate
    val colors = TextFieldDefaults.colors(
        focusedTextColor = EkataTextPrimary, unfocusedTextColor = EkataTextPrimary,
        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White, focusedIndicatorColor = EkataBlue,
        unfocusedIndicatorColor = EkataBlue, focusedLabelColor = EkataBlue,
        unfocusedLabelColor = EkataTextSecondary, cursorColor = EkataBlue,
    )
    Column(modifier.fillMaxSize().background(EkataBackground).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.create_trip_back)) }; Text(stringResource(R.string.create_trip_title), style = MaterialTheme.typography.headlineSmall) }
        Spacer(Modifier.height(22.dp))
        TripInput(name, { onFieldChange(CreateTripField.NAME, it) }, R.string.create_trip_name, colors); Spacer(Modifier.height(12.dp))
        TripInput(destination, { onFieldChange(CreateTripField.DESTINATION, it) }, R.string.create_trip_destination, colors); Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DateInput(startText, { onFieldChange(CreateTripField.START, it) }, R.string.create_trip_start_date, { pickerForStart = true; showPicker = true }, Modifier.weight(1f))
            DateInput(endText, { onFieldChange(CreateTripField.END, it) }, R.string.create_trip_end_date, { pickerForStart = false; showPicker = true }, Modifier.weight(1f))
        }
        if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            val duration = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
            Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = CardDefaults.cardColors(containerColor = EkataLightBlue), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = EkataBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp)); Column { Text(stringResource(R.string.trips_calendar_trip_dates), fontSize = 11.sp, color = EkataTextSecondary); Text(stringResource(R.string.trip_duration_dates, pluralStringResource(R.plurals.days_count,duration.toInt(),duration),startDate.format(tripDateFormatter),endDate.format(tripDateFormatter)), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EkataTextPrimary) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(budget, { onFieldChange(CreateTripField.BUDGET, it) }, label = { Text(stringResource(R.string.create_trip_budget)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(notes, { onFieldChange(CreateTripField.NOTES, it) }, label = { Text(stringResource(R.string.create_trip_notes)) }, minLines = 4, modifier = Modifier.fillMaxWidth(), colors = colors)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        uiState.operationError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.height(22.dp))
        Button(onClick = onSave, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(stringResource(R.string.create_trip_save))
        }
    }
    if (showPicker) {
        val current = if (pickerForStart) startDate else endDate
        val state = rememberDatePickerState(initialSelectedDateMillis = current?.toPickerMillis())
        MaterialTheme(colorScheme = lightColorScheme(primary = EkataBlue, surface = Color.White, onSurface = EkataTextPrimary)) {
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { selectedMillis ->
                            val selected = dateFromPickerMillis(selectedMillis)
                            if (onDateSelected(pickerForStart, selected)) showPicker = false
                        }
                    }) { Text(stringResource(R.string.create_trip_ok)) }
                },
                dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.create_trip_cancel)) } },
            ) { DatePicker(state) }
        }
    }
}

@Composable private fun TripInput(value: String, onValueChange: (String) -> Unit, label: Int, colors: TextFieldColors) { OutlinedTextField(value, onValueChange, label = { Text(stringResource(label)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = colors) }
@Composable private fun DateInput(value: String, onValueChange: (String) -> Unit, label: Int, onPickerClick: () -> Unit, modifier: Modifier) { OutlinedTextField(value, onValueChange, label = { Text(stringResource(label)) }, placeholder = { Text(stringResource(R.string.ui_25_sep_2026)) }, singleLine = true, modifier = modifier, trailingIcon = { IconButton(onClick = onPickerClick) { Icon(Icons.Outlined.CalendarMonth, stringResource(R.string.create_trip_choose_date)) } }, colors = TextFieldDefaults.colors(focusedTextColor = EkataTextPrimary, unfocusedTextColor = EkataTextPrimary, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = EkataBlue, unfocusedIndicatorColor = EkataBlue, focusedLabelColor = EkataBlue, unfocusedLabelColor = EkataTextSecondary, cursorColor = EkataBlue)) }
