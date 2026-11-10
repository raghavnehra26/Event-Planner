package com.focusforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.ui.theme.AccentAmber
import com.focusforge.app.ui.theme.StreakOrange

@Composable
fun ProBadge(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(AccentAmber, StreakOrange)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Pro",
                tint = Color.Black,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "PRO",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}
