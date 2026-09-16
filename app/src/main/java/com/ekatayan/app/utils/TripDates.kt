package com.ekatayan.app.utils

import java.time.*
import java.time.format.*
import java.util.Locale

val tripDateFormatter: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

fun parseTripDate(value: String): LocalDate? = try { LocalDate.parse(value.trim(), tripDateFormatter) } catch (_: DateTimeParseException) { null }
fun LocalDate.toPickerMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
fun dateFromPickerMillis(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
