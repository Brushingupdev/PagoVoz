package com.example.pagovoz

import java.util.Locale

object PaymentSourceResolver {

    fun resolveAppName(packageName: String, fullText: String): String {
        val normalized = fullText.lowercase(Locale.ROOT)
        val mentionsPlin = normalized.contains("plin") || normalized.contains("plineo") || normalized.contains("plineó")
        val mentionsYape = normalized.contains("yape") || normalized.contains("yapeo") || normalized.contains("yapeó")

        return when {
            mentionsPlin -> "Plin"
            packageName == PaymentNotificationParser.PLIN_PACKAGE -> "Plin"
            mentionsYape -> "Yape"
            packageName == PaymentNotificationParser.YAPE_PACKAGE -> "Yape"
            else -> "Pago"
        }
    }
}
