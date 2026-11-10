package com.focusforge.app.ui.screens.dashboard

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.domain.model.Habit
import com.focusforge.app.domain.model.HabitCategory
import com.focusforge.app.domain.model.TierLimits
import com.focusforge.app.ui.components.HeatmapGrid
import com.focusforge.app.ui.components.ProBadge
import com.focusforge.app.ui.theme.AccentAmber
import com.focusforge.app.ui.theme.DarkBackground
import com.focusforge.app.ui.theme.DarkBorder
import com.focusforge.app.ui.theme.DarkSurface
import com.focusforge.app.ui.theme.DarkSurfaceVariant
import com.focusforge.app.ui.theme.NeonEmerald
import com.focusforge.app.ui.theme.StreakOrange
import com.focusforge.app.ui.theme.TextMuted
import com.focusforge.app.ui.theme.TextPrimary
import com.focusforge.app.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTimer: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val heatmapDays by viewModel.heatmapDays.collectAsState()
    val todayFocusMins by viewModel.todayFocusMinutes.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                DashboardEvent.ShowPaywallLimitReached -> {
                    Toast.makeText(
                        context,
                        "Free tier limited to ${TierLimits.MAX_FREE_ACTIVE_HABITS} habits. Upgrade to Pro!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateToPaywall()
                }
                is DashboardEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!userSettings.isProActive && habits.size >= TierLimits.MAX_FREE_ACTIVE_HABITS) {
                        onNavigateToPaywall()
                    } else {
                        showAddDialog = true
                    }
                },
                containerColor = NeonEmerald,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FocusForge",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Today's Execution",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    if (userSettings.isProActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .border(1.dp, NeonEmerald, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRO ACTIVE",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        ProBadge(onClick = onNavigateToPaywall)
                    }
                }
            }

            // Quick Pomodoro Focus Launcher Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkSurfaceVariant, DarkSurface)
                            )
                        )
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToTimer() }
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Start Focus Session",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${todayFocusMins}m completed today",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Contribution Streak Heatmap Strip
            item {
                HeatmapGrid(
                    days = heatmapDays,
                    isPro = userSettings.isProActive,
                    onLockedClick = onNavigateToPaywall
                )
            }

            // Habits Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Streaks (${habits.size}${if (!userSettings.isProActive) "/${TierLimits.MAX_FREE_ACTIVE_HABITS}" else ""})",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!userSettings.isProActive && habits.size >= TierLimits.MAX_FREE_ACTIVE_HABITS) {
                        Text(
                            text = "Limit reached",
                            color = AccentAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToPaywall() }
                        )
                    }
                }
            }

            // Empty state if no habits
            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active habits yet. Tap '+' below to forge your first streak!",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Habits List
            items(habits, key = { it.id }) { habit ->
                HabitCardItem(
                    habit = habit,
                    onToggle = { viewModel.toggleHabit(habit.id, activity) },
                    onDelete = { viewModel.deleteHabit(habit) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, colorHex, days ->
                viewModel.addHabit(name, category, colorHex, days) {
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
private fun HabitCardItem(
    habit: Habit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        NeonEmerald
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                if (habit.isCompletedToday) NeonEmerald.copy(alpha = 0.6f) else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(habitColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = habit.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = StreakOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${habit.currentStreak} day streak",
                        color = StreakOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${habit.category.displayName}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Checkmark Toggle Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (habit.isCompletedToday) NeonEmerald else DarkSurfaceVariant)
                    .border(
                        1.5.dp,
                        if (habit.isCompletedToday) NeonEmerald else DarkBorder,
                        CircleShape
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (habit.isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: HabitCategory, colorHex: String, targetDays: Int) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(HabitCategory.STUDY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Forge New Habit",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Habit Title (e.g., LeetCode 2 Problems)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = NeonEmerald,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HabitCategory.values().forEach { category ->
                        val isSelected = category == selectedCategory
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
                                .clickable { selectedCategory = category }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(category.defaultColor)))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = category.displayName,
                                color = if (isSelected) NeonEmerald else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        onConfirm(
                            habitName.trim(),
                            selectedCategory,
                            selectedCategory.defaultColor,
                            7
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
            ) {
                Text("Create Habit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
