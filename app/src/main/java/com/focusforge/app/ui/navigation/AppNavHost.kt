package com.focusforge.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusforge.app.ui.screens.analytics.AnalyticsScreen
import com.focusforge.app.ui.screens.analytics.AnalyticsViewModel
import com.focusforge.app.ui.screens.dashboard.DashboardScreen
import com.focusforge.app.ui.screens.dashboard.DashboardViewModel
import com.focusforge.app.ui.screens.onboarding.OnboardingScreen
import com.focusforge.app.ui.screens.onboarding.OnboardingViewModel
import com.focusforge.app.ui.screens.paywall.PaywallScreen
import com.focusforge.app.ui.screens.paywall.PaywallViewModel
import com.focusforge.app.ui.screens.settings.SettingsScreen
import com.focusforge.app.ui.screens.settings.SettingsViewModel
import com.focusforge.app.ui.screens.timer.TimerScreen
import com.focusforge.app.ui.screens.timer.TimerViewModel
import com.focusforge.app.ui.theme.DarkBackground
import com.focusforge.app.ui.theme.DarkBorder
import com.focusforge.app.ui.theme.DarkSurface
import com.focusforge.app.ui.theme.NeonEmerald
import com.focusforge.app.ui.theme.TextMuted
import com.focusforge.app.ui.theme.TextPrimary
import com.focusforge.app.ui.theme.TextSecondary

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AppNavHost(
    isFirstLaunch: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, Icons.Default.CheckCircle, "Forge"),
        BottomNavItem(Screen.Timer, Icons.Default.Timer, "Timer"),
        BottomNavItem(Screen.Analytics, Icons.Default.BarChart, "Analytics"),
        BottomNavItem(Screen.Settings, Icons.Default.Settings, "Settings")
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier.border(0.5.dp, DarkBorder)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    color = if (selected) NeonEmerald else TextSecondary
                                )
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonEmerald,
                                unselectedIconColor = TextSecondary,
                                indicatorColor = NeonEmerald.copy(alpha = 0.15f)
                            ),
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isFirstLaunch) Screen.Onboarding.route else Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Onboarding.route) {
                val viewModel: OnboardingViewModel = hiltViewModel()
                OnboardingScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onNavigateToPaywall = {
                        navController.navigate(Screen.Paywall.route)
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTimer = { navController.navigate(Screen.Timer.route) },
                    onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                )
            }

            composable(Screen.Timer.route) {
                val viewModel: TimerViewModel = hiltViewModel()
                TimerScreen(viewModel = viewModel)
            }

            composable(Screen.Analytics.route) {
                val viewModel: AnalyticsViewModel = hiltViewModel()
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToPaywall = { navController.navigate(Screen.Paywall.route) }
                )
            }

            composable(Screen.Paywall.route) {
                val viewModel: PaywallViewModel = hiltViewModel()
                PaywallScreen(
                    viewModel = viewModel,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
