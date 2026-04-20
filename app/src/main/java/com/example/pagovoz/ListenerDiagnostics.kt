package com.example.pagovoz

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ListenerDiagnostics {
    private const val PREF_NAME = "listener_diagnostics"
    private const val KEY_CONNECTED = "connected"
    private const val KEY_LAST_CONNECT_AT = "last_connect_at"
    private const val KEY_LAST_DISCONNECT_AT = "last_disconnect_at"
    private const val KEY_LAST_NOTIFICATION_AT = "last_notification_at"
    private const val KEY_LAST_PAYMENT_AT = "last_payment_at"
    private const val KEY_LAST_REBIND_AT = "last_rebind_at"
    private const val KEY_LAST_REBIND_FORCE = "last_rebind_force"
    private const val KEY_EVENT_LOG = "event_log"
    private const val MAX_LOG_LINES = 40

    fun markListenerCreated(context: Context) {
        appendEvent(context, "listener_created")
    }

    fun markListenerConnected(context: Context) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putBoolean(KEY_CONNECTED, true)
            .putLong(KEY_LAST_CONNECT_AT, now)
            .apply()
        appendEvent(context, "listener_connected")
    }

    fun markListenerDisconnected(context: Context, reason: String) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putBoolean(KEY_CONNECTED, false)
            .putLong(KEY_LAST_DISCONNECT_AT, now)
            .apply()
        appendEvent(context, "listener_disconnected:$reason")
    }

    fun markNotificationReceived(context: Context, packageName: String) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putLong(KEY_LAST_NOTIFICATION_AT, now)
            .apply()
        appendEvent(context, "notification:$packageName")
    }

    fun markPaymentCaptured(context: Context, packageName: String, amount: Double, sender: String) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putLong(KEY_LAST_PAYMENT_AT, now)
            .apply()
        appendEvent(
            context,
            "payment:$packageName:${String.format(Locale.US, "%.2f", amount)}:${sender.take(24)}"
        )
    }

    fun markPaymentIgnored(context: Context, packageName: String, reason: String) {
        appendEvent(context, "ignored:$packageName:$reason")
    }

    fun markTtsReady(context: Context) {
        appendEvent(context, "tts_ready")
    }

    fun markTtsError(context: Context, reason: String) {
        appendEvent(context, "tts_error:$reason")
    }

    fun markRebindAttempt(context: Context, forceToggle: Boolean, reason: String) {
        val now = System.currentTimeMillis()
        prefs(context).edit()
            .putLong(KEY_LAST_REBIND_AT, now)
            .putBoolean(KEY_LAST_REBIND_FORCE, forceToggle)
            .apply()
        appendEvent(context, "rebind:${if (forceToggle) "force" else "soft"}:$reason")
    }

    fun shouldForceRecovery(context: Context): Boolean {
        val prefs = prefs(context)
        val connected = prefs.getBoolean(KEY_CONNECTED, false)
        val lastConnectAt = prefs.getLong(KEY_LAST_CONNECT_AT, 0L)
        val lastDisconnectAt = prefs.getLong(KEY_LAST_DISCONNECT_AT, 0L)
        return !connected || lastConnectAt == 0L || lastDisconnectAt > lastConnectAt
    }

    private fun appendEvent(context: Context, event: String) {
        val current = prefs(context).getString(KEY_EVENT_LOG, "").orEmpty()
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val lines = buildList {
            add("$timestamp $event")
            current.lineSequence()
                .filter { it.isNotBlank() }
                .take(MAX_LOG_LINES - 1)
                .forEach { add(it) }
        }
        prefs(context).edit()
            .putString(KEY_EVENT_LOG, lines.joinToString(separator = "\n"))
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
