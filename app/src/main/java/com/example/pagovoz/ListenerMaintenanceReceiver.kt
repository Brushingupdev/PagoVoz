package com.example.pagovoz

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ListenerMaintenanceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                ListenerDiagnostics.markListenerDisconnected(context, "system_event")
                ListenerHeartbeatScheduler.schedule(context)
                ListenerDiagnostics.markRebindAttempt(context, forceToggle = true, reason = "system_event")
                NotificationListenerHelper.requestRebind(context, forceToggle = true)
            }
            Intent.ACTION_USER_PRESENT,
            ListenerHeartbeatScheduler.ACTION_HEARTBEAT -> {
                if (!SessionManager.isActive(context)) {
                    ListenerHeartbeatScheduler.cancel(context)
                    return
                }
                ListenerHeartbeatScheduler.schedule(context)
                val forceRecovery = ListenerDiagnostics.shouldForceRecovery(context)
                ListenerDiagnostics.markRebindAttempt(
                    context,
                    forceToggle = forceRecovery,
                    reason = intent.action ?: "heartbeat"
                )
                NotificationListenerHelper.requestRebind(context, forceToggle = forceRecovery)
                PagoGlanceWidget.updateAll(context)
            }
        }
    }
}
