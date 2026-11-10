package com.focusforge.app.domain.repository

import com.focusforge.app.domain.model.Habit
import com.focusforge.app.domain.model.HeatmapDay
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getActiveHabits(): Flow<List<Habit>>
    fun getActiveHabitsCount(): Flow<Int>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun addHabit(habit: Habit): Result<Long>
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun toggleHabitCompletion(habitId: Long, dateString: String): Boolean
    fun getHeatmapData(daysCount: Int): Flow<List<HeatmapDay>>
    suspend fun exportDataAsJson(): String
    suspend fun exportDataAsCsv(): String
}
