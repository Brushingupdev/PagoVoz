package com.example.pagovoz

import java.time.LocalDate

object DailyResetPolicy {
    fun shouldReset(lastResetDate: String?, today: LocalDate): Boolean {
        return lastResetDate != null && lastResetDate != today.toString()
    }
}
