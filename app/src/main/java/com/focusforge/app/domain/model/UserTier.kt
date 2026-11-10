package com.focusforge.app.domain.model

enum class UserTier {
    FREE,
    PRO
}

data class PlanFeature(
    val title: String,
    val freeDesc: String,
    val proDesc: String,
    val isProExclusive: Boolean = false
)

object TierLimits {
    const val MAX_FREE_ACTIVE_HABITS = 3
    const val FREE_ANALYTICS_DAYS = 7
    const val PRO_ANALYTICS_DAYS = 365
}
