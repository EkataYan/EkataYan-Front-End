package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.Trip
import com.ekatayan.app.R
import java.time.LocalDate

fun initialTrips(today: LocalDate) = listOf(
        Trip(1, R.string.trip_galle_name, R.string.trip_galle_location, R.string.trip_status_upcoming, today.plusDays(3), today.plusDays(5), R.drawable.galle),
        Trip(2, R.string.trip_kandy_name, R.string.trip_kandy_location, R.string.trip_status_planned, today.plusDays(12), today.plusDays(15), R.drawable.kandy),
        Trip(3, R.string.trip_sigiriya_name, R.string.trip_sigiriya_location, R.string.trip_status_planned, today.plusDays(25), today.plusDays(27), R.drawable.sigiriya),
        Trip(4, R.string.trip_nuwara_eliya_name, R.string.trip_nuwara_eliya_location, R.string.trip_status_planned, today.plusDays(40), today.plusDays(43), R.drawable.nine_arch_bridge),
    )
