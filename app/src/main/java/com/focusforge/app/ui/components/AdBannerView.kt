package com.focusforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.focusforge.app.monetization.MonetizationConfig
import com.focusforge.app.ui.theme.DarkSurfaceVariant
import com.focusforge.app.ui.theme.TextMuted
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBannerView(
    isProUser: Boolean,
    modifier: Modifier = Modifier
) {
    if (isProUser) {
        // Completely removed / zero space occupied for Pro subscribers
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = MonetizationConfig.ADMOB_BANNER_AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
