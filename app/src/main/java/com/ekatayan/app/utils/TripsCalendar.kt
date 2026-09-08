package com.ekatayan.app.utils

import com.ekatayan.app.data.model.Trip

import com.ekatayan.app.R
import java.time.LocalDate
import java.time.YearMonth

fun Trip.statusFor(today: LocalDate): Int = when {
    today.isBefore(startDate) -> R.string.trip_status_upcoming
    today.isAfter(endDate) -> R.string.trip_status_past
    else -> R.string.trip_status_ongoing
}

internal fun calendarMonthGrid(month: YearMonth): List<LocalDate?> {
    val leadingEmptyDays = month.atDay(1).dayOfWeek.value - 1
    return List(42) { index ->
        val day = index - leadingEmptyDays + 1
        if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
    }
}
