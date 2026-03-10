package com.example.pagovoz

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
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

    private val _updates = MutableSharedFlow<Unit>(replay = 0)
    val updates = _updates.asSharedFlow()

    fun isActive(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ACTIVE, false)

    fun setActive(context: Context, active: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ACTIVE, active).apply()
    }

    fun isTrialModalShown(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_TRIAL_MODAL_SHOWN, false)

    fun setTrialModalShown(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TRIAL_MODAL_SHOWN, true).apply()
    }

    fun isPremium(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PREMIUM, false)

    fun setPremium(context: Context, premium: Boolean, premiumUntil: String? = null) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putBoolean(KEY_PREMIUM, premium)
            if (premiumUntil != null) putString(KEY_PREMIUM_UNTIL, premiumUntil) else remove(KEY_PREMIUM_UNTIL)
            putLong(KEY_LAST_PREMIUM_CHECK, System.currentTimeMillis())
        }.apply()
    }

    fun getPremiumDaysLeft(context: Context): Int {
        val premiumUntilStr = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PREMIUM_UNTIL, null) ?: return 0
        return try {
            val expiry = Instant.parse(premiumUntilStr)
            val days = ChronoUnit.DAYS.between(Instant.now(), expiry)
            days.toInt().coerceAtLeast(0)
        } catch (_: Exception) {
            0
        }
    }

    fun resetIfNewDay(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now()
        val lastDate = prefs.getString(KEY_LAST_RESET_DATE, null)

        if (DailyResetPolicy.shouldReset(lastDate, today)) {
            val lastTotal = getDailyTotal(context)
            val lastCount = getDailyCount(context)
            val lastHistory = prefs.getString(KEY_PAYMENTS_JSON, "[]") ?: "[]"

            prefs.edit()
                .putFloat(KEY_YESTERDAY_TOTAL, lastTotal)
                .putLong(KEY_YESTERDAY_TOTAL_CENTS, readAmountCents(prefs, KEY_TOTAL_AMOUNT_CENTS, KEY_TOTAL_AMOUNT))
                .putInt(KEY_YESTERDAY_COUNT, lastCount)
                .putString(KEY_YESTERDAY_HISTORY, lastHistory)
                .putString(KEY_YESTERDAY_DATE, lastDate)
                .apply()

            resetDailyTotals(context)
        }
    }

    fun resetDailyTotals(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_TOTAL_AMOUNT, 0f)
            .putLong(KEY_TOTAL_AMOUNT_CENTS, 0L)
            .putInt(KEY_TOTAL_COUNT, 0)
            .putString(KEY_PAYMENTS_JSON, "[]")
            .putString(KEY_LAST_RESET_DATE, LocalDate.now().toString())
            .apply()
        _updates.tryEmit(Unit)
    }

    fun addPayment(context: Context, amount: Double, sender: String) {
        resetIfNewDay(context)

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentTotalCents = readAmountCents(prefs, KEY_TOTAL_AMOUNT_CENTS, KEY_TOTAL_AMOUNT)
        val amountCents = amountToCents(amount)
        val currentCount = prefs.getInt(KEY_TOTAL_COUNT, 0)

        val history = getPaymentHistory(context).toMutableList()
        history.add(PaymentRecord(amount, sender, System.currentTimeMillis()))
        val historyJson = Json.encodeToString(history)

        prefs.edit()
            .putFloat(KEY_TOTAL_AMOUNT, centsToFloat(currentTotalCents + amountCents))
            .putLong(KEY_TOTAL_AMOUNT_CENTS, currentTotalCents + amountCents)
            .putInt(KEY_TOTAL_COUNT, currentCount + 1)
            .putString(KEY_PAYMENTS_JSON, historyJson)
            .apply()

        _updates.tryEmit(Unit)
    }

    fun getPaymentHistory(context: Context): List<PaymentRecord> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PAYMENTS_JSON, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getDailyTotal(context: Context): Float =
        centsToFloat(
            readAmountCents(
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE),
                KEY_TOTAL_AMOUNT_CENTS,
                KEY_TOTAL_AMOUNT
            )
        )

    fun getDailyCount(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_TOTAL_COUNT, 0)

    fun getYesterdayTotal(context: Context): Float =
        centsToFloat(
            readAmountCents(
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE),
                KEY_YESTERDAY_TOTAL_CENTS,
                KEY_YESTERDAY_TOTAL
            )
        )

    fun getYesterdayCount(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_YESTERDAY_COUNT, 0)

    fun getYesterdayHistory(context: Context): List<PaymentRecord> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_YESTERDAY_HISTORY, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getYesterdayDate(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_YESTERDAY_DATE, "---") ?: "---"

    fun isBatteryWarningDismissed(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BATTERY_WARNING_DISMISSED, false)

    fun setBatteryWarningDismissed(context: Context, dismissed: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BATTERY_WARNING_DISMISSED, dismissed)
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
}
