package com.example.pagovoz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActivationUiState(
    val code: String = "",
    val error: String = "",
    val isLoading: Boolean = false
)

sealed interface ActivationEvent {
    data object Activated : ActivationEvent
}

class ActivationViewModel(
    private val licenseRepository: LicenseRepository,
    private val emptyCodeError: String,
    private val invalidCodeError: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ActivationEvent>()
    val events = _events.asSharedFlow()

    fun onCodeChanged(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun activate() {
        val code = _uiState.value.code.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(error = emptyCodeError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            val isValid = licenseRepository.validateCode(code)
            if (isValid) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(ActivationEvent.Activated)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = invalidCodeError
                    )
                }
            }
        }
    }
}
