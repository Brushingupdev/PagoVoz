package com.example.pagovoz

data class ParsedPayment(
    val amount: Double,
    val sender: String
)

object PaymentNotificationParser {
    const val YAPE_PACKAGE = "com.bcp.innovacxion.yapeapp"
    const val PLIN_PACKAGE = "pe.interbank.plin"
    private val supportedPackages = setOf(YAPE_PACKAGE, PLIN_PACKAGE)
    private val relevantHints = listOf(
        "yape",
        "plin",
        "plineo",
        "plineó",
        "confirmacion de pago",
        "confirmación de pago",
        "recibiste",
        "has recibido",
        "te llego",
        "te llegó",
        "te envio",
        "te envió",
        "ha enviado"
    )

    fun isSupportedPackage(packageName: String): Boolean {
        return packageName in supportedPackages
    }

    fun shouldInspect(packageName: String, fullText: String): Boolean {
        if (isSupportedPackage(packageName)) return true

        val normalized = normalizeText(fullText).lowercase()
        return relevantHints.any { hint -> normalized.contains(hint) }
    }

    fun parse(packageName: String, fullText: String): ParsedPayment? {
        val normalized = normalizeText(fullText)
        val parsed = when (packageName) {
            YAPE_PACKAGE -> parseYape(normalized) ?: parsePlin(normalized)
            PLIN_PACKAGE -> parsePlin(normalized) ?: parseYape(normalized)
            else -> {
                when {
                    mentionsPlin(normalized) -> parsePlin(normalized) ?: parseYape(normalized)
                    mentionsYape(normalized) -> parseYape(normalized) ?: parsePlin(normalized)
                    else -> parseYape(normalized) ?: parsePlin(normalized)
                }
            }
        }

        return parsed ?: parseGenericPayment(normalized)
    }

