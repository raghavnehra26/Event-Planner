package com.focusforge.app.monetization

import android.app.Activity
import android.content.Context
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var rewardedAd: RewardedAd? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isLoadingAd = MutableStateFlow(false)
    val isLoadingAd: StateFlow<Boolean> = _isLoadingAd.asStateFlow()

    init {
        initializeAdMob()
    }

    private fun initializeAdMob() {
        try {
            MobileAds.initialize(context) {
                loadRewardedAd()
            }
        } catch (e: Exception) {
            // Log or fallback
        }
    }

    fun loadRewardedAd() {
        if (_isLoadingAd.value || rewardedAd != null) return

        _isLoadingAd.value = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            MonetizationConfig.ADMOB_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _isRewardedAdLoaded.value = true
                    _isLoadingAd.value = false
                    setupAdCallbacks(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    _isLoadingAd.value = false
                }
            }
        )
    }

    private fun setupAdCallbacks(ad: RewardedAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _isRewardedAdLoaded.value = false
                // Pre-load next rewarded ad
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                _isRewardedAdLoaded.value = false
            }

            override fun onAdShowedFullScreenContent() {
                // Ad displayed
            }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardGranted: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { _ ->
                scope.launch {
                    preferencesRepository.grantRewardedProHours(24)
                    onRewardGranted()
                }
            }
        } else {
            loadRewardedAd()
            onUnavailable()
        }
    }
}
