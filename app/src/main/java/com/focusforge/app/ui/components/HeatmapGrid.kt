package com.focusforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusforge.app.domain.model.HeatmapDay
import com.focusforge.app.ui.theme.DarkBorder
import com.focusforge.app.ui.theme.DarkSurface
import com.focusforge.app.ui.theme.DarkSurfaceVariant
import com.focusforge.app.ui.theme.HeatmapLevel0
import com.focusforge.app.ui.theme.HeatmapLevel1
import com.focusforge.app.ui.theme.HeatmapLevel2
import com.focusforge.app.ui.theme.HeatmapLevel3
import com.focusforge.app.ui.theme.HeatmapLevel4
import com.focusforge.app.ui.theme.TextPrimary
import com.focusforge.app.ui.theme.TextSecondary

@Composable
fun HeatmapGrid(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    isPro: Boolean = false,
    onLockedClick: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    // Group days into columns of 7 (representing weeks)
    val columns = days.chunked(7)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Contribution Heatmap",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${days.count { it.count > 0 }} active days in cycle",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            if (!isPro) {
                ProBadge(onClick = onLockedClick)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heatmap Matrix
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            columns.forEach { weekColumn ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    weekColumn.forEach { day ->
                        val cellColor = when {
                            day.count == 0 -> HeatmapLevel0
                            day.intensity < 0.25f -> HeatmapLevel1
                            day.intensity < 0.50f -> HeatmapLevel2
                            day.intensity < 0.75f -> HeatmapLevel3
                            else -> HeatmapLevel4
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cellColor)
                                .border(0.5.dp, DarkBorder, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Less", color = TextSecondary, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(4.dp))
            listOf(
                HeatmapLevel0,
                HeatmapLevel1,
                HeatmapLevel2,
                HeatmapLevel3,
                HeatmapLevel4
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                        .border(0.5.dp, DarkBorder, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(text = "More", color = TextSecondary, fontSize = 10.sp)
        }
    }
}
