package com.example.pagovoz

import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `blank code sets empty error`() = runTest {
        val viewModel = ActivationViewModel(
            licenseRepository = FakeActivationLicenseRepository(validateResult = false),
            emptyCodeError = "EMPTY",
            invalidCodeError = "INVALID"
        )

        viewModel.activate()

        assertEquals("EMPTY", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `invalid code sets invalid error`() = runTest {
        val viewModel = ActivationViewModel(
            licenseRepository = FakeActivationLicenseRepository(validateResult = false),
            emptyCodeError = "EMPTY",
            invalidCodeError = "INVALID"
        )
        viewModel.onCodeChanged("abc")

        viewModel.activate()

        assertEquals("INVALID", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `valid code clears error and loading`() = runTest {
        val viewModel = ActivationViewModel(
            licenseRepository = FakeActivationLicenseRepository(validateResult = true),
            emptyCodeError = "EMPTY",
            invalidCodeError = "INVALID"
        )
        viewModel.onCodeChanged("ok")

        viewModel.activate()

        assertEquals("", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
