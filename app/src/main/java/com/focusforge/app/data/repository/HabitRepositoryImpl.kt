package com.focusforge.app.data.repository

import com.focusforge.app.core.util.DateUtils
import com.focusforge.app.core.util.Exporter
import com.focusforge.app.data.local.dao.FocusSessionDao
import com.focusforge.app.data.local.dao.HabitDao
import com.focusforge.app.data.local.entity.DailyLogEntity
import com.focusforge.app.data.local.entity.HabitEntity
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.domain.model.Habit
import com.focusforge.app.domain.model.HeatmapDay
import com.focusforge.app.domain.model.TierLimits
import com.focusforge.app.domain.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val focusSessionDao: FocusSessionDao,
    private val preferencesRepository: UserPreferencesRepository
) : HabitRepository {

    override fun getActiveHabits(): Flow<List<Habit>> {
        val today = DateUtils.todayString()
        return combine(
            habitDao.getAllActiveHabits(),
            habitDao.getLogsForDate(today)
        ) { entities, todayLogs ->
            val completedHabitIds = todayLogs.map { it.habitId }.toSet()
            entities.map { entity ->
                entity.toDomain(isCompletedToday = completedHabitIds.contains(entity.id))
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getActiveHabitsCount(): Flow<Int> {
        return habitDao.getActiveHabitsCount()
    }

    override suspend fun getHabitById(id: Long): Habit? = withContext(Dispatchers.IO) {
        val entity = habitDao.getHabitById(id) ?: return@withContext null
        val today = DateUtils.todayString()
        val todayLogs = habitDao.getLogsForDateSync(today)
        val isCompleted = todayLogs.any { it.habitId == id }
        entity.toDomain(isCompletedToday = isCompleted)
    }

    override suspend fun addHabit(habit: Habit): Result<Long> = withContext(Dispatchers.IO) {
        val userSettings = preferencesRepository.userSettingsFlow.first()
        val currentCount = habitDao.getAllActiveHabitsSync().size

        if (!userSettings.isProActive && currentCount >= TierLimits.MAX_FREE_ACTIVE_HABITS) {
            return@withContext Result.failure(
                IllegalStateException("Free limit reached: Upgrade to FocusForge Pro to track unlimited habits.")
            )
        }

        val id = habitDao.insertHabit(HabitEntity.fromDomain(habit))
        Result.success(id)
    }

    override suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.updateHabit(HabitEntity.fromDomain(habit))
    }

    override suspend fun deleteHabit(habit: Habit) = withContext(Dispatchers.IO) {
        habitDao.deleteHabit(HabitEntity.fromDomain(habit))
    }

    override suspend fun toggleHabitCompletion(habitId: Long, dateString: String): Boolean = withContext(Dispatchers.IO) {
        val existingLogs = habitDao.getLogsForDateSync(dateString)
        val isAlreadyCompleted = existingLogs.any { it.habitId == habitId }

        if (isAlreadyCompleted) {
            habitDao.deleteDailyLog(habitId, dateString)
        } else {
            habitDao.insertDailyLog(
                DailyLogEntity(
                    habitId = habitId,
                    dateString = dateString
                )
            )
            // Increment overall completed session streak metric for Play Review triggers
            preferencesRepository.incrementCompletedSessions()
        }

        // Recalculate streak for this habit
        recalculateHabitStreaks(habitId)

        !isAlreadyCompleted
    }

    private suspend fun recalculateHabitStreaks(habitId: Long) {
        val habitEntity = habitDao.getHabitById(habitId) ?: return
        val dateStrings = habitDao.getLogDatesForHabit(habitId)

        if (dateStrings.isEmpty()) {
            habitDao.updateHabit(habitEntity.copy(currentStreak = 0))
            return
        }

        val dates = dateStrings.mapNotNull {
            try {
                DateUtils.parseDate(it)
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()

        if (dates.isEmpty()) {
            habitDao.updateHabit(habitEntity.copy(currentStreak = 0))
            return
        }

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Calculate current streak
        var currentStreak = 0
        var checkDate = if (dates.contains(today)) today else yesterday

        while (dates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Calculate longest streak
        var maxStreak = 0
        var tempStreak = 0
        var previousDate: LocalDate? = null

        for (date in dates) {
            if (previousDate == null) {
                tempStreak = 1
            } else if (date == previousDate.plusDays(1)) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > maxStreak) {
                maxStreak = tempStreak
            }
            previousDate = date
        }

        val newLongest = maxOf(habitEntity.longestStreak, maxStreak, currentStreak)
        habitDao.updateHabit(
            habitEntity.copy(
                currentStreak = currentStreak,
                longestStreak = newLongest
            )
        )
    }

    override fun getHeatmapData(daysCount: Int): Flow<List<HeatmapDay>> {
        val pastDays = DateUtils.getPastDays(daysCount)
        val startDate = pastDays.first()

        return combine(
            habitDao.getLogCountsSinceDate(startDate),
            focusSessionDao.getFocusMinutesByDate()
        ) { habitTuples, focusTuples ->
            val habitMap = habitTuples.associate { it.dateString to it.count }
            val focusMap = focusTuples.associate { it.dateString to it.count }

            var maxActivity = 1
            val dayStats = pastDays.map { dateStr ->
                val hCount = habitMap[dateStr] ?: 0
                val fCount = (focusMap[dateStr] ?: 0) / 25 // 25 min = 1 activity point
                val totalActivity = hCount + fCount
                if (totalActivity > maxActivity) maxActivity = totalActivity
                dateStr to totalActivity
            }

            dayStats.map { (dateStr, totalActivity) ->
                val intensity = if (totalActivity == 0) 0f else (totalActivity.toFloat() / maxActivity).coerceIn(0.2f, 1.0f)
                HeatmapDay(
                    dateString = dateStr,
                    count = totalActivity,
                    intensity = intensity
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllActiveHabitsSync()
        val logs = habitDao.getAllLogsSync()
        val sessions = focusSessionDao.getAllSessionsSync()

        val exportPayload = mapOf(
            "app" to "FocusForge",
            "version" to "1.0.0",
            "exportedAt" to System.currentTimeMillis(),
            "habits" to habits,
            "dailyLogs" to logs,
            "focusSessions" to sessions
        )
        Exporter.toJson(exportPayload)
    }

    override suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val logs = habitDao.getAllLogsSync()
        val habitsMap = habitDao.getAllActiveHabitsSync().associateBy { it.id }

        val headers = listOf("Log ID", "Habit Name", "Category", "Date", "Completed Timestamp")
        val rows = logs.map { log ->
            val habit = habitsMap[log.habitId]
            listOf(
                log.id.toString(),
                habit?.name ?: "Unknown Habit",
                habit?.category ?: "OTHER",
                log.dateString,
                log.completedAt.toString()
            )
        }
        Exporter.toCsv(headers, rows)
    }
}
