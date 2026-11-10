package com.focusforge.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.monetization.BillingManager
import com.focusforge.app.ui.navigation.AppNavHost
import com.focusforge.app.ui.theme.DarkBackground
import com.focusforge.app.ui.theme.FocusForgeTheme
import com.focusforge.app.ui.theme.NeonEmerald
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var billingManager: BillingManager

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        billingManager.startConnection()

        setContent {
            FocusForgeTheme {
                val userSettings by preferencesRepository.userSettingsFlow.collectAsState(initial = null)

                if (userSettings == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonEmerald)
                    }
                } else {
                    AppNavHost(
                        isFirstLaunch = userSettings!!.isFirstLaunch
                    )
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
