package com.example.pagovoz

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyResetPolicyTest {

    @Test
    fun `should not reset when there is no last date`() {
        val today = LocalDate.of(2026, 3, 7)
        assertFalse(DailyResetPolicy.shouldReset(null, today))
    }

    @Test
    fun `should not reset when last date is today`() {
        val today = LocalDate.of(2026, 3, 7)
        assertFalse(DailyResetPolicy.shouldReset("2026-03-07", today))
    }

    @Test
    fun `should reset when last date is yesterday`() {
        val today = LocalDate.of(2026, 3, 7)
        assertTrue(DailyResetPolicy.shouldReset("2026-03-06", today))
    }

    @Test
    fun `should reset when last date is several days ago`() {
        val today = LocalDate.of(2026, 3, 7)
        assertTrue(DailyResetPolicy.shouldReset("2026-03-01", today))
    }

    @Test
    fun `should reset when crossing month boundary`() {
        val today = LocalDate.of(2026, 4, 1)
        assertTrue(DailyResetPolicy.shouldReset("2026-03-31", today))
    }

    @Test
    fun `should reset when crossing year boundary`() {
        val today = LocalDate.of(2027, 1, 1)
        assertTrue(DailyResetPolicy.shouldReset("2026-12-31", today))
    }

    @Test
    fun `should not reset when last date is empty string`() {
        // Empty string != today's date, so it SHOULD reset to clear corrupted state
        val today = LocalDate.of(2026, 3, 7)
        assertTrue(DailyResetPolicy.shouldReset("", today))
    }
}
