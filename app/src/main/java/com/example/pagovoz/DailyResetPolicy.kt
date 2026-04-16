package com.example.pagovoz

import java.time.LocalDate

object DailyResetPolicy {
    fun shouldReset(lastResetDate: String?, today: LocalDate): Boolean {
        // Si es null es la primera vez, necesitamos inicializar la fecha
        if (lastResetDate == null) return true
        return lastResetDate != today.toString()
    }
}
