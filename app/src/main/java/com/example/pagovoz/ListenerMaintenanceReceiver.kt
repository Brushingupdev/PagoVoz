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
                NotificationListenerHelper.requestRebind(context, forceToggle = true)
            }
        }
    }
}