    private fun parseYape(text: String): ParsedPayment? {
        val amountPattern = """S/\s*\.?\s*(\d+(?:[\.,]\d{1,2})?)"""
        val regex1 = Regex(
            """(?:Confirmaci[o\u00F3]n de pago(?: Yape!)?\s+)?(.+?)\s+te\s+(?:envi[o\u00F3]|ha enviado|mand[o\u00F3]|yape[o\u00F3])(?:\s+un pago por)?\s+$amountPattern(?:\s+por\s+Yape)?""",
            RegexOption.IGNORE_CASE
        )
        val regex2 = Regex("""(?:Recibiste|Has recibido|Te lleg[o\u00F3])(?:\s+un)?(?:\s+Yape)?\s+$amountPattern\s+de\s+(.+)$""", RegexOption.IGNORE_CASE)
        val regex3 = Regex("""Yape!\s+$amountPattern\s+de\s+(.+)$""", RegexOption.IGNORE_CASE)
        val regex4 = Regex("""(?:Yape(?:\s+recibido)?[:\s-]*)$amountPattern\s+(?:de|de parte de)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val regex5 = Regex("""(.+?)\s+te\s+yape[o\u00F3]\s+$amountPattern(?:\s|$)""", RegexOption.IGNORE_CASE)

        regex1.find(text)?.let {
            val senderRaw = it.groupValues[1].trim()
            val amount = it.groupValues[2].replace(",", ".").toDoubleOrNull()
            if (amount != null) return ParsedPayment(amount, cleanSender(senderRaw))
        }

        regex2.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val senderRaw = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(senderRaw))
        }

        regex3.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val senderRaw = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(senderRaw))
        }

        regex4.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val senderRaw = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(senderRaw))
        }

        regex5.find(text)?.let {
            val senderRaw = it.groupValues[1].trim()
            val amount = it.groupValues[2].replace(",", ".").toDoubleOrNull()
            if (amount != null) return ParsedPayment(amount, cleanSender(senderRaw))
        }

        return null
    }

    private fun parsePlin(text: String): ParsedPayment? {
        val amountPattern = """S/\s*\.?\s*(\d+(?:[\.,]\d{1,2})?)"""
        val receivedRegex = Regex(
            """(?:Recibiste|Has recibido|Te lleg[o\u00F3]|Plin recibido)(?:\s+un)?(?:\s+Plin)?(?:\s+de)?\s+$amountPattern\s+(?:de|de parte de)\s+(.+?)(?:\s+por\s+Plin)?$""",
            RegexOption.IGNORE_CASE
        )
        val senderFirstRegex = Regex(
            """(.+?)\s+te\s+(?:pline[o\u00F3]|envi[o\u00F3]|ha enviado|mand[o\u00F3]|transferi[o\u00F3]|transfiri[o\u00F3]|pag[o\u00F3])(?:\s+un\s+Plin|\s+por\s+Plin)?(?:\s+de)?\s+$amountPattern(?:\s+por\s+Plin)?(?:\s|$)""",
            RegexOption.IGNORE_CASE
        )
        val plinPrefixRegex = Regex(
            """Plin(?:\s+recibido)?[:\s-]*(?:de\s+)?$amountPattern\s+(?:de|de parte de)\s+(.+)$""",
            RegexOption.IGNORE_CASE
        )
        val genericPlinRegex = Regex(
            """(?:Te\s+lleg[o\u00F3]|Recibiste)\s+(?:un\s+)?Plin\s+de\s+$amountPattern(?:\s+de)?\s+(.+)$""",
            RegexOption.IGNORE_CASE
        )

        receivedRegex.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val sender = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(sender))
        }

        senderFirstRegex.find(text)?.let {
            val sender = it.groupValues[1].trim()
            val amount = it.groupValues[2].replace(",", ".").toDoubleOrNull()
            if (amount != null) return ParsedPayment(amount, cleanSender(sender))
        }

        plinPrefixRegex.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val sender = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(sender))
        }

        genericPlinRegex.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val sender = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(sender))
        }

        return null
    }

    private fun cleanSender(name: String): String {
        val normalized = normalizeText(name)
            .replace("Confirmaci\u00F3n de pago Yape!", "", ignoreCase = true)
            .replace("Confirmaci\u00F3n de Pago Yape!", "", ignoreCase = true)
            .replace("\u00A1Yape!", "", ignoreCase = true)
            .replace("Yape!", "", ignoreCase = true)
            .replace("por Plin", "", ignoreCase = true)
            .replace("Plin recibido", "", ignoreCase = true)
            .replace("Recibiste un Plin de", "", ignoreCase = true)
            .replace("Has recibido un Plin de", "", ignoreCase = true)
            .trimEnd('.', ',', ';', ':')
            .trim()

        val words = normalized
            .split(" ")
            .map { token -> token.trim(' ', '.', ',', ';', ':', '-', '_', '(', ')') }
            .filter { token -> token.isNotBlank() && token.any(Char::isLetter) }
            .take(3)

        if (words.isEmpty()) return normalized

        return words.joinToString(" ") { word ->
            when (word.lowercase()) {
                "de", "del", "la", "las", "los", "y" -> word.lowercase()
                else -> word.lowercase().replaceFirstChar { it.titlecase() }
            }
        }
    }

    private fun parseGenericPayment(text: String): ParsedPayment? {
        val amount = Regex("""S/\s*\.?\s*(\d+(?:[\.,]\d{1,2})?)""", RegexOption.IGNORE_CASE)
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(",", ".")
            ?.toDoubleOrNull()
            ?: return null

        val senderPatterns = listOf(
            Regex("""de\s+([A-Za-zÁÉÍÓÚÑáéíóúñ0-9 .'-]{2,})$""", RegexOption.IGNORE_CASE),
            Regex("""(.+?)\s+te\s+(?:envi[oó]|envio|ha enviado|mand[oó]|mando|yape[oó]|yapeo|pline[oó]|plineo|transferi[oó]|transfirió)\b""", RegexOption.IGNORE_CASE)
        )

        val sender = senderPatterns.firstNotNullOfOrNull { regex ->
            regex.find(text)?.groupValues?.getOrNull(1)?.let(::cleanSender)?.takeIf { it.isNotBlank() }
        } ?: "Cliente"

        return ParsedPayment(amount = amount, sender = sender)
    }

    private fun normalizeText(value: String): String {
        return value
            .replace("\n", " ")
            .replace("Ã³", "\u00F3")
            .replace("Ã“", "\u00D3")
            .replace("Ã­", "\u00ED")
            .replace("Ã", "\u00CD")
            .replace("Ã¡", "\u00E1")
            .replace("Ã", "\u00C1")
            .replace("Ã©", "\u00E9")
            .replace("Ã‰", "\u00C9")
            .replace("Ãº", "\u00FA")
            .replace("Ãš", "\u00DA")
            .replace("Ã±", "\u00F1")
            .replace("Ã‘", "\u00D1")
            .replace("Â¡", "\u00A1")
            .replace("Ã‚Â¡", "\u00A1")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun mentionsYape(text: String): Boolean {
        val normalized = text.lowercase()
        return normalized.contains("yape")
    }

    private fun mentionsPlin(text: String): Boolean {
        val normalized = text.lowercase()
        return normalized.contains("plin") || normalized.contains("plineo") || normalized.contains("plineó")
    }
}
