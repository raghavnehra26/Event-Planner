package com.focusforge.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.focusforge.app.MainActivity
import com.focusforge.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID_TIMER = "channel_focus_timer"
        const val CHANNEL_ID_REMINDERS = "channel_streak_reminders"
        const val NOTIFICATION_ID_TIMER = 1001
        const val NOTIFICATION_ID_REMINDER = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(
                CHANNEL_ID_TIMER,
                context.getString(R.string.notification_channel_timer),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_timer_desc)
                setShowBadge(false)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun buildTimerNotification(
        timeFormatted: String,
        modeTitle: String,
        isRunning: Boolean
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
            .setContentTitle(modeTitle)
            .setContentText("Time remaining: $timeFormatted")
            .setSmallIcon(R.drawable.ic_stat_focus)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showStreakReminderNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_focus)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }

    fun cancelTimerNotification() {
        notificationManager.cancel(NOTIFICATION_ID_TIMER)
    }
}
