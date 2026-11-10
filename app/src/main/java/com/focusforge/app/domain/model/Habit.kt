package com.focusforge.app.domain.model

data class Habit(
    val id: Long = 0,
    val name: String,
    val category: HabitCategory = HabitCategory.STUDY,
    val targetDaysPerWeek: Int = 7,
    val colorHex: String = "#00E676",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HabitCategory(val displayName: String, val defaultColor: String) {
    STUDY("Study & Learn", "#00E676"),
    CODING("Deep Coding", "#2979FF"),
    WORKOUT("Fitness & Health", "#FF3D00"),
    MINDFULNESS("Mindfulness", "#AB47BC"),
    OTHER("Productivity", "#FFAB00")
}
