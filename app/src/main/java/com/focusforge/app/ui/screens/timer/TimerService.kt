package com.focusforge.app.ui.screens.timer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import com.focusforge.app.core.notification.FocusNotificationManager
import com.focusforge.app.core.sound.SoundFeedbackManager
import com.focusforge.app.core.util.DateUtils
import com.focusforge.app.domain.model.FocusSession
import com.focusforge.app.domain.model.SessionMode
import com.focusforge.app.domain.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var notificationManager: FocusNotificationManager

    @Inject
    lateinit var soundFeedbackManager: SoundFeedbackManager

    @Inject
    lateinit var focusSessionRepository: FocusSessionRepository

    private val binder = TimerBinder()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var deadlineElapsedRealtime = 0L
    private val _currentMode = MutableStateFlow(SessionMode.POMODORO)
    val currentMode: StateFlow<SessionMode> = _currentMode.asStateFlow()

    private val _totalSeconds = MutableStateFlow(25 * 60L)
    val totalSeconds: StateFlow<Long> = _totalSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _currentModeTitle = MutableStateFlow("Focus Block")
    val currentModeTitle: StateFlow<String> = _currentModeTitle.asStateFlow()

    var onSessionCompleted: (() -> Unit)? = null

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    fun configureTimer(minutes: Int, mode: SessionMode) {
        pauseTimer()
        val secs = minutes * 60L
        _totalSeconds.value = secs
        _remainingSeconds.value = secs
        _currentMode.value = mode
        _currentModeTitle.value = mode.title
    }

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        deadlineElapsedRealtime = SystemClock.elapsedRealtime() + _remainingSeconds.value * 1_000L

        val notification = notificationManager.buildTimerNotification(
            timeFormatted = DateUtils.formatSecondsToTimer(_remainingSeconds.value),
            modeTitle = _currentModeTitle.value,
            isRunning = true
        )
        startForeground(FocusNotificationManager.NOTIFICATION_ID_TIMER, notification)

        timerJob = scope.launch {
            while (_isRunning.value && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value = remainingFromDeadline()

                val updatedNotification = notificationManager.buildTimerNotification(
                    timeFormatted = DateUtils.formatSecondsToTimer(_remainingSeconds.value),
                    modeTitle = _currentModeTitle.value,
                    isRunning = true
                )
                val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                nm.notify(FocusNotificationManager.NOTIFICATION_ID_TIMER, updatedNotification)

                if (_remainingSeconds.value <= 0) {
                    _isRunning.value = false
                    soundFeedbackManager.playSessionCompleteTone()
                    soundFeedbackManager.triggerCompletionCelebrationHaptic()
                    recordCompletedFocusSession()
                    onSessionCompleted?.invoke()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        if (_isRunning.value) {
            _remainingSeconds.value = remainingFromDeadline()
        }
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
        deadlineElapsedRealtime = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun resetTimer() {
        pauseTimer()
        _remainingSeconds.value = _totalSeconds.value
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        notificationManager.cancelTimerNotification()
    }

    private fun remainingFromDeadline(): Long {
        val millisecondsLeft = deadlineElapsedRealtime - SystemClock.elapsedRealtime()
        return ((millisecondsLeft + 999L) / 1_000L).coerceAtLeast(0L)
    }

    private suspend fun recordCompletedFocusSession() {
        val mode = _currentMode.value
        if (mode != SessionMode.POMODORO && mode != SessionMode.CUSTOM) return
        try {
            focusSessionRepository.recordFocusSession(
                FocusSession(
                    durationMinutes = (_totalSeconds.value / 60L).toInt(),
                    mode = mode,
                    dateString = DateUtils.todayString(),
                )
            )
        } catch (error: Exception) {
            Log.e("FocusForgeTimer", "Could not save completed focus session", error)
        }
    }
}
