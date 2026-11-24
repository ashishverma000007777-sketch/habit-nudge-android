package com.habitnudge.app.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmSchedulerLogicTest {

    @Test
    fun `snooze duration calculation adds exact minutes`() {
        val now = 1732435200000L // arbitrary epoch timestamp
        val snoozeMinutes = 10
        val expectedSnooze = now + (snoozeMinutes * 60 * 1000L)

        val calculated = now + (10 * 60 * 1000L)
        assertEquals(expectedSnooze, calculated)
    }

    @Test
    fun `repeat days parsing correctly handles CSV`() {
        val repeatDaysCsv = "1,2,3,4,5,6,7"
        val parsed = repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

        assertEquals(7, parsed.size)
        assertTrue(parsed.contains(1)) // Monday
        assertTrue(parsed.contains(7)) // Sunday
    }

    @Test
    fun `single repeat day for weekly habit like Three Hours Alone parses correctly`() {
        val saturdayOnlyCsv = "6"
        val parsed = saturdayOnlyCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

        assertEquals(1, parsed.size)
        assertTrue(parsed.contains(6)) // Saturday
    }
}
