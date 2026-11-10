package com.focusforge.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val isFirstLaunch: Boolean = true,
    val isProUser: Boolean = false,
    val rewardedUnlockUntil: Long = 0L,
    val completedSessionsCount: Int = 0,
    val hasReviewedApp: Boolean = false,
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val streakReminderEnabled: Boolean = true
) {
    val isProActive: Boolean
        get() = isProUser || (rewardedUnlockUntil > System.currentTimeMillis())
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("key_first_launch")
        val KEY_IS_PRO = booleanPreferencesKey("key_is_pro")
        val KEY_REWARDED_UNLOCK_UNTIL = longPreferencesKey("key_rewarded_unlock_until")
        val KEY_COMPLETED_SESSIONS = intPreferencesKey("key_completed_sessions")
        val KEY_HAS_REVIEWED = booleanPreferencesKey("key_has_reviewed")
        val KEY_WORK_MINS = intPreferencesKey("key_work_mins")
        val KEY_SHORT_BREAK_MINS = intPreferencesKey("key_short_break_mins")
        val KEY_LONG_BREAK_MINS = intPreferencesKey("key_long_break_mins")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("key_sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("key_vibration_enabled")
        val KEY_STREAK_REMINDER_ENABLED = booleanPreferencesKey("key_streak_reminder_enabled")
    }

    val userSettingsFlow: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            isFirstLaunch = preferences[KEY_FIRST_LAUNCH] ?: true,
            isProUser = preferences[KEY_IS_PRO] ?: false,
            rewardedUnlockUntil = preferences[KEY_REWARDED_UNLOCK_UNTIL] ?: 0L,
            completedSessionsCount = preferences[KEY_COMPLETED_SESSIONS] ?: 0,
            hasReviewedApp = preferences[KEY_HAS_REVIEWED] ?: false,
            workMinutes = preferences[KEY_WORK_MINS] ?: 25,
            shortBreakMinutes = preferences[KEY_SHORT_BREAK_MINS] ?: 5,
            longBreakMinutes = preferences[KEY_LONG_BREAK_MINS] ?: 15,
            soundEnabled = preferences[KEY_SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[KEY_VIBRATION_ENABLED] ?: true,
            streakReminderEnabled = preferences[KEY_STREAK_REMINDER_ENABLED] ?: true
        )
    }

    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { it[KEY_FIRST_LAUNCH] = false }
    }

    suspend fun setProUser(isPro: Boolean) {
        dataStore.edit { it[KEY_IS_PRO] = isPro }
    }

    suspend fun grantRewardedProHours(hours: Int = 24) {
        val unlockUntil = System.currentTimeMillis() + (hours * 60 * 60 * 1000L)
        dataStore.edit { it[KEY_REWARDED_UNLOCK_UNTIL] = unlockUntil }
    }

    suspend fun incrementCompletedSessions(): Int {
        var newCount = 0
        dataStore.edit { preferences ->
            val current = preferences[KEY_COMPLETED_SESSIONS] ?: 0
            newCount = current + 1
            preferences[KEY_COMPLETED_SESSIONS] = newCount
        }
        return newCount
    }

    suspend fun setHasReviewedApp(reviewed: Boolean = true) {
        dataStore.edit { it[KEY_HAS_REVIEWED] = reviewed }
    }

    suspend fun updateTimerSettings(workMins: Int, shortBreakMins: Int, longBreakMins: Int) {
        dataStore.edit {
            it[KEY_WORK_MINS] = workMins
            it[KEY_SHORT_BREAK_MINS] = shortBreakMins
            it[KEY_LONG_BREAK_MINS] = longBreakMins
        }
    }

    suspend fun updateFeedbackSettings(sound: Boolean, vibration: Boolean) {
        dataStore.edit {
            it[KEY_SOUND_ENABLED] = sound
            it[KEY_VIBRATION_ENABLED] = vibration
        }
    }

    suspend fun updateStreakReminder(enabled: Boolean) {
        dataStore.edit { it[KEY_STREAK_REMINDER_ENABLED] = enabled }
    }
}
