package com.focusforge.app.ui.screens.analytics

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.ui.components.AdBannerView
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
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateToPaywall: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val totalFocusMinutes by viewModel.totalFocusMinutes.collectAsState()
    val totalSessions by viewModel.totalSessionsCount.collectAsState()
    val heatmapDays by viewModel.heatmapDays.collectAsState()
    val isRewardedReady by viewModel.isRewardedAdReady.collectAsState()
    val isRewardedLoading by viewModel.isRewardedAdLoading.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val totalHours = String.format("%.1f", totalFocusMinutes / 60.0f)

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            AdBannerView(isProUser = userSettings.isProActive)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Deep Analytics",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (userSettings.isProActive) "Full 365-Day History" else "7-Day Free History",
                        color = if (userSettings.isProActive) NeonEmerald else TextSecondary,
                        fontSize = 13.sp
                    )
                }
                if (!userSettings.isProActive) {
                    ProBadge(onClick = onNavigateToPaywall)
                }
            }

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Focus",
                    value = "${totalHours}h",
                    subtitle = "$totalFocusMinutes mins logged",
                    icon = Icons.Default.HourglassBottom,
                    tint = NeonEmerald,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Sessions",
                    value = "$totalSessions",
                    subtitle = "Blocks forged",
                    icon = Icons.Default.LocalFireDepartment,
                    tint = StreakOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            // Heatmap Matrix Card
            HeatmapGrid(
                days = heatmapDays,
                isPro = userSettings.isProActive,
                onLockedClick = onNavigateToPaywall
            )

            // Rewarded Ad / Pro Unlock Section for Free Users
            if (!userSettings.isProActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Full Year Analytics Locked",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Upgrade to Pro for full history, or complete an available rewarded ad to unlock the 365-day view for 24 hours. No unlock is granted if an ad cannot be shown.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rewarded Ad Trigger Button
                    Button(
                        onClick = { viewModel.watchRewardedAd(activity) },
                        enabled = !isRewardedLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isRewardedReady -> "Watch Ad to Unlock (24 Hours)"
                                    isRewardedLoading -> "Loading Rewarded Ad…"
                                    else -> "Retry Loading Rewarded Ad"
                                },
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct Paywall CTA
                    Button(
                        onClick = onNavigateToPaywall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Get Pro (Lifetime / Monthly)",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}
