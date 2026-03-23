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
    var multiDayHistoryValue: List<PaymentRecord> = emptyList()

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
    override fun getMultiDayHistory(): List<PaymentRecord> = multiDayHistoryValue

    suspend fun emitUpdate() { _updates.emit(Unit) }
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

    // ─────────────────────────────────────────────
    // Estado inicial
    // ─────────────────────────────────────────────

    @Test
    fun `initial state reflects repository values`() = runTest {
        val session = FakeSessionRepository().apply {
            dailyTotalValue = 25.5f
            dailyCountValue = 3
            trialModalShownValue = false
            premiumValue = true
            premiumDaysValue = 7
        }
        val viewModel = makeViewModel(session, premiumStatus = true)

        val state = viewModel.uiState.value
        assertEquals(25.5f, state.dailyTotal)
        assertEquals(3, state.dailyCount)
        assertTrue(state.showTrialModal)
        assertTrue(state.isPremium)
        assertEquals(7, state.trialDays)
        assertTrue(state.isPermissionEnabled)
    }

    @Test
    fun `initial state when notification permission is disabled`() = runTest {
        val viewModel = HomeViewModel(
            sessionRepository = FakeSessionRepository(),
            licenseRepository = FakeHomeLicenseRepository(premiumStatus = false),
            isNotificationEnabled = { false }
        )

        assertFalse(viewModel.uiState.value.isPermissionEnabled)
    }

    @Test
    fun `trial modal is hidden when already shown`() = runTest {
        val session = FakeSessionRepository().apply { trialModalShownValue = true }
        val viewModel = makeViewModel(session)

        assertFalse(viewModel.uiState.value.showTrialModal)
    }

    // ─────────────────────────────────────────────
    // Acciones del usuario
    // ─────────────────────────────────────────────

    @Test
    fun `dismiss trial modal updates state and repository`() = runTest {
        val session = FakeSessionRepository().apply { trialModalShownValue = false }
        val viewModel = makeViewModel(session)

        viewModel.dismissTrialModal()

        assertTrue(session.trialModalShownValue)
        assertFalse(viewModel.uiState.value.showTrialModal)
    }

    @Test
    fun `show delete confirm dialog`() = runTest {
        val viewModel = makeViewModel(FakeSessionRepository())

        viewModel.showDeleteConfirm()

        assertTrue(viewModel.uiState.value.showDeleteConfirm)
    }

    @Test
    fun `dismiss delete confirm dialog`() = runTest {
        val viewModel = makeViewModel(FakeSessionRepository())
        viewModel.showDeleteConfirm()

        viewModel.dismissDeleteConfirm()

        assertFalse(viewModel.uiState.value.showDeleteConfirm)
    }

    @Test
    fun `confirm delete resets totals and closes dialog`() = runTest {
        val session = FakeSessionRepository().apply {
            dailyTotalValue = 10f
            dailyCountValue = 2
        }
        val viewModel = makeViewModel(session)
        viewModel.showDeleteConfirm()

        viewModel.confirmDeleteHistory()

        val state = viewModel.uiState.value
        assertEquals(0f, state.dailyTotal)
        assertEquals(0, state.dailyCount)
        assertFalse(state.showDeleteConfirm)
    }

    // ─────────────────────────────────────────────
    // onResume y actualización de premium
    // ─────────────────────────────────────────────

    @Test
    fun `onResume updates premium status from repository`() = runTest {
        val session = FakeSessionRepository().apply {
            premiumValue = false
            premiumDaysValue = 0
        }
        // El repositorio de licencias dice que ahora sí es premium
        val viewModel = HomeViewModel(
            sessionRepository = session,
            licenseRepository = FakeHomeLicenseRepository(premiumStatus = true),
            isNotificationEnabled = { false }
        )

        viewModel.onResume()

        assertTrue(viewModel.uiState.value.isPremium)
    }

    @Test
    fun `onResume keeps non-premium when repository returns false`() = runTest {
        val session = FakeSessionRepository().apply { premiumValue = false }
        val viewModel = makeViewModel(session, premiumStatus = false)

        viewModel.onResume()

        assertFalse(viewModel.uiState.value.isPremium)
    }

    // ─────────────────────────────────────────────
    // recentPayments — limitado a 4 y ordenado DESC
    // ─────────────────────────────────────────────

    @Test
    fun `recent payments limited to 4 most recent`() = runTest {
        val payments = (1..6).map { i ->
            PaymentRecord(amount = i.toDouble(), sender = "Sender $i", timestamp = i.toLong())
        }
        val session = FakeSessionRepository().apply { paymentHistoryValue = payments }
        val viewModel = makeViewModel(session)

        val recent = viewModel.uiState.value.recentPayments

        assertEquals(4, recent.size)
        // Los más recientes tienen timestamps más altos (6, 5, 4, 3)
        assertEquals(6.0, recent[0].amount, 0.001)
        assertEquals(5.0, recent[1].amount, 0.001)
    }

    @Test
    fun `recent payments is empty when no payments today`() = runTest {
        val session = FakeSessionRepository().apply { paymentHistoryValue = emptyList() }
        val viewModel = makeViewModel(session)

        assertTrue(viewModel.uiState.value.recentPayments.isEmpty())
    }

    @Test
    fun `recent payments shows all when less than 4`() = runTest {
        val payments = listOf(
            PaymentRecord(10.0, "Ana", 1L),
            PaymentRecord(20.0, "Luis", 2L)
        )
        val session = FakeSessionRepository().apply { paymentHistoryValue = payments }
        val viewModel = makeViewModel(session)

        assertEquals(2, viewModel.uiState.value.recentPayments.size)
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private fun makeViewModel(
        session: FakeSessionRepository = FakeSessionRepository(),
        premiumStatus: Boolean = false
    ) = HomeViewModel(
        sessionRepository = session,
        licenseRepository = FakeHomeLicenseRepository(premiumStatus),
        isNotificationEnabled = { true }
    )
}
