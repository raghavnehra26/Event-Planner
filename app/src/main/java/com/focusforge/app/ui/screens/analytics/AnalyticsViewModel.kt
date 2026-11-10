package com.focusforge.app.ui.screens.analytics

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusforge.app.core.util.DateUtils
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.data.preferences.UserSettings
import com.focusforge.app.domain.model.HeatmapDay
import com.focusforge.app.domain.model.TierLimits
import com.focusforge.app.domain.repository.FocusSessionRepository
import com.focusforge.app.domain.repository.HabitRepository
import com.focusforge.app.monetization.AdManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val sessionRepository: FocusSessionRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val adManager: AdManager
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val totalFocusMinutes: StateFlow<Int> = sessionRepository.getTotalFocusMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSessionsCount: StateFlow<Int> = sessionRepository.getTotalSessionsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isRewardedAdReady: StateFlow<Boolean> = adManager.isRewardedAdLoaded
    val isRewardedAdLoading: StateFlow<Boolean> = adManager.isLoadingAd

    val heatmapDays: StateFlow<List<HeatmapDay>> = userSettings.flatMapLatest { settings ->
        val days = if (settings.isProActive) TierLimits.PRO_ANALYTICS_DAYS else TierLimits.FREE_ANALYTICS_DAYS
        habitRepository.getHeatmapData(days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    fun watchRewardedAd(activity: Activity) {
        if (!adManager.isRewardedAdLoaded.value) {
            adManager.loadRewardedAd()
            viewModelScope.launch {
                _messageEvent.emit("A rewarded ad is not ready yet. It is being loaded; try again shortly.")
            }
            return
        }
        adManager.showRewardedAd(
            activity = activity,
            onRewardGranted = {
                viewModelScope.launch {
                    _messageEvent.emit("Unlocked full 365-day analytics for 24 hours!")
                }
            },
            onUnavailable = {
                viewModelScope.launch {
                    _messageEvent.emit("The rewarded ad could not be shown. Please try again when an ad is available.")
                }
            }
        )
    }
}
