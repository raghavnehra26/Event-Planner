package com.focusforge.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.ui.components.ProBadge
import com.focusforge.app.ui.theme.AccentAmber
import com.focusforge.app.ui.theme.DarkBackground
import com.focusforge.app.ui.theme.DarkBorder
import com.focusforge.app.ui.theme.DarkSurface
import com.focusforge.app.ui.theme.DarkSurfaceVariant
import com.focusforge.app.ui.theme.NeonEmerald
import com.focusforge.app.ui.theme.TextMuted
import com.focusforge.app.ui.theme.TextPrimary
import com.focusforge.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToPaywall: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current

    var showTimerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.ShareFile -> {
                    context.startActivity(event.intent)
                }
                is SettingsEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                SettingsEvent.OpenPaywall -> {
                    onNavigateToPaywall()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Text(
            text = "Engine Settings",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Pro Status Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (userSettings.isProActive) NeonEmerald.copy(alpha = 0.1f) else DarkSurface)
                .border(
                    1.dp,
                    if (userSettings.isProActive) NeonEmerald.copy(alpha = 0.5f) else DarkBorder,
                    RoundedCornerShape(14.dp)
                )
                .clickable {
                    if (!userSettings.isProActive) onNavigateToPaywall()
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (userSettings.isProActive) NeonEmerald else AccentAmber),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (userSettings.isProActive) "FocusForge Pro Unlocked" else "Free Tier Plan",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (userSettings.isProActive) "Unlimited habits & full heatmaps active" else "Tap to upgrade for unlimited features",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (!userSettings.isProActive) {
                ProBadge(onClick = onNavigateToPaywall)
            }
        }

        // Section: Timer Configuration
        SettingsSection(title = "Timer Intervals") {
            SettingsItemRow(
                title = "Focus & Break Durations",
                subtitle = "${userSettings.workMinutes}m focus • ${userSettings.shortBreakMinutes}m short • ${userSettings.longBreakMinutes}m long",
                icon = Icons.Default.Timer,
                onClick = { showTimerDialog = true }
            )
        }

        // Section: Feedback & Audio
        SettingsSection(title = "Haptics & Cues") {
            SettingsToggleRow(
                title = "Audio Feedback",
                subtitle = "Subtle chimes on block completion",
                icon = Icons.Default.VolumeUp,
                checked = userSettings.soundEnabled,
                onCheckedChange = { viewModel.toggleSound(it) }
            )
            SettingsToggleRow(
                title = "Haptic Vibration",
                subtitle = "Tactile clicks for habit check-ins",
                icon = Icons.Default.Vibration,
                checked = userSettings.vibrationEnabled,
                onCheckedChange = { viewModel.toggleVibration(it) }
            )
        }

        // Section: Notifications
        SettingsSection(title = "Retention & Alerts") {
            SettingsToggleRow(
                title = "Daily Streak Reminder",
                subtitle = "Evening notification if habits are pending",
                icon = Icons.Default.Notifications,
                checked = userSettings.streakReminderEnabled,
                onCheckedChange = { viewModel.toggleStreakReminder(it) }
            )
        }

        // Section: Data Portability & Export
        SettingsSection(title = "Data Portability (Pro)") {
            SettingsItemRow(
                title = "Export as CSV",
                subtitle = "Spreadsheet format of all habit logs",
                icon = Icons.Default.Download,
                isProLocked = !userSettings.isProActive,
                onClick = { viewModel.exportData(isCsv = true) }
            )
            SettingsItemRow(
                title = "Export as JSON",
                subtitle = "Full database backup payload",
                icon = Icons.Default.Download,
                isProLocked = !userSettings.isProActive,
                onClick = { viewModel.exportData(isCsv = false) }
            )
        }

        // Offline & Security Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "100% Offline & Private",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zero remote telemetry or server storage. Your habits and focus data remain strictly on this device.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Version & Build Footnote
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FocusForge v1.0.0 • Capstone Demo",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }

    if (showTimerDialog) {
        TimerConfigDialog(
            currentWork = userSettings.workMinutes,
            currentShortBreak = userSettings.shortBreakMinutes,
            currentLongBreak = userSettings.longBreakMinutes,
            onDismiss = { showTimerDialog = false },
            onSave = { work, shortBreak, longBreak ->
                viewModel.updateTimer(work, shortBreak, longBreak)
                showTimerDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItemRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isProLocked: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        if (isProLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = AccentAmber,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = NeonEmerald,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun TimerConfigDialog(
    currentWork: Int,
    currentShortBreak: Int,
    currentLongBreak: Int,
    onDismiss: () -> Unit,
    onSave: (work: Int, shortBreak: Int, longBreak: Int) -> Unit
) {
    var work by remember { mutableStateOf(currentWork.toFloat()) }
    var shortBreak by remember { mutableStateOf(currentShortBreak.toFloat()) }
    var longBreak by remember { mutableStateOf(currentLongBreak.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Configure Durations",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text = "Focus Block: ${work.toInt()} min",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = work,
                        onValueChange = { work = it },
                        valueRange = 10f..60f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonEmerald,
                            activeTrackColor = NeonEmerald,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                }

                Column {
                    Text(
                        text = "Short Break: ${shortBreak.toInt()} min",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = shortBreak,
                        onValueChange = { shortBreak = it },
                        valueRange = 2f..15f,
                        steps = 12,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonEmerald,
                            activeTrackColor = NeonEmerald,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                }

                Column {
                    Text(
                        text = "Long Break: ${longBreak.toInt()} min",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Slider(
                        value = longBreak,
                        onValueChange = { longBreak = it },
                        valueRange = 10f..30f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonEmerald,
                            activeTrackColor = NeonEmerald,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(work.toInt(), shortBreak.toInt(), longBreak.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
