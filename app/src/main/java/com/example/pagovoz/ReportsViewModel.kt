package com.example.pagovoz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class ReportRangeFilter(
    val label: String,
    val summaryLabel: String,
    val fileKey: String
) {
    Today(label = "Hoy", summaryLabel = "dia", fileKey = "hoy"),
    Week(label = "Semana", summaryLabel = "semana", fileKey = "semana"),
    Month(label = "Mes", summaryLabel = "mes", fileKey = "mes")
}

data class ReportsUiState(
    val selectedTab: Int = 0,
    val isToday: Boolean = true,
    val selectedRange: ReportRangeFilter = ReportRangeFilter.Today,
    val reportDate: String = "",
    val reportTotal: Float = 0f,
    val reportCount: Int = 0,
    val reportHistory: List<PaymentRecord> = emptyList(),
    val hasYesterdayData: Boolean = true,
    val rangeNotice: String? = null
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
        val filter = when (tab) {
            0 -> ReportRangeFilter.Today
            1 -> ReportRangeFilter.Week
            else -> ReportRangeFilter.Month
        }
        onRangeSelected(filter)
    }

    fun onRangeSelected(filter: ReportRangeFilter) {
        _uiState.update { it.copy(selectedRange = filter) }
        refreshReport()
    }

    private fun refreshReport() {
        val selectedRange = _uiState.value.selectedRange
        val today = todayProvider()
        val todayHistory = sessionRepository.getPaymentHistory()
            .sortedBy { it.timestamp }
        val multiDayHistory = sessionRepository.getMultiDayHistory()
            .sortedBy { it.timestamp }
        val rangeStart = when (selectedRange) {
            ReportRangeFilter.Today -> today
            ReportRangeFilter.Week -> today.minusDays(6)
            ReportRangeFilter.Month -> today.minusDays(29)
        }
        val reportHistory = when (selectedRange) {
            ReportRangeFilter.Today -> todayHistory
            ReportRangeFilter.Week,
            ReportRangeFilter.Month -> multiDayHistory.filter { record ->
                val recordDate = record.toLocalDate()
                !recordDate.isBefore(rangeStart) && !recordDate.isAfter(today)
            }
        }
        val oldestAvailableDate = multiDayHistory.firstOrNull()?.toLocalDate()
        val isMonthDataLimited = selectedRange == ReportRangeFilter.Month &&
            oldestAvailableDate != null &&
            oldestAvailableDate.isAfter(rangeStart)

        _uiState.update {
            it.copy(
                selectedTab = when (selectedRange) {
                    ReportRangeFilter.Today -> 0
                    ReportRangeFilter.Week -> 1
                    ReportRangeFilter.Month -> 2
                },
                isToday = selectedRange == ReportRangeFilter.Today,
                selectedRange = selectedRange,
                reportDate = buildReportDateLabel(
                    filter = selectedRange,
                    today = today,
                    history = reportHistory
                ),
                reportTotal = reportHistory.sumOf { record -> record.amount }.toFloat(),
                reportCount = reportHistory.size,
                reportHistory = reportHistory,
                hasYesterdayData = reportHistory.isNotEmpty(),
                rangeNotice = if (isMonthDataLimited) {
                    "Mostrando el historial reciente disponible para este mes."
                } else {
                    null
                }
            )
        }
    }
}

private fun buildReportDateLabel(
    filter: ReportRangeFilter,
    today: LocalDate,
    history: List<PaymentRecord>
): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "PE"))
    return when (filter) {
        ReportRangeFilter.Today -> today.format(formatter)
        ReportRangeFilter.Week -> {
            val start = today.minusDays(6)
            "${start.format(formatter)} - ${today.format(formatter)}"
        }
        ReportRangeFilter.Month -> {
            val earliestAvailable = history
                .firstOrNull()
                ?.toLocalDate()
                ?.takeIf { it.isAfter(today.minusDays(29)) }
                ?: today.minusDays(29)
            "${earliestAvailable.format(formatter)} - ${today.format(formatter)}"
        }
    }
}

private fun PaymentRecord.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
