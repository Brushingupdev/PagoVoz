package com.example.pagovoz

data class ParsedPayment(
    val amount: Double,
    val sender: String
)

object PaymentNotificationParser {
    const val YAPE_PACKAGE = "com.bcp.innovacxion.yapeapp"
    const val PLIN_PACKAGE = "pe.interbank.plin"

    fun isSupportedPackage(packageName: String): Boolean {
        return packageName == YAPE_PACKAGE || packageName == PLIN_PACKAGE
    }

    fun parse(packageName: String, fullText: String): ParsedPayment? {
        val normalized = normalizeText(fullText)
        return when (packageName) {
            YAPE_PACKAGE -> parseYape(normalized)
            PLIN_PACKAGE -> parsePlin(normalized)
            else -> null
        }
    }

    private fun parseYape(text: String): ParsedPayment? {
        val amountPattern = """S/\.?\s*(\d+(?:[\.,]\d{1,2})?)"""
        val regex1 = Regex(
            """(?:Confirmaci[o\u00F3]n de pago(?: Yape!)?\s+)?(.+?)\s+te\s+(?:envi\u00F3|ha enviado)(?:\s+un pago por)?\s+$amountPattern(?:\s+por\s+Yape)?""",
            RegexOption.IGNORE_CASE
        )
        val regex2 = Regex("""(?:Recibiste|Has recibido)\s+$amountPattern\s+de\s+(.+)$""", RegexOption.IGNORE_CASE)
        val regex3 = Regex("""Yape!\s+$amountPattern\s+de\s+(.+)$""", RegexOption.IGNORE_CASE)

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

        return null
    }

    private fun parsePlin(text: String): ParsedPayment? {
        val amountPattern = """S/\.?\s*(\d+(?:[\.,]\d{1,2})?)"""
        val regex1 = Regex("""$amountPattern\s+(?:de|de parte de)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val regex2 = Regex("""(.+?)\s+te\s+(?:pline\u00F3|envi\u00F3)\s+$amountPattern(?:\s|$)""", RegexOption.IGNORE_CASE)

        regex1.find(text)?.let {
            val amount = it.groupValues[1].replace(",", ".").toDoubleOrNull()
            val sender = it.groupValues[2].trim()
            if (amount != null) return ParsedPayment(amount, cleanSender(sender))
        }

        regex2.find(text)?.let {
            val sender = it.groupValues[1].trim()
            val amount = it.groupValues[2].replace(",", ".").toDoubleOrNull()
            if (amount != null) return ParsedPayment(amount, cleanSender(sender))
        }

        return null
    }

    private fun cleanSender(name: String): String {
        return normalizeText(name)
            .replace("Confirmaci\u00F3n de pago Yape!", "", ignoreCase = true)
            .replace("Confirmaci\u00F3n de Pago Yape!", "", ignoreCase = true)
            .replace("\u00A1Yape!", "", ignoreCase = true)
            .replace("Yape!", "", ignoreCase = true)
            .trimEnd('.', ',', ';', ':')
            .trim()
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
}
