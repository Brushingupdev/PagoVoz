package com.example.pagovoz

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.pagovoz.ui.theme.HablaPagoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListenerHeartbeatScheduler.schedule(this)
        ListenerDiagnostics.markRebindAttempt(this, forceToggle = false, reason = "main_create")
        NotificationListenerHelper.requestRebind(this, forceToggle = false)
        enableEdgeToEdge()

        setContent {
            HablaPagoTheme {
                val context = LocalContext.current
                var isActive by remember { mutableStateOf(SessionManager.isActive(context)) }
                var pendingNotificationAccessSettings by remember { mutableStateOf(false) }
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    if (pendingNotificationAccessSettings) {
                        openNotificationAccessSettings()
                        pendingNotificationAccessSettings = false
                    }
                }

                LaunchedEffect(isActive) {
                    if (
                        isActive &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                if (isActive) {
                    AppNavigation()
                } else {
                    ActivationScreen(
                        onActivated = {
                            CoroutineScope(Dispatchers.IO).launch {
                                SupabaseManager.checkPremiumStatus(context)
                            }
                            SessionManager.setActive(context, true)
                            isActive = true
                            if (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                pendingNotificationAccessSettings = true
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                openNotificationAccessSettings()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ListenerHeartbeatScheduler.schedule(this)
        ListenerDiagnostics.markRebindAttempt(this, forceToggle = false, reason = "main_resume")
        NotificationListenerHelper.requestRebind(this, forceToggle = false)
    }

    private fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}
