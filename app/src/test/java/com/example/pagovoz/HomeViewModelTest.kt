package com.example.pagovoz

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeSessionRepository : SessionRepository {
    private val _updates = MutableSharedFlow<Unit>()
    override val updates: SharedFlow<Unit> = _updates

    var activeValue = false
    var trialModalShownValue = false
    var premiumValue = false
    var premiumDaysValue = 0
    var dailyTotalValue = 0f
    var dailyCountValue = 0
    var paymentHistoryValue: List<PaymentRecord> = emptyList()
    var yesterdayDateValue = "---"
    var yesterdayTotalValue = 0f
    var yesterdayCountValue = 0
    var yesterdayHistoryValue: List<PaymentRecord> = emptyList()

    override fun isActive(): Boolean = activeValue
    override fun setActive(active: Boolean) { this.activeValue = active }
    override fun isTrialModalShown(): Boolean = trialModalShownValue
    override fun setTrialModalShown() { trialModalShownValue = true }
    override fun isPremium(): Boolean = premiumValue
    override fun getPremiumDaysLeft(): Int = premiumDaysValue
    override fun resetIfNewDay() = Unit
    override fun resetDailyTotals() {
        dailyTotalValue = 0f
        dailyCountValue = 0
        paymentHistoryValue = emptyList()
    }
    override fun getDailyTotal(): Float = dailyTotalValue
    override fun getDailyCount(): Int = dailyCountValue
    override fun getPaymentHistory(): List<PaymentRecord> = paymentHistoryValue
    override fun getYesterdayDate(): String = yesterdayDateValue
    override fun getYesterdayTotal(): Float = yesterdayTotalValue
    override fun getYesterdayCount(): Int = yesterdayCountValue
    override fun getYesterdayHistory(): List<PaymentRecord> = yesterdayHistoryValue

    suspend fun emitUpdate() {
        _updates.emit(Unit)
    }
}

private class FakeHomeLicenseRepository(
    private val premiumStatus: Boolean
) : LicenseRepository {
    override suspend fun validateCode(code: String): Boolean = false
    override suspend fun checkPremiumStatus(force: Boolean): Boolean = premiumStatus
    override fun listenForPremiumChanges() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state reflects repository values`() = runTest {
        val session = FakeSessionRepository().apply {
            dailyTotalValue = 25.5f
            dailyCountValue = 3
            trialModalShownValue = false
            premiumValue = true
            premiumDaysValue = 7
        }
        val viewModel = HomeViewModel(
            sessionRepository = session,
            licenseRepository = FakeHomeLicenseRepository(premiumStatus = true),
            isNotificationEnabled = { true }
        )

        val state = viewModel.uiState.value
        assertEquals(25.5f, state.dailyTotal)
        assertEquals(3, state.dailyCount)
        assertTrue(state.showTrialModal)
        assertTrue(state.isPremium)
        assertEquals(7, state.trialDays)
        assertTrue(state.isPermissionEnabled)
    }

    @Test
    fun `dismiss trial modal updates state and repository`() = runTest {
        val session = FakeSessionRepository().apply { trialModalShownValue = false }
        val viewModel = HomeViewModel(
            sessionRepository = session,
            licenseRepository = FakeHomeLicenseRepository(premiumStatus = false),
            isNotificationEnabled = { false }
        )

        viewModel.dismissTrialModal()

        assertTrue(session.trialModalShownValue)
        assertFalse(viewModel.uiState.value.showTrialModal)
    }

    @Test
    fun `confirm delete resets totals and closes dialog`() = runTest {
        val session = FakeSessionRepository().apply {
            dailyTotalValue = 10f
            dailyCountValue = 2
        }
        val viewModel = HomeViewModel(
            sessionRepository = session,
            licenseRepository = FakeHomeLicenseRepository(premiumStatus = false),
            isNotificationEnabled = { false }
        )
        viewModel.showDeleteConfirm()

        viewModel.confirmDeleteHistory()

        val state = viewModel.uiState.value
        assertEquals(0f, state.dailyTotal)
        assertEquals(0, state.dailyCount)
        assertFalse(state.showDeleteConfirm)
    }
}
