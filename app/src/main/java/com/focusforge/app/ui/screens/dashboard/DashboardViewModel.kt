package com.focusforge.app.ui.screens.dashboard

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusforge.app.core.sound.SoundFeedbackManager
import com.focusforge.app.core.util.DateUtils
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.data.preferences.UserSettings
import com.focusforge.app.domain.model.Habit
import com.focusforge.app.domain.model.HabitCategory
import com.focusforge.app.domain.model.HeatmapDay
import com.focusforge.app.domain.model.TierLimits
import com.focusforge.app.domain.repository.FocusSessionRepository
import com.focusforge.app.domain.repository.HabitRepository
import com.focusforge.app.monetization.PlayReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardEvent {
    object ShowPaywallLimitReached : DashboardEvent()
    data class ShowToast(val message: String) : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val soundFeedbackManager: SoundFeedbackManager,
    private val playReviewManager: PlayReviewManager
) : ViewModel() {

    val habits: StateFlow<List<Habit>> = habitRepository.getActiveHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val heatmapDays: StateFlow<List<HeatmapDay>> = habitRepository.getHeatmapData(14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayFocusMinutes: StateFlow<Int> = focusSessionRepository.getTodayFocusMinutes(DateUtils.todayString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    fun toggleHabit(habitId: Long, activity: Activity) {
        viewModelScope.launch {
            val settings = userSettings.value
            val nowCompleted = habitRepository.toggleHabitCompletion(habitId, DateUtils.todayString())
            if (nowCompleted) {
                soundFeedbackManager.playHabitCheckTone(settings.soundEnabled)
                soundFeedbackManager.triggerHapticFeedback(settings.vibrationEnabled)
                playReviewManager.checkAndTriggerReview(activity)
            } else {
                soundFeedbackManager.triggerHapticFeedback(settings.vibrationEnabled)
            }
        }
    }

    fun addHabit(
        name: String,
        category: HabitCategory,
        colorHex: String,
        targetDays: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val isPro = userSettings.value.isProActive
            val currentCount = habits.value.size
            if (!isPro && currentCount >= TierLimits.MAX_FREE_ACTIVE_HABITS) {
                _events.emit(DashboardEvent.ShowPaywallLimitReached)
                return@launch
            }

            val result = habitRepository.addHabit(
                Habit(
                    name = name,
                    category = category,
                    colorHex = colorHex,
                    targetDaysPerWeek = targetDays
                )
            )

            if (result.isSuccess) {
                onSuccess()
            } else {
                _events.emit(DashboardEvent.ShowPaywallLimitReached)
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }
}
