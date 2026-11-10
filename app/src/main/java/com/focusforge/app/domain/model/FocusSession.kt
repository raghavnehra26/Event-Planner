package com.focusforge.app.domain.model

enum class SessionMode(val title: String) {
    POMODORO("Focus Block"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break"),
    CUSTOM("Deep Work")
}

data class FocusSession(
    val id: Long = 0,
    val habitId: Long? = null,
    val habitName: String? = null,
    val durationMinutes: Int,
    val mode: SessionMode = SessionMode.POMODORO,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val dateString: String
)
