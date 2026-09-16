package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExpensesTripSelectionTest {
    private val tripA = trip(1, "trip-a")
    private val tripB = trip(2, "trip-b")

    @Test
    fun explicitTripIdSelectsOnlyThatTrip() {
        assertEquals(tripB, selectedExpenseTrip(listOf(tripA, tripB), "trip-b"))
    }

    @Test
    fun missingTripIdDoesNotFallBackToFirstTrip() {
        assertNull(selectedExpenseTrip(listOf(tripA, tripB), null))
    }

    @Test
    fun unknownTripIdDoesNotFallBackToAnotherTrip() {
        assertNull(selectedExpenseTrip(listOf(tripA, tripB), "trip-c"))
    }

    private fun trip(localId: Int, remoteId: String) = Trip(
        id = localId,
        nameRes = 0,
        locationRes = 0,
        statusRes = 0,
        startDate = LocalDate.parse("2026-09-18"),
        endDate = LocalDate.parse("2026-09-21"),
        imageRes = 0,
        customName = remoteId,
        remoteId = remoteId,
    )
}
