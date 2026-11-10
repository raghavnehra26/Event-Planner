package com.focusforge.app.ui.screens.timer

import android.app.Activity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.core.util.DateUtils
import com.focusforge.app.domain.model.SessionMode
import com.focusforge.app.ui.components.CircularTimerProgress
import com.focusforge.app.ui.theme.DarkBackground
import com.focusforge.app.ui.theme.DarkBorder
import com.focusforge.app.ui.theme.DarkSurface
import com.focusforge.app.ui.theme.DarkSurfaceVariant
import com.focusforge.app.ui.theme.NeonEmerald
import com.focusforge.app.ui.theme.TextMuted
import com.focusforge.app.ui.theme.TextPrimary
import com.focusforge.app.ui.theme.TextSecondary

@Composable
fun TimerScreen(
    viewModel: TimerViewModel
) {
    val selectedMode by viewModel.selectedMode.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalSeconds by viewModel.totalSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(context) {
        if (context is Activity) {
            viewModel.setActivity(context)
        }
    }

    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 0f

    val formattedTime = DateUtils.formatSecondsToTimer(remainingSeconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text(
                text = "Deep Work Engine",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Distraction-free focus timer",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Mode Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ModeChip(
                label = "Focus (25m)",
                isSelected = selectedMode == SessionMode.POMODORO,
                onClick = { viewModel.setMode(SessionMode.POMODORO) }
            )
            ModeChip(
                label = "Short (5m)",
                isSelected = selectedMode == SessionMode.SHORT_BREAK,
                onClick = { viewModel.setMode(SessionMode.SHORT_BREAK) }
            )
            ModeChip(
                label = "Long (15m)",
                isSelected = selectedMode == SessionMode.LONG_BREAK,
                onClick = { viewModel.setMode(SessionMode.LONG_BREAK) }
            )
            ModeChip(
                label = "Deep (50m)",
                isSelected = selectedMode == SessionMode.CUSTOM,
                onClick = { viewModel.setMode(SessionMode.CUSTOM, 50) }
            )
        }

        // Circular Timer Gauge
        CircularTimerProgress(
            progress = progress,
            timeText = formattedTime,
            modeLabel = selectedMode.title,
            size = 280.dp,
            strokeWidth = 16.dp,
            progressColor = when (selectedMode) {
                SessionMode.POMODORO, SessionMode.CUSTOM -> NeonEmerald
                SessionMode.SHORT_BREAK -> Color(0xFF2979FF)
                SessionMode.LONG_BREAK -> Color(0xFFAB47BC)
            }
        )

        // Control Buttons (Play/Pause, Reset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable { viewModel.resetTimer() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Main Start/Pause Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(NeonEmerald)
                    .clickable { viewModel.toggleStartPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(84.dp)) // Visual balance with reset button
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonEmerald else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
