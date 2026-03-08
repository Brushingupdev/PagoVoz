package com.example.pagovoz

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.pagovoz.ui.theme.PagoVozTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PagoVozTheme {
                val context = LocalContext.current
                var isActive by remember { mutableStateOf(SessionManager.isActive(context)) }

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
                            openNotificationAccessSettings()
                        }
                    )
                }
            }
        }
    }

    private fun openNotificationAccessSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
}
