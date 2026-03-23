package com.example.pagovoz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppNavigationUiState(
    val currentScreen: String = "home",
    val isPremium: Boolean = false,
    val historyOpenedFromRecent: Boolean = false
)

class AppNavigationViewModel(
    private val sessionRepository: SessionRepository,
    private val licenseRepository: LicenseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppNavigationUiState())
    val uiState = _uiState.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        refreshPremium()
        observeSessionUpdates()
        runCatching {
            licenseRepository.listenForPremiumChanges()
        }
    }

    private fun observeSessionUpdates() {
        viewModelScope.launch {
            sessionRepository.updates.collectLatest {
                refreshPremium()
            }
        }
    }

    private fun refreshPremium() {
        _uiState.update { it.copy(isPremium = sessionRepository.isPremium()) }
    }

    fun openHome() {
        _uiState.update { it.copy(currentScreen = "home", historyOpenedFromRecent = false) }
    }

    fun openHistory() {
        _uiState.update { it.copy(currentScreen = "history", historyOpenedFromRecent = false) }
    }

    fun openHistoryFromRecent() {
        _uiState.update { it.copy(currentScreen = "history", historyOpenedFromRecent = true) }
    }

    fun openPayments() {
        _uiState.update { it.copy(currentScreen = "payments", historyOpenedFromRecent = false) }
    }

    fun openReports() {
        _uiState.update { it.copy(currentScreen = "reports", historyOpenedFromRecent = false) }
    }

    fun openVoiceSettings() {
        _uiState.update { it.copy(currentScreen = "voice_settings", historyOpenedFromRecent = false) }
    }

    fun openProfile() {
        _uiState.update { it.copy(currentScreen = "profile", historyOpenedFromRecent = false) }
    }

    fun openPremium() {
        refreshPremium()
        _uiState.update { it.copy(currentScreen = "premium", historyOpenedFromRecent = false) }
    }
}
