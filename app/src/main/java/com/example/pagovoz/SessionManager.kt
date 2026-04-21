package com.example.pagovoz

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Serializable
data class PaymentRecord(
    val amount: Double,
    val sender: String,
    val timestamp: Long
)

object SessionManager {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_ACTIVE = "is_active"
    private const val KEY_PREMIUM = "is_premium"
    private const val KEY_PREMIUM_UNTIL = "premium_until"
    private const val KEY_LAST_PREMIUM_CHECK = "last_premium_check"
    private const val KEY_TRIAL_MODAL_SHOWN = "trial_modal_shown"
    private const val KEY_TOTAL_AMOUNT = "total_amount"
    private const val KEY_TOTAL_AMOUNT_CENTS = "total_amount_cents"
    private const val KEY_TOTAL_COUNT = "total_count"
    private const val KEY_PAYMENTS_JSON = "payments_history"
    private const val KEY_LAST_RESET_DATE = "last_reset_date"
    private const val KEY_BATTERY_WARNING_DISMISSED = "battery_warning_dismissed"

    private const val KEY_YESTERDAY_TOTAL = "yesterday_total"
    private const val KEY_YESTERDAY_TOTAL_CENTS = "yesterday_total_cents"
    private const val KEY_YESTERDAY_COUNT = "yesterday_count"
    private const val KEY_YESTERDAY_HISTORY = "yesterday_history"
    private const val KEY_YESTERDAY_DATE = "yesterday_date"
    private const val KEY_TTS_VOICE_NAME = "tts_voice_name"
    private const val KEY_TTS_SPEECH_RATE = "tts_speech_rate"
    private const val KEY_TTS_SPEECH_PITCH = "tts_speech_pitch"
    private const val KEY_TTS_AMOUNT_ONLY = "tts_amount_only"
    private const val KEY_TTS_REPEAT_COUNT = "tts_repeat_count"
    private const val KEY_MULTI_DAY_HISTORY = "multi_day_history"
    private const val MULTI_DAY_HISTORY_DAYS = 7L

    private val _updates = MutableSharedFlow<Unit>(replay = 0)
    val updates = _updates.asSharedFlow()

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isActive(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ACTIVE, false)

    fun setActive(context: Context, active: Boolean) {
        prefs(context).edit().putBoolean(KEY_ACTIVE, active).apply()
        if (active) {
            ListenerHeartbeatScheduler.schedule(context)
        } else {
            ListenerHeartbeatScheduler.cancel(context)
        }
        _updates.tryEmit(Unit)
    }

    fun isTrialModalShown(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TRIAL_MODAL_SHOWN, false)

    fun setTrialModalShown(context: Context) {
        prefs(context).edit().putBoolean(KEY_TRIAL_MODAL_SHOWN, true).apply()
    }

    fun isPremium(context: Context): Boolean {
        val prefs = prefs(context)
        val isPremiumSaved = prefs.getBoolean(KEY_PREMIUM, false)
        if (!isPremiumSaved) return false

        // Validación adicional por fecha para evitar "premium infinito" offline
        val premiumUntilStr = prefs.getString(KEY_PREMIUM_UNTIL, null) ?: return true // Si no hay fecha pero es premium, asumimos legacy o prueba
        return try {
            val expiry = Instant.parse(premiumUntilStr)
            Instant.now().isBefore(expiry)
        } catch (_: Exception) {
            true
        }
    }

    fun setPremium(context: Context, premium: Boolean, premiumUntil: String? = null) {
        prefs(context).edit().apply {
            putBoolean(KEY_PREMIUM, premium)
            if (premiumUntil != null) {
                putString(KEY_PREMIUM_UNTIL, premiumUntil)
            } else if (!premium) {
                remove(KEY_PREMIUM_UNTIL)
            }
            putLong(KEY_LAST_PREMIUM_CHECK, System.currentTimeMillis())
        }.apply()
        _updates.tryEmit(Unit)
    }

    fun getPremiumDaysLeft(context: Context): Int {
        val premiumUntilStr = prefs(context).getString(KEY_PREMIUM_UNTIL, null) ?: return 0
        return try {
            val expiry = Instant.parse(premiumUntilStr)
            val days = ChronoUnit.DAYS.between(Instant.now(), expiry)
            days.toInt().coerceAtLeast(0)
        } catch (_: Exception) {
            0
        }
    }

