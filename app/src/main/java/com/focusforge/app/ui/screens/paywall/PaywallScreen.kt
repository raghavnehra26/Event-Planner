package com.focusforge.app.ui.screens.paywall

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.BuildConfig
import com.focusforge.app.monetization.MonetizationConfig
import com.focusforge.app.monetization.PurchaseState
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
fun PaywallScreen(
    viewModel: PaywallViewModel,
    onDismiss: () -> Unit
) {
    val monthlyPrice by viewModel.monthlyPrice.collectAsState()
    val lifetimePrice by viewModel.lifetimePrice.collectAsState()
    val selectedPlan by viewModel.selectedPlan.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(uiState) {
        when (uiState) {
            is PurchaseState.Success -> {
                Toast.makeText(context, "Welcome to FocusForge Pro! 🎉", Toast.LENGTH_LONG).show()
                onDismiss()
            }
            is PurchaseState.Error -> {
                Toast.makeText(context, (uiState as PurchaseState.Error).message, Toast.LENGTH_SHORT).show()
            }
            is PurchaseState.Cancelled -> {
                // Cancelled silently
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Close Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (BuildConfig.DEBUG && MonetizationConfig.USE_MOCK_BILLING_FALLBACK) {
            Text(
                text = "Demo build: unavailable Play products may use a simulated purchase. No payment is taken; this is not a real subscription.",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Icon & Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AccentAmber, StreakOrange))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FOCUSFORGE PRO",
                color = AccentAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Forge Unstoppable Momentum",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Break through all limits with complete habit tracking & analytics.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, start = 16.dp, end = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Features Checklist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProFeatureRow("Unlimited Habits & Workouts", "Free limited to 3 habits")
            ProFeatureRow("Full 365-Day Contribution Heatmap", "Free limited to 7 days")
            ProFeatureRow("Zero Ads Guaranteed", "100% distraction-free offline speed")
            ProFeatureRow("CSV & JSON Complete Data Export", "Ownership of all your records")
            ProFeatureRow("Custom Sound & Interval Timers", "Tailored deep work rounds")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pricing Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Lifetime Plan (Recommended)
            PlanCard(
                title = "Lifetime Access",
                price = lifetimePrice,
                badge = "BEST VALUE",
                isSelected = selectedPlan == SelectedPlan.LIFETIME,
                onClick = { viewModel.selectPlan(SelectedPlan.LIFETIME) },
                modifier = Modifier.weight(1f)
            )

            // Monthly Subscription
            PlanCard(
                title = "Monthly Plan",
                price = monthlyPrice,
                badge = "FLEXIBLE",
                isSelected = selectedPlan == SelectedPlan.MONTHLY,
                onClick = { viewModel.selectPlan(SelectedPlan.MONTHLY) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Purchase CTA Button
        Button(
            onClick = { viewModel.initiatePurchase(activity) },
            enabled = uiState !is PurchaseState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
        ) {
            if (uiState is PurchaseState.Loading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedPlan == SelectedPlan.LIFETIME) "Unlock Lifetime Pro" else "Start Monthly Pro",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Restore & Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { viewModel.restorePurchases() }) {
                Text(
                    text = "Restore Purchases",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "One-time purchase or recurring billing. Cancel anytime in Google Play Store.",
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun ProFeatureRow(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(NeonEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    badge: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AccentAmber else DarkBorder

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) AccentAmber else DarkSurfaceVariant)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = badge,
                color = if (isSelected) Color.Black else TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = price,
            color = if (isSelected) AccentAmber else TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    }
}
