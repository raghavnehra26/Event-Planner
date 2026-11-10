package com.focusforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusforge.app.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions ORDER BY completedTimestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY completedTimestamp DESC")
    suspend fun getAllSessionsSync(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE dateString = :dateString ORDER BY completedTimestamp DESC")
    fun getSessionsForDate(dateString: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    suspend fun getTotalSessionsCountSync(): Int

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE dateString = :dateString")
    fun getTodayFocusMinutes(dateString: String): Flow<Int?>

    @Query("SELECT dateString, SUM(durationMinutes) as count FROM focus_sessions GROUP BY dateString")
    fun getFocusMinutesByDate(): Flow<List<DateCountTuple>>
}