    fun resetIfNewDay(context: Context) {
        val prefs = prefs(context)
        val today = LocalDate.now()
        val lastDate = prefs.getString(KEY_LAST_RESET_DATE, null)

        if (DailyResetPolicy.shouldReset(lastDate, today)) {
            val lastTotalCents = readAmountCents(prefs, KEY_TOTAL_AMOUNT_CENTS, KEY_TOTAL_AMOUNT)
            val lastCount = prefs.getInt(KEY_TOTAL_COUNT, 0)
            val lastHistory = prefs.getString(KEY_PAYMENTS_JSON, "[]") ?: "[]"

            prefs.edit()
                .putFloat(KEY_YESTERDAY_TOTAL, centsToFloat(lastTotalCents))
                .putLong(KEY_YESTERDAY_TOTAL_CENTS, lastTotalCents)
                .putInt(KEY_YESTERDAY_COUNT, lastCount)
                .putString(KEY_YESTERDAY_HISTORY, lastHistory)
                .putString(KEY_YESTERDAY_DATE, lastDate)
                .putFloat(KEY_TOTAL_AMOUNT, 0f)
                .putLong(KEY_TOTAL_AMOUNT_CENTS, 0L)
                .putInt(KEY_TOTAL_COUNT, 0)
                .putString(KEY_PAYMENTS_JSON, "[]")
                .putString(KEY_LAST_RESET_DATE, today.toString())
                .apply()

            pruneMultiDayHistory(prefs, today)
            _updates.tryEmit(Unit)
            PagoGlanceWidget.updateAll(context)
        }

        normalizeStoredHistory(prefs, today)
    }

    fun resetDailyTotals(context: Context) {
        prefs(context).edit()
            .putFloat(KEY_TOTAL_AMOUNT, 0f)
            .putLong(KEY_TOTAL_AMOUNT_CENTS, 0L)
            .putInt(KEY_TOTAL_COUNT, 0)
            .putString(KEY_PAYMENTS_JSON, "[]")
            .putString(KEY_LAST_RESET_DATE, LocalDate.now().toString())
            .apply()
        _updates.tryEmit(Unit)
        PagoGlanceWidget.updateAll(context)
    }

    fun addPayment(context: Context, amount: Double, sender: String): Boolean {
        if (isDuplicate(context, amount, sender)) return false
        resetIfNewDay(context)

        val prefs = prefs(context)
        val currentTotalCents = readAmountCents(prefs, KEY_TOTAL_AMOUNT_CENTS, KEY_TOTAL_AMOUNT)
        val amountCents = amountToCents(amount)
        val currentCount = prefs.getInt(KEY_TOTAL_COUNT, 0)
        val newRecord = PaymentRecord(amount, sender, System.currentTimeMillis())

        val history = getPaymentHistory(context).toMutableList()
        history.add(newRecord)
        val historyJson = Json.encodeToString(history)

        // Also append to the rolling 7-day history
        val multiDay = decodeHistory(prefs.getString(KEY_MULTI_DAY_HISTORY, "[]") ?: "[]").toMutableList()
        multiDay.add(newRecord)
        val multiDayJson = Json.encodeToString(multiDay)

        prefs.edit()
            .putFloat(KEY_TOTAL_AMOUNT, centsToFloat(currentTotalCents + amountCents))
            .putLong(KEY_TOTAL_AMOUNT_CENTS, currentTotalCents + amountCents)
            .putInt(KEY_TOTAL_COUNT, currentCount + 1)
            .putString(KEY_PAYMENTS_JSON, historyJson)
            .putString(KEY_MULTI_DAY_HISTORY, multiDayJson)
            .commit()

        _updates.tryEmit(Unit)
        PagoGlanceWidget.updateAll(context.applicationContext)
        return true
    }

