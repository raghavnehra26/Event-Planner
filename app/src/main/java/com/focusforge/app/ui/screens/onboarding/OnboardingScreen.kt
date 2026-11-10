package com.focusforge.app.ui.screens.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val isActive = index == currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (isActive) 28.dp else 10.dp)
                        .clip(CircleShape)
                        .background(if (isActive) NeonEmerald else DarkBorder)
                )
            }
        }

        // Content Area per Step
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (currentPage) {
                0 -> StepOneValueProp()
                1 -> StepTwoGoalPicker(
                    presets = viewModel.goalPresets,
                    selectedPreset = selectedPreset,
                    onSelect = { viewModel.selectPreset(it) }
                )
                2 -> StepThreeSoftPaywall()
            }
        }

        // Bottom CTA Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentPage) {
                0 -> {
                    Button(
                        onClick = { viewModel.setPage(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Text(
                            text = "Get Started",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                1 -> {
                    Button(
                        onClick = { viewModel.setPage(2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Text(
                            text = "Lock In My Goal",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                2 -> {
                    Button(
                        onClick = {
                            viewModel.completeOnboarding {
                                onNavigateToPaywall()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Try FocusForge Pro",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding {
                                onNavigateToHome()
                            }
                        }
                    ) {
                        Text(
                            text = "Continue with Free Plan",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepOneValueProp() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(NeonEmerald, Color(0xFF00B248)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "FocusForge",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Habit & Deep Work Engine",
            color = NeonEmerald,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Zero bloat. No forced account signups. 100% offline-first speed with GitHub-style contribution heatmaps.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun StepTwoGoalPicker(
    presets: List<GoalPreset>,
    selectedPreset: GoalPreset?,
    onSelect: (GoalPreset) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "What is your #1 focus goal?",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "We will set up your daily streak tracking engine.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        presets.forEach { preset ->
            val isSelected = preset == selectedPreset
            val borderColor = if (isSelected) NeonEmerald else DarkBorder

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable { onSelect(preset) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(preset.colorHex)))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = preset.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = NeonEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepThreeSoftPaywall() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentAmber, StreakOrange))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Unlock Your Peak Potential",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        listOf(
            "Unlimited Habit & Workout Trackers",
            "365-Day Contribution Heatmap Matrix",
            "Zero Ads Experience",
            "CSV & JSON Data Backup Export"
        ).forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = feature,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
