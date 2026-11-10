package com.focusforge.app

import com.focusforge.app.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun testTodayString_formatIsCorrect() {
        val today = DateUtils.todayString()
        assertNotNull(today)
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testFormatSecondsToTimer() {
        assertEquals("25:00", DateUtils.formatSecondsToTimer(1500))
        assertEquals("05:00", DateUtils.formatSecondsToTimer(300))
        assertEquals("00:45", DateUtils.formatSecondsToTimer(45))
        assertEquals("00:00", DateUtils.formatSecondsToTimer(0))
    }

    @Test
    fun testGetPastDays_returnsExactCount() {
        val past7Days = DateUtils.getPastDays(7)
        assertEquals(7, past7Days.size)
        assertEquals(DateUtils.todayString(), past7Days.last())

        val past365Days = DateUtils.getPastDays(365)
        assertEquals(365, past365Days.size)
        assertEquals(DateUtils.todayString(), past365Days.last())
    }
}