    fun getPaymentHistory(context: Context): List<PaymentRecord> {
        resetIfNewDay(context)
        val json = prefs(context).getString(KEY_PAYMENTS_JSON, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getDailyTotal(context: Context): Float {
        resetIfNewDay(context)
        return centsToFloat(readAmountCents(prefs(context), KEY_TOTAL_AMOUNT_CENTS, KEY_TOTAL_AMOUNT))
    }

    fun getDailyCount(context: Context): Int {
        resetIfNewDay(context)
        return prefs(context).getInt(KEY_TOTAL_COUNT, 0)
    }

    fun getYesterdayTotal(context: Context): Float {
        resetIfNewDay(context)
        return centsToFloat(readAmountCents(prefs(context), KEY_YESTERDAY_TOTAL_CENTS, KEY_YESTERDAY_TOTAL))
    }

    fun getYesterdayCount(context: Context): Int {
        resetIfNewDay(context)
        return prefs(context).getInt(KEY_YESTERDAY_COUNT, 0)
    }

    fun getYesterdayHistory(context: Context): List<PaymentRecord> {
        resetIfNewDay(context)
        val json = prefs(context).getString(KEY_YESTERDAY_HISTORY, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getYesterdayDate(context: Context): String {
        resetIfNewDay(context)
        return prefs(context).getString(KEY_YESTERDAY_DATE, "---") ?: "---"
    }

    /**
     * Returns all payments from the last [MULTI_DAY_HISTORY_DAYS] days, sorted by timestamp DESC.
     * On first use, back-fills from today+yesterday so users don't lose data after upgrading.
     */
    fun getMultiDayHistory(context: Context): List<PaymentRecord> {
        resetIfNewDay(context)
        val prefs = prefs(context)

        val raw = decodeHistory(prefs.getString(KEY_MULTI_DAY_HISTORY, "[]") ?: "[]")

        // Back-fill: if multi_day_history is empty but we have today/yesterday data, seed it.
        if (raw.isEmpty()) {
            val today = getPaymentHistory(context)
            val yesterday = getYesterdayHistory(context)
            val seeded = (today + yesterday)
                .distinctBy { "${it.sender}-${it.amount}-${it.timestamp}" }
                .sortedByDescending { it.timestamp }
            if (seeded.isNotEmpty()) {
                prefs.edit().putString(KEY_MULTI_DAY_HISTORY, Json.encodeToString(seeded)).apply()
            }
            return seeded
        }

        return raw.sortedByDescending { it.timestamp }
    }

    fun isBatteryWarningDismissed(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BATTERY_WARNING_DISMISSED, false)

    fun setBatteryWarningDismissed(context: Context, dismissed: Boolean) {
        prefs(context)
            .edit()
            .putBoolean(KEY_BATTERY_WARNING_DISMISSED, dismissed)
            .apply()
    }

    fun getTtsVoiceName(context: Context): String? =
        prefs(context)
            .getString(KEY_TTS_VOICE_NAME, null)
            ?.takeIf { it.isNotBlank() }

    fun setTtsVoiceName(context: Context, voiceName: String?) {
        prefs(context)
            .edit()
            .apply {
                if (voiceName.isNullOrBlank()) remove(KEY_TTS_VOICE_NAME) else putString(KEY_TTS_VOICE_NAME, voiceName)
            }
            .apply()
    }

    fun getTtsSpeechRate(context: Context): Float =
        prefs(context)
            .getFloat(KEY_TTS_SPEECH_RATE, 0.85f)
            .coerceIn(0.6f, 1.8f)

    fun setTtsSpeechRate(context: Context, rate: Float) {
        prefs(context)
            .edit()
            .putFloat(KEY_TTS_SPEECH_RATE, rate.coerceIn(0.6f, 1.8f))
            .apply()
    }

    fun getTtsSpeechPitch(context: Context): Float =
        prefs(context)
            .getFloat(KEY_TTS_SPEECH_PITCH, 0.88f)
            .coerceIn(0.7f, 1.6f)

    fun setTtsSpeechPitch(context: Context, pitch: Float) {
        prefs(context)
            .edit()
            .putFloat(KEY_TTS_SPEECH_PITCH, pitch.coerceIn(0.7f, 1.6f))
            .apply()
    }

    fun isTtsAmountOnly(context: Context): Boolean =
        prefs(context)
            .getBoolean(KEY_TTS_AMOUNT_ONLY, false)

    fun setTtsAmountOnly(context: Context, amountOnly: Boolean) {
        prefs(context)
            .edit()
            .putBoolean(KEY_TTS_AMOUNT_ONLY, amountOnly)
            .apply()
    }

    fun getTtsRepeatCount(context: Context): Int =
        prefs(context)
            .getInt(KEY_TTS_REPEAT_COUNT, 1)
            .coerceIn(1, 3)

    fun setTtsRepeatCount(context: Context, repeatCount: Int) {
        prefs(context)
            .edit()
            .putInt(KEY_TTS_REPEAT_COUNT, repeatCount.coerceIn(1, 3))
            .apply()
    }

    fun resetTtsSettings(context: Context) {
        prefs(context)
            .edit()
            .remove(KEY_TTS_VOICE_NAME)
            .putFloat(KEY_TTS_SPEECH_RATE, 1f)
            .putFloat(KEY_TTS_SPEECH_PITCH, 1f)
            .putBoolean(KEY_TTS_AMOUNT_ONLY, false)
            .putInt(KEY_TTS_REPEAT_COUNT, 1)
            .apply()
    }

    private fun readAmountCents(
        prefs: android.content.SharedPreferences,
        centsKey: String,
        legacyFloatKey: String
    ): Long {
        return if (prefs.contains(centsKey)) {
            prefs.getLong(centsKey, 0L)
        } else {
            amountToCents(prefs.getFloat(legacyFloatKey, 0f).toDouble())
        }
    }

    private fun amountToCents(amount: Double): Long =
        (amount * 100).toBigDecimal().setScale(0, RoundingMode.HALF_UP).longValueExact()

    private fun centsToFloat(cents: Long): Float = cents.toFloat() / 100f

    private fun normalizeStoredHistory(prefs: SharedPreferences, today: LocalDate) {
        val storedToday = decodeHistory(prefs.getString(KEY_PAYMENTS_JSON, "[]") ?: "[]")
        val storedYesterday = decodeHistory(prefs.getString(KEY_YESTERDAY_HISTORY, "[]") ?: "[]")

        val combined = (storedToday + storedYesterday)
            .distinctBy { "${it.sender}-${it.amount}-${it.timestamp}" }
            .sortedBy { it.timestamp }

        val expectedToday = combined.filter { it.toLocalDate() == today }
        val expectedYesterday = combined.filter { it.toLocalDate() == today.minusDays(1) }
        val expectedTodayTotalCents = expectedToday.sumOf { amountToCents(it.amount) }
        val expectedYesterdayTotalCents = expectedYesterday.sumOf { amountToCents(it.amount) }
        val expectedLastResetDate = today.toString()
        val expectedYesterdayDate = today.minusDays(1).toString()

        val needsRewrite =
            storedToday != expectedToday ||
                storedYesterday != expectedYesterday ||
                prefs.getInt(KEY_TOTAL_COUNT, 0) != expectedToday.size ||
                prefs.getInt(KEY_YESTERDAY_COUNT, 0) != expectedYesterday.size ||
                readAmountCents(prefs, KEY_TOTAL_AMOUNT_CENTS, KEY_TOTAL_AMOUNT) != expectedTodayTotalCents ||
                readAmountCents(prefs, KEY_YESTERDAY_TOTAL_CENTS, KEY_YESTERDAY_TOTAL) != expectedYesterdayTotalCents ||
                prefs.getString(KEY_LAST_RESET_DATE, null) != expectedLastResetDate ||
                prefs.getString(KEY_YESTERDAY_DATE, null) != expectedYesterdayDate

        if (!needsRewrite) return

        prefs.edit()
            .putString(KEY_PAYMENTS_JSON, Json.encodeToString(expectedToday))
            .putFloat(KEY_TOTAL_AMOUNT, centsToFloat(expectedTodayTotalCents))
            .putLong(KEY_TOTAL_AMOUNT_CENTS, expectedTodayTotalCents)
            .putInt(KEY_TOTAL_COUNT, expectedToday.size)
            .putString(KEY_YESTERDAY_HISTORY, Json.encodeToString(expectedYesterday))
            .putFloat(KEY_YESTERDAY_TOTAL, centsToFloat(expectedYesterdayTotalCents))
            .putLong(KEY_YESTERDAY_TOTAL_CENTS, expectedYesterdayTotalCents)
            .putInt(KEY_YESTERDAY_COUNT, expectedYesterday.size)
            .putString(KEY_LAST_RESET_DATE, expectedLastResetDate)
            .putString(KEY_YESTERDAY_DATE, expectedYesterdayDate)
            .apply()

        _updates.tryEmit(Unit)
    }

    private fun decodeHistory(json: String): List<PaymentRecord> =
        try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }

    private fun pruneMultiDayHistory(prefs: SharedPreferences, today: LocalDate) {
        val cutoff = today.minusDays(MULTI_DAY_HISTORY_DAYS)
        val raw = decodeHistory(prefs.getString(KEY_MULTI_DAY_HISTORY, "[]") ?: "[]")
        val pruned = raw.filter {
            val date = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            !date.isBefore(cutoff)
        }
        if (pruned.size != raw.size) {
            prefs.edit().putString(KEY_MULTI_DAY_HISTORY, Json.encodeToString(pruned)).apply()
        }
    }

    private fun PaymentRecord.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

    /**
     * Checks if a payment with the same amount and sender was processed in the last 15 seconds.
     * This prevents double-announcing when captured via both NotificationListener and AccessibilityService.
     */
    private fun isDuplicate(context: Context, amount: Double, sender: String): Boolean {
        val now = System.currentTimeMillis()
        val history = getPaymentHistory(context)
        val multiDay = getMultiDayHistory(context)
        
        // Check today's history
        val isMatch = (history + multiDay).any { 
            it.amount == amount && 
            it.sender.equals(sender, ignoreCase = true) && 
            Math.abs(now - it.timestamp) < 15_000L 
        }
        return isMatch
    }
}
