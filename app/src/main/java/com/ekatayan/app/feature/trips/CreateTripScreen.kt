package com.ekatayan.app.feature.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import java.time.*
import java.time.format.*
import java.util.Locale

private val tripDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(onBackClick: () -> Unit, onSave: (String, String, LocalDate, LocalDate, String, String) -> Unit, modifier: Modifier = Modifier) {
    var name by rememberSaveable { mutableStateOf("") }; var destination by rememberSaveable { mutableStateOf("") }
    var startText by rememberSaveable { mutableStateOf("") }; var endText by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }; var notes by rememberSaveable { mutableStateOf("") }
    var pickerForStart by rememberSaveable { mutableStateOf(true) }; var showPicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val startDate = parseTripDate(startText); val endDate = parseTripDate(endText)
    val colors = TextFieldDefaults.colors(
        focusedTextColor = EkataTextPrimary, unfocusedTextColor = EkataTextPrimary,
        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White, focusedIndicatorColor = EkataBlue,
        unfocusedIndicatorColor = EkataBlue, focusedLabelColor = EkataBlue,
        unfocusedLabelColor = EkataTextSecondary, cursorColor = EkataBlue,
    )
    val nameRequired = stringResource(R.string.create_trip_name_required)
    val destinationRequired = stringResource(R.string.create_trip_destination_required)
    val datesRequired = stringResource(R.string.create_trip_dates_required)
    val dateInvalid = stringResource(R.string.create_trip_date_invalid)
    val dateRangeInvalid = stringResource(R.string.create_trip_date_range_invalid)
    val budgetInvalid = stringResource(R.string.create_trip_budget_invalid)
    Column(modifier.fillMaxSize().background(EkataBackground).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.create_trip_back)) }; Text(stringResource(R.string.create_trip_title), style = MaterialTheme.typography.headlineSmall) }
        Spacer(Modifier.height(22.dp))
        TripInput(name, { name = it; error = null }, R.string.create_trip_name, colors); Spacer(Modifier.height(12.dp))
        TripInput(destination, { destination = it; error = null }, R.string.create_trip_destination, colors); Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DateInput(startText, { startText = it; error = null }, R.string.create_trip_start_date, { pickerForStart = true; showPicker = true }, Modifier.weight(1f))
            DateInput(endText, { endText = it; error = null }, R.string.create_trip_end_date, { pickerForStart = false; showPicker = true }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(budget, { budget = it; error = null }, label = { Text(stringResource(R.string.create_trip_budget)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = colors)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(notes, { notes = it; error = null }, label = { Text(stringResource(R.string.create_trip_notes)) }, minLines = 4, modifier = Modifier.fillMaxWidth(), colors = colors)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }; Spacer(Modifier.height(22.dp))
        Button(onClick = {
            val validation = when { name.isBlank() -> nameRequired; destination.isBlank() -> destinationRequired; startDate == null || endDate == null -> if (startText.isNotBlank() || endText.isNotBlank()) dateInvalid else datesRequired; startDate != null && endDate != null && startDate.isAfter(endDate) -> dateRangeInvalid; budget.isNotBlank() && budget.toDoubleOrNull() == null -> budgetInvalid; else -> null }
            error = validation
            if (validation == null && startDate != null && endDate != null) onSave(name.trim(), destination.trim(), startDate, endDate, budget.trim(), notes.trim())
        }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.create_trip_save)) }
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
                            if (!pickerForStart && startDate != null && selected.isBefore(startDate)) {
                                error = dateRangeInvalid
                            } else {
                                if (pickerForStart) {
                                    startText = selected.format(tripDateFormatter)
                                    if (endDate != null && selected.isAfter(endDate)) endText = ""
                                } else {
                                    endText = selected.format(tripDateFormatter)
                                }
                                error = null
                                showPicker = false
                            }
                        }
                    }) { Text(stringResource(R.string.create_trip_ok)) }
                },
                dismissButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.create_trip_cancel)) } },
            ) { DatePicker(state) }
        }
    }
}

@Composable private fun TripInput(value: String, onValueChange: (String) -> Unit, label: Int, colors: TextFieldColors) { OutlinedTextField(value, onValueChange, label = { Text(stringResource(label)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = colors) }
@Composable private fun DateInput(value: String, onValueChange: (String) -> Unit, label: Int, onPickerClick: () -> Unit, modifier: Modifier) { OutlinedTextField(value, onValueChange, label = { Text(stringResource(label)) }, placeholder = { Text("25 Sep 2026") }, singleLine = true, modifier = modifier, trailingIcon = { IconButton(onClick = onPickerClick) { Icon(Icons.Outlined.CalendarMonth, stringResource(R.string.create_trip_choose_date)) } }, colors = TextFieldDefaults.colors(focusedTextColor = EkataTextPrimary, unfocusedTextColor = EkataTextPrimary, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = EkataBlue, unfocusedIndicatorColor = EkataBlue, focusedLabelColor = EkataBlue, unfocusedLabelColor = EkataTextSecondary, cursorColor = EkataBlue)) }
private fun parseTripDate(value: String): LocalDate? = try { LocalDate.parse(value.trim(), tripDateFormatter) } catch (_: DateTimeParseException) { null }
private fun LocalDate.toPickerMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
private fun dateFromPickerMillis(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
