package com.focusforge.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusforge.app.core.notification.StreakReminderWorker
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.data.preferences.UserSettings
import com.focusforge.app.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class SettingsEvent {
    data class ShareFile(val intent: Intent) : SettingsEvent()
    data class ShowToast(val message: String) : SettingsEvent()
    object OpenPaywall : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun updateTimer(work: Int, shortBreak: Int, longBreak: Int) {
        viewModelScope.launch {
            preferencesRepository.updateTimerSettings(work, shortBreak, longBreak)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateFeedbackSettings(
                sound = enabled,
                vibration = userSettings.value.vibrationEnabled
            )
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateFeedbackSettings(
                sound = userSettings.value.soundEnabled,
                vibration = enabled
            )
        }
    }

    fun toggleStreakReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStreakReminder(enabled)
            if (enabled) {
                StreakReminderWorker.schedule(context)
            }
        }
    }

    fun exportData(isCsv: Boolean) {
        viewModelScope.launch {
            if (!userSettings.value.isProActive) {
                _events.emit(SettingsEvent.OpenPaywall)
                return@launch
            }

            try {
                val content = if (isCsv) {
                    habitRepository.exportDataAsCsv()
                } else {
                    habitRepository.exportDataAsJson()
                }

                val fileName = "focusforge_export_${System.currentTimeMillis()}.${if (isCsv) "csv" else "json"}"
                val cacheDir = File(context.cacheDir, "exports")
                if (!cacheDir.exists()) cacheDir.mkdirs()

                val file = File(cacheDir, fileName)
                file.writeText(content)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (isCsv) "text/csv" else "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "FocusForge Data Export")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Export FocusForge Records")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                _events.emit(SettingsEvent.ShareFile(chooser))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowToast("Export failed: ${e.message}"))
            }
        }
    }
}
