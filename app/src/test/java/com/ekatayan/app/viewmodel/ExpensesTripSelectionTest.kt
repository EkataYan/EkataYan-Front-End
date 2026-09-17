package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.Trip
import java.math.BigDecimal
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

    @Test
    fun expenseTotalsPreserveTwoDecimalPrecision() {
        val total = sumMoneyAmounts(listOf("500.99", "0.01", "1000.50", "999999.99"))

        assertEquals(BigDecimal("1001501.49"), total)
    }

    @Test
    fun categoryPercentageUsesDecimalValuesWithoutTruncation() {
        assertEquals(33, expensePercentage(BigDecimal("0.01"), BigDecimal("0.03")))
        assertEquals(50, expensePercentage(BigDecimal("500.25"), BigDecimal("1000.50")))
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
