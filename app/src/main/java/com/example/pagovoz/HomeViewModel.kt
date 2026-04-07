package com.example.pagovoz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isPermissionEnabled: Boolean = false,
    val dailyTotal: Float = 0f,
    val yesterdayTotal: Float = 0f,
    val dailyCount: Int = 0,
    val recentPayments: List<PaymentRecord> = emptyList(),
    val showDeleteConfirm: Boolean = false,
    val showTrialModal: Boolean = false,
    val isPremium: Boolean = false,
    val trialDays: Int = 0
)

class HomeViewModel(
    private val sessionRepository: SessionRepository,
    private val licenseRepository: LicenseRepository,
    private val isNotificationEnabled: () -> Boolean
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshFromSession()
        observeSessionUpdates()
    }

    private fun observeSessionUpdates() {
        viewModelScope.launch {
            sessionRepository.updates.collectLatest {
                refreshFromSession()
            }
        }
    }

    private fun refreshFromSession() {
        _uiState.update {
            it.copy(
                isPermissionEnabled = isNotificationEnabled(),
                dailyTotal = sessionRepository.getDailyTotal(),
                yesterdayTotal = sessionRepository.getYesterdayTotal(),
                dailyCount = sessionRepository.getDailyCount(),
                recentPayments = sessionRepository.getPaymentHistory()
                    .sortedByDescending { payment -> payment.timestamp }
                    .take(3),
                showTrialModal = !sessionRepository.isTrialModalShown(),
                isPremium = sessionRepository.isPremium(),
                trialDays = sessionRepository.getPremiumDaysLeft()
            )
        }
    }

    fun onResume() {
        sessionRepository.resetIfNewDay()
        refreshFromSession()
        viewModelScope.launch {
            val premiumStatus = licenseRepository.checkPremiumStatus(force = BuildConfig.DEBUG)
            _uiState.update {
                it.copy(
                    isPremium = premiumStatus,
                    trialDays = sessionRepository.getPremiumDaysLeft()
                )
            }
        }
    }

    fun dismissTrialModal() {
        sessionRepository.setTrialModalShown()
        _uiState.update { it.copy(showTrialModal = false) }
    }

    fun showDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDeleteHistory() {
        sessionRepository.resetDailyTotals()
        _uiState.update { it.copy(showDeleteConfirm = false) }
        refreshFromSession()
    }
}
