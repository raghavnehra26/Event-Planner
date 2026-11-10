package com.focusforge.app.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.focusforge.app.core.util.DateUtils
import com.focusforge.app.data.local.dao.HabitDao
import com.focusforge.app.data.preferences.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val habitDao: HabitDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationManager: FocusNotificationManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userSettings = preferencesRepository.userSettingsFlow.first()
        if (!userSettings.streakReminderEnabled) {
            return Result.success()
        }

        val today = DateUtils.todayString()
        val habits = habitDao.getAllActiveHabitsSync()
        if (habits.isEmpty()) {
            return Result.success()
        }

        val todayLogs = habitDao.getLogsForDateSync(today)
        val completedIds = todayLogs.map { it.habitId }.toSet()
        val pendingCount = habits.count { !completedIds.contains(it.id) }

        if (pendingCount > 0) {
            notificationManager.showStreakReminderNotification(
                title = "Maintain Your Streak 🔥",
                message = "You have $pendingCount habits pending today. Forge your momentum before midnight!"
            )
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "FocusForgeDailyStreakReminder"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(
                12, TimeUnit.HOURS,
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
