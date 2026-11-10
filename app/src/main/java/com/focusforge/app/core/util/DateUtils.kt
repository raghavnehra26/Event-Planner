package com.focusforge.app.core.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun todayString(): String {
        return LocalDate.now().format(dateFormatter)
    }

    fun yesterdayString(): String {
        return LocalDate.now().minusDays(1).format(dateFormatter)
    }

    fun formatDate(localDate: LocalDate): String {
        return localDate.format(dateFormatter)
    }

    fun parseDate(dateStr: String): LocalDate {
        return LocalDate.parse(dateStr, dateFormatter)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr, dateFormatter)
            val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)
            date.format(displayFormatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatSecondsToTimer(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun getPastDays(daysCount: Int): List<String> {
        val today = LocalDate.now()
        val list = mutableListOf<String>()
        for (i in (daysCount - 1) downTo 0) {
            list.add(today.minusDays(i.toLong()).format(dateFormatter))
        }
        return list
    }
}
