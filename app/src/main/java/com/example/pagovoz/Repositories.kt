package com.example.pagovoz

import android.content.Context
import kotlinx.coroutines.flow.SharedFlow

interface SessionRepository {
    val updates: SharedFlow<Unit>
    fun isActive(): Boolean
    fun setActive(active: Boolean)
    fun isTrialModalShown(): Boolean
    fun setTrialModalShown()
    fun isPremium(): Boolean
    fun getPremiumDaysLeft(): Int
    fun resetIfNewDay()
    fun resetDailyTotals()
    fun getDailyTotal(): Float
    fun getDailyCount(): Int
    fun getPaymentHistory(): List<PaymentRecord>
    fun getYesterdayDate(): String
    fun getYesterdayTotal(): Float
    fun getYesterdayCount(): Int
    fun getYesterdayHistory(): List<PaymentRecord>
    fun getMultiDayHistory(): List<PaymentRecord>
}

class DefaultSessionRepository(
    private val context: Context
) : SessionRepository {
    override val updates: SharedFlow<Unit> = SessionManager.updates
    override fun isActive(): Boolean = SessionManager.isActive(context)
    override fun setActive(active: Boolean) = SessionManager.setActive(context, active)
    override fun isTrialModalShown(): Boolean = SessionManager.isTrialModalShown(context)
    override fun setTrialModalShown() = SessionManager.setTrialModalShown(context)
    override fun isPremium(): Boolean = SessionManager.isPremium(context)
    override fun getPremiumDaysLeft(): Int = SessionManager.getPremiumDaysLeft(context)
    override fun resetIfNewDay() = SessionManager.resetIfNewDay(context)
    override fun resetDailyTotals() = SessionManager.resetDailyTotals(context)
    override fun getDailyTotal(): Float = SessionManager.getDailyTotal(context)
    override fun getDailyCount(): Int = SessionManager.getDailyCount(context)
    override fun getPaymentHistory(): List<PaymentRecord> = SessionManager.getPaymentHistory(context)
    override fun getYesterdayDate(): String = SessionManager.getYesterdayDate(context)
    override fun getYesterdayTotal(): Float = SessionManager.getYesterdayTotal(context)
    override fun getYesterdayCount(): Int = SessionManager.getYesterdayCount(context)
    override fun getYesterdayHistory(): List<PaymentRecord> = SessionManager.getYesterdayHistory(context)
    override fun getMultiDayHistory(): List<PaymentRecord> = SessionManager.getMultiDayHistory(context)
}

interface LicenseRepository {
    suspend fun validateCode(code: String): Boolean
    suspend fun checkPremiumStatus(force: Boolean = false): Boolean
    fun listenForPremiumChanges()
}

class DefaultLicenseRepository(
    private val context: Context
) : LicenseRepository {
    override suspend fun validateCode(code: String): Boolean = SupabaseManager.validarCodigo(context, code)
    override suspend fun checkPremiumStatus(force: Boolean): Boolean = SupabaseManager.checkPremiumStatus(context, force)
    override fun listenForPremiumChanges() = SupabaseManager.listenForPremiumChanges(context)
}

interface UpdateRepository {
    suspend fun fetchAppConfig(): AppConfig?
}

class DefaultUpdateRepository : UpdateRepository {
    override suspend fun fetchAppConfig(): AppConfig? = SupabaseManager.checkAppUpdate()
}
