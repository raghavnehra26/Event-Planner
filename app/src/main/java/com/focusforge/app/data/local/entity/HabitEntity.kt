package com.focusforge.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.focusforge.app.domain.model.Habit
import com.focusforge.app.domain.model.HabitCategory

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val targetDaysPerWeek: Int,
    val colorHex: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(isCompletedToday: Boolean = false): Habit {
        val cat = try {
            HabitCategory.valueOf(category)
        } catch (e: Exception) {
            HabitCategory.OTHER
        }
        return Habit(
            id = id,
            name = name,
            category = cat,
            targetDaysPerWeek = targetDaysPerWeek,
            colorHex = colorHex,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            isCompletedToday = isCompletedToday,
            isArchived = isArchived,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(habit: Habit): HabitEntity {
            return HabitEntity(
                id = habit.id,
                name = habit.name,
                category = habit.category.name,
                targetDaysPerWeek = habit.targetDaysPerWeek,
                colorHex = habit.colorHex,
                currentStreak = habit.currentStreak,
                longestStreak = habit.longestStreak,
                isArchived = habit.isArchived,
                createdAt = habit.createdAt
            )
        }
    }
}
