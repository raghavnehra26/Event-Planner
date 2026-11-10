package com.focusforge.app.monetization

import android.app.Activity
import android.content.Context
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayReviewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val reviewManager = ReviewManagerFactory.create(context)

    suspend fun checkAndTriggerReview(activity: Activity) {
        val userSettings = preferencesRepository.userSettingsFlow.first()
        if (userSettings.hasReviewedApp) return

        // Retention & Rating trigger: after 5 completed sessions or streak logs
        if (userSettings.completedSessionsCount >= 5) {
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        // Mark as reviewed to respect user experience
                        scope.launch {
                            preferencesRepository.setHasReviewedApp(true)
                        }
                    }
                }
            }
        }
    }
}
