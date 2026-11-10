package com.focusforge.app.monetization

object MonetizationConfig {

    // =============================================================================================
    // GOOGLE PLAY BILLING PRODUCT CONFIGURATION
    // TODO: Configure matching product IDs in Google Play Console > Monetize > In-app products / Subscriptions
    // =============================================================================================
    const val PRODUCT_ID_PRO_MONTHLY = "focusforge_pro_monthly"
    const val PRODUCT_ID_PRO_LIFETIME = "focusforge_pro_lifetime"

    // Debug-only demo path. BillingManager gates this with BuildConfig.DEBUG.
    const val USE_MOCK_BILLING_FALLBACK = true

    // =============================================================================================
    // ADMOB AD UNIT IDS
    // Replace with your real AdMob unit IDs before releasing to Google Play.
    // The current strings are Google's verified sample test unit IDs.
    // =============================================================================================
    
    // TODO: Paste your real AdMob Banner Unit ID below
    const val ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Google Sample Banner

    // TODO: Paste your real AdMob Rewarded Ad Unit ID below
    const val ADMOB_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Google Sample Rewarded
}
