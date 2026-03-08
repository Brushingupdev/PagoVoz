package com.example.pagovoz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReportsUiState(
    val selectedTab: Int = 0,
    val isToday: Boolean = true,
    val reportDate: String = "",
    val reportTotal: Float = 0f,
    val reportCount: Int = 0,
    val reportHistory: List<PaymentRecord> = emptyList(),
    val hasYesterdayData: Boolean = true
)

class ReportsViewModel(
    private val sessionRepository: SessionRepository,
    private val todayProvider: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshReport()
        observeSessionUpdates()
    }

    private fun observeSessionUpdates() {
        viewModelScope.launch {
            sessionRepository.updates.collectLatest {
                refreshReport()
            }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
        refreshReport()
    }

    private fun refreshReport() {
        val selectedTab = _uiState.value.selectedTab
        val isToday = selectedTab == 0
        val reportDate = if (isToday) todayProvider().toString() else sessionRepository.getYesterdayDate()
        val reportHistory = if (isToday) sessionRepository.getPaymentHistory() else sessionRepository.getYesterdayHistory()
        _uiState.update {
            it.copy(
                isToday = isToday,
                reportDate = reportDate,
                reportTotal = if (isToday) sessionRepository.getDailyTotal() else sessionRepository.getYesterdayTotal(),
                reportCount = if (isToday) sessionRepository.getDailyCount() else sessionRepository.getYesterdayCount(),
                reportHistory = reportHistory,
                hasYesterdayData = reportDate != "---"
            )
        }
    }
}
