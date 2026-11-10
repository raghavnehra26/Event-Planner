package com.focusforge.app.core.sound

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playSessionCompleteTone(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 350)
        } catch (e: Exception) {
            // Ignore tone failure
        }
    }

    fun playHabitCheckTone(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (e: Exception) {
            // Ignore tone failure
        }
    }

    fun triggerHapticFeedback(vibrationEnabled: Boolean = true) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Ignore vibration failure
        }
    }

    fun triggerCompletionCelebrationHaptic(vibrationEnabled: Boolean = true) {
        if (!vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val timings = longArrayOf(0, 100, 80, 150)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}
