package com.focusforge.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.domain.model.Habit
import com.focusforge.app.domain.model.HabitCategory
import com.focusforge.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalPreset(
    val title: String,
    val category: HabitCategory,
    val colorHex: String
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _selectedPreset = MutableStateFlow<GoalPreset?>(null)
    val selectedPreset: StateFlow<GoalPreset?> = _selectedPreset.asStateFlow()

    val goalPresets = listOf(
        GoalPreset("Deep Work 90 Mins", HabitCategory.CODING, "#2979FF"),
        GoalPreset("Daily 45-min Workout", HabitCategory.WORKOUT, "#FF3D00"),
        GoalPreset("Read 30 Mins Daily", HabitCategory.STUDY, "#00E676"),
        GoalPreset("Mindful Meditation", HabitCategory.MINDFULNESS, "#AB47BC")
    )

    init {
        _selectedPreset.value = goalPresets.first()
    }

    fun setPage(page: Int) {
        _currentPage.value = page
    }

    fun selectPreset(preset: GoalPreset) {
        _selectedPreset.value = preset
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val preset = _selectedPreset.value ?: goalPresets.first()
            habitRepository.addHabit(
                Habit(
                    name = preset.title,
                    category = preset.category,
                    colorHex = preset.colorHex,
                    targetDaysPerWeek = 7
                )
            )
            preferencesRepository.setFirstLaunchCompleted()
            onFinished()
        }
    }
}
