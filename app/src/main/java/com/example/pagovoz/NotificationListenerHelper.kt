package com.example.pagovoz

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService

object NotificationListenerHelper {

    fun isNotificationServiceEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        if (enabledListeners.isBlank()) return false

        val targetComponent = ComponentName(context, PagoNotificationListener::class.java)
        return enabledListeners
            .split(':')
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it == targetComponent }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, PagoAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        return enabledServices.split(':').any { it.equals(expectedComponentName, ignoreCase = true) }
    }

    fun requestRebind(context: Context, forceToggle: Boolean = false) {
        if (!isNotificationServiceEnabled(context)) return

        val component = ComponentName(context, PagoNotificationListener::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching {
                NotificationListenerService.requestRebind(component)
            }
        }

        // Forzar que el servicio se ponga en primer plano (foreground) con su notificacion
        val intent = android.content.Intent(context, PagoNotificationListener::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        if (!forceToggle) return

        val packageManager = context.packageManager
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
