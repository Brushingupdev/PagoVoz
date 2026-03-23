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
    val isLoading: Boolean = false,
    val isCodeFormatValid: Boolean = false
)

sealed interface ActivationEvent {
    data object Activated : ActivationEvent
}

class ActivationViewModel(
    private val licenseRepository: LicenseRepository,
    private val emptyCodeError: String,
    private val codeFormatError: String,
    private val invalidCodeError: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ActivationEvent>()
    val events = _events.asSharedFlow()

    fun onCodeChanged(code: String) {
        val normalizedCode = normalizeActivationCode(code)
        _uiState.update {
            it.copy(
                code = normalizedCode,
                error = "",
                isCodeFormatValid = isActivationCodeFormatValid(normalizedCode)
            )
        }
    }

    fun activate() {
        val code = _uiState.value.code.trim()
        if (code.isBlank()) {
            _uiState.update { it.copy(error = emptyCodeError) }
            return
        }
        if (!isActivationCodeFormatValid(code)) {
            _uiState.update { it.copy(error = codeFormatError) }
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

private const val ACTIVATION_CODE_GROUP_SIZE = 4
private const val ACTIVATION_CODE_GROUPS = 3
private const val ACTIVATION_CODE_RAW_MAX_LENGTH = ACTIVATION_CODE_GROUP_SIZE * ACTIVATION_CODE_GROUPS
private val ACTIVATION_CODE_REGEX = Regex("^YAPE-[A-Z0-9]{4}-[A-Z0-9]{4}$")

fun normalizeActivationCode(input: String): String {
    val rawCode = buildString {
        input.uppercase().forEach { character ->
            if (character.isLetterOrDigit() && length < ACTIVATION_CODE_RAW_MAX_LENGTH) {
                append(character)
            }
        }
    }

    return rawCode.chunked(ACTIVATION_CODE_GROUP_SIZE).joinToString(separator = "-")
}

fun isActivationCodeFormatValid(code: String): Boolean = ACTIVATION_CODE_REGEX.matches(code)
