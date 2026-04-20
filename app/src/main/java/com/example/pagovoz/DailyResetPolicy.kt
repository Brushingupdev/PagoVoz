package com.example.pagovoz

import java.time.LocalDate

object DailyResetPolicy {
    fun shouldReset(lastResetDate: String?, today: LocalDate): Boolean {
        if (lastResetDate == null) return false
        return lastResetDate != today.toString()
    }
}
