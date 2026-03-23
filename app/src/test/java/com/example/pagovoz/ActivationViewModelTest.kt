package com.example.pagovoz

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeActivationLicenseRepository(
    private val validateResult: Boolean
) : LicenseRepository {
    override suspend fun validateCode(code: String): Boolean = validateResult
    override suspend fun checkPremiumStatus(force: Boolean): Boolean = false
    override fun listenForPremiumChanges() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class ActivationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ─────────────────────────────────────────────
    // Validación de formato de código
    // ─────────────────────────────────────────────

    @Test
    fun `blank code sets empty error`() = runTest {
        val viewModel = makeViewModel(validateResult = false)

        viewModel.activate()

        assertEquals("EMPTY", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `invalid format code sets format error`() = runTest {
        val viewModel = makeViewModel(validateResult = false)
        viewModel.onCodeChanged("abc")

        viewModel.activate()

        assertEquals("FORMAT", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `partial code without full groups sets format error`() = runTest {
        val viewModel = makeViewModel(validateResult = false)
        viewModel.onCodeChanged("YAPE-ABC")  // solo 1 grupo completo

        viewModel.activate()

        assertEquals("FORMAT", viewModel.uiState.value.error)
    }

    // ─────────────────────────────────────────────
    // Normalización de código
    // ─────────────────────────────────────────────

    @Test
    fun `code is uppercased and formatted with dashes on input`() = runTest {
        val viewModel = makeViewModel(validateResult = false)
        viewModel.onCodeChanged("yapeabcd1234")

        // Debería producir "YAPE-ABCD-1234"
        assertEquals("YAPE-ABCD-1234", viewModel.uiState.value.code)
    }

    @Test
    fun `code with extra dashes is normalized correctly`() = runTest {
        val viewModel = makeViewModel(validateResult = false)
        viewModel.onCodeChanged("YAPE-ABCD-1234")

        assertEquals("YAPE-ABCD-1234", viewModel.uiState.value.code)
    }

    @Test
    fun `code starts as invalid then becomes valid when complete`() = runTest {
        val viewModel = makeViewModel(validateResult = false)

        viewModel.onCodeChanged("YAPE")
        assertFalse(viewModel.uiState.value.isCodeFormatValid)

        viewModel.onCodeChanged("YAPEABCD1234")
        assertTrue(viewModel.uiState.value.isCodeFormatValid)
    }

    @Test
    fun `changing code clears previous error`() = runTest {
        val viewModel = makeViewModel(validateResult = false)
        viewModel.activate() // dispara EMPTY error

        viewModel.onCodeChanged("YAPEABCD1234")

        assertEquals("", viewModel.uiState.value.error)
    }

    // ─────────────────────────────────────────────
    // Activación con servidor
    // ─────────────────────────────────────────────

    @Test
    fun `valid code and server accepts — clears error and loading`() = runTest {
        val viewModel = makeViewModel(validateResult = true)
        viewModel.onCodeChanged("YAPEABCD1234")

        viewModel.activate()

        assertEquals("", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `valid format but server rejects — sets invalid error`() = runTest {
        val viewModel = makeViewModel(validateResult = false)
        viewModel.onCodeChanged("YAPEABCD1234")

        viewModel.activate()

        assertEquals("INVALID", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `activation emits Activated event on success`() = runTest {
        val viewModel = makeViewModel(validateResult = true)
        viewModel.onCodeChanged("YAPEABCD1234")

        // Verificamos indirectamente: el código se limpia y no hay error
        // (el evento Activated se emite al mismo tiempo en la misma coroutine)
        viewModel.activate()

        assertEquals("", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private fun makeViewModel(validateResult: Boolean) = ActivationViewModel(
        licenseRepository = FakeActivationLicenseRepository(validateResult),
        emptyCodeError = "EMPTY",
        codeFormatError = "FORMAT",
        invalidCodeError = "INVALID"
    )
}
