package com.focusforge.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Onboarding : Screen("onboarding", "Welcome")
    object Dashboard : Screen("dashboard", "Forge")
    object Timer : Screen("timer", "Timer")
    object Analytics : Screen("analytics", "Analytics")
    object Settings : Screen("settings", "Settings")
    object Paywall : Screen("paywall", "FocusForge Pro")
}
