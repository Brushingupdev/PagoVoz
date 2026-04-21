package com.example.pagovoz

internal sealed interface PaymentNotificationGateResult {
    data class Accepted(val payment: ParsedPayment) : PaymentNotificationGateResult
    data class Ignored(val reason: String) : PaymentNotificationGateResult
}

internal class PaymentNotificationGate(
    private val maxProcessedKeys: Int = 200,
    private val shouldInspect: (packageName: String, fullText: String) -> Boolean = PaymentNotificationParser::shouldInspect,
    private val parsePayment: (packageName: String, fullText: String) -> ParsedPayment? = PaymentNotificationParser::parse
) {
    private val processedNotificationKeys = LinkedHashSet<String>()

    fun evaluate(
        packageName: String,
        fullText: String,
        notificationKey: String
    ): PaymentNotificationGateResult {
        if (!shouldInspect(packageName, fullText)) {
            return PaymentNotificationGateResult.Ignored("filtered_before_parse")
        }

        if (processedNotificationKeys.contains(notificationKey)) {
            return PaymentNotificationGateResult.Ignored("duplicate_processed")
        }

        val parsed = parsePayment(packageName, fullText)
            ?: return PaymentNotificationGateResult.Ignored("parse_null")

        markProcessed(notificationKey)
        return PaymentNotificationGateResult.Accepted(parsed)
    }

    private fun markProcessed(notificationKey: String) {
        if (processedNotificationKeys.size >= maxProcessedKeys) {
            processedNotificationKeys.clear()
        }
        processedNotificationKeys.add(notificationKey)
    }
}
