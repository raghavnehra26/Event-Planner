package com.focusforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusforge.app.data.local.entity.DailyLogEntity
import com.focusforge.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0")
    suspend fun getAllActiveHabitsSync(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0")
    fun getActiveHabitsCount(): Flow<Int>

    // Daily log operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyLog(log: DailyLogEntity): Long

    @Query("DELETE FROM daily_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteDailyLog(habitId: Long, dateString: String)

    @Query("SELECT * FROM daily_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE dateString = :dateString")
    suspend fun getLogsForDateSync(dateString: String): List<DailyLogEntity>

    @Query("SELECT dateString FROM daily_logs WHERE habitId = :habitId")
    suspend fun getLogDatesForHabit(habitId: Long): List<String>

    @Query("SELECT dateString, COUNT(*) as count FROM daily_logs GROUP BY dateString")
    fun getAllLogCountsByDate(): Flow<List<DateCountTuple>>

    @Query("SELECT dateString, COUNT(*) as count FROM daily_logs WHERE dateString >= :startDate GROUP BY dateString")
    fun getLogCountsSinceDate(startDate: String): Flow<List<DateCountTuple>>

    @Query("SELECT * FROM daily_logs")
    suspend fun getAllLogsSync(): List<DailyLogEntity>
}

data class DateCountTuple(
    val dateString: String,
    val count: Int
)
