package com.example.pagovoz

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

object ListenerHeartbeatScheduler {
    const val ACTION_HEARTBEAT = "com.example.pagovoz.action.LISTENER_HEARTBEAT"

    private const val HEARTBEAT_INTERVAL_MILLIS = 3 * 60 * 1000L
    private const val HEARTBEAT_REQUEST_CODE = 1042

    fun schedule(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        alarmManager.cancel(pendingIntent(appContext))
        val triggerAtMillis = SystemClock.elapsedRealtime() + HEARTBEAT_INTERVAL_MILLIS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                pendingIntent(appContext)
            )
        } else {
            alarmManager.setExact(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMillis,
                pendingIntent(appContext)
            )
        }
    }

    fun cancel(context: Context) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(pendingIntent(appContext))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(ACTION_HEARTBEAT).apply {
            component = ComponentName(context, ListenerMaintenanceReceiver::class.java)
        }
        return PendingIntent.getBroadcast(
            context,
            HEARTBEAT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
