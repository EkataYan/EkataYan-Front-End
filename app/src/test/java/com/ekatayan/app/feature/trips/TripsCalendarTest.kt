package com.ekatayan.app.feature.trips

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripsCalendarTest {
    @Test
    fun calendarMonthGrid_positionsDaysFromMondayAndIncludesLeapDay() {
        val grid = calendarMonthGrid(YearMonth.of(2024, 2))

        assertEquals(42, grid.size)
        assertNull(grid[0])
        assertNull(grid[2])
        assertEquals(LocalDate.of(2024, 2, 1), grid[3])
        assertEquals(LocalDate.of(2024, 2, 29), grid[31])
    }
}
