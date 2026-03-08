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
    fun `should reset when last date is different from today`() {
        val today = LocalDate.of(2026, 3, 7)
        assertTrue(DailyResetPolicy.shouldReset("2026-03-06", today))
    }
}
