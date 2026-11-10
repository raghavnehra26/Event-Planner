package com.focusforge.app.ui.screens.timer

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.focusforge.app.core.sound.SoundFeedbackManager
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.domain.model.SessionMode
import com.focusforge.app.monetization.PlayReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val soundFeedbackManager: SoundFeedbackManager,
    private val playReviewManager: PlayReviewManager
) : ViewModel() {

    private var timerService: TimerService? = null
    private var isBound = false

    private val _selectedMode = MutableStateFlow(SessionMode.POMODORO)
    val selectedMode: StateFlow<SessionMode> = _selectedMode.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _totalSeconds = MutableStateFlow(25 * 60L)
    val totalSeconds: StateFlow<Long> = _totalSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var currentActivity: Activity? = null

    val userSettings = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            val svc = binder.getService()
            timerService = svc
            isBound = true

            svc.onSessionCompleted = {
                handleSessionFinished()
            }

            viewModelScope.launch {
                svc.remainingSeconds.collect { _remainingSeconds.value = it }
            }
            viewModelScope.launch {
                svc.totalSeconds.collect { _totalSeconds.value = it }
            }
            viewModelScope.launch {
                svc.isRunning.collect { _isRunning.value = it }
            }
            viewModelScope.launch {
                if (svc.isRunning.value) {
                    _selectedMode.value = svc.currentMode.value
                } else {
                    val settings = preferencesRepository.userSettingsFlow.first()
                    setMode(SessionMode.POMODORO, settings.workMinutes)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    fun setMode(mode: SessionMode, customMinutes: Int? = null) {
        _selectedMode.value = mode
        viewModelScope.launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            val minutes = customMinutes ?: when (mode) {
                SessionMode.POMODORO -> settings.workMinutes
                SessionMode.SHORT_BREAK -> settings.shortBreakMinutes
                SessionMode.LONG_BREAK -> settings.longBreakMinutes
                SessionMode.CUSTOM -> 50
            }
            val secs = minutes * 60L
            _totalSeconds.value = secs
            _remainingSeconds.value = secs
            timerService?.configureTimer(minutes, mode)
        }
    }

    fun toggleStartPause() {
        val running = _isRunning.value
        val svc = timerService
        viewModelScope.launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            if (running) {
                svc?.pauseTimer()
                soundFeedbackManager.triggerHapticFeedback(settings.vibrationEnabled)
            } else {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, TimerService::class.java)
                )
                svc?.startTimer()
                soundFeedbackManager.triggerHapticFeedback(settings.vibrationEnabled)
            }
        }
    }

    fun resetTimer() {
        timerService?.resetTimer()
        viewModelScope.launch {
            val settings = preferencesRepository.userSettingsFlow.first()
            soundFeedbackManager.triggerHapticFeedback(settings.vibrationEnabled)
        }
    }

    private fun handleSessionFinished() {
        val mode = _selectedMode.value
        if (mode == SessionMode.POMODORO || mode == SessionMode.CUSTOM) {
            // The service persists the session so navigation cannot discard it.
            currentActivity?.let { activity ->
                viewModelScope.launch {
                    playReviewManager.checkAndTriggerReview(activity)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            timerService?.onSessionCompleted = null
            context.unbindService(connection)
            isBound = false
        }
    }
}
