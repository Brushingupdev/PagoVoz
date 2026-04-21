package com.example.pagovoz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentNotificationGateTest {

    @Test
    fun `no marca como procesada una notificacion incompleta antes de su actualizacion valida`() {
        val gate = PaymentNotificationGate()
        val key = "same-key"

        val first = gate.evaluate(
            packageName = PaymentNotificationParser.YAPE_PACKAGE,
            fullText = "Recibiste un Yape",
            notificationKey = key
        )
        assertTrue(first is PaymentNotificationGateResult.Ignored)
        assertEquals("parse_null", (first as PaymentNotificationGateResult.Ignored).reason)

        val updated = gate.evaluate(
            packageName = PaymentNotificationParser.YAPE_PACKAGE,
            fullText = "Recibiste un Yape S/.21.30 de Micaela Torres",
            notificationKey = key
        )

        assertTrue(updated is PaymentNotificationGateResult.Accepted)
        val payment = (updated as PaymentNotificationGateResult.Accepted).payment
        assertEquals(21.30, payment.amount, 0.01)
        assertEquals("Micaela Torres", payment.sender)
    }

    @Test
    fun `ignora duplicado cuando la notificacion valida ya fue procesada`() {
        val gate = PaymentNotificationGate()
        val key = "processed-key"
        val text = "Recibiste un Yape S/.18.50 de Rosa Diaz"

        val first = gate.evaluate(
            packageName = PaymentNotificationParser.YAPE_PACKAGE,
            fullText = text,
            notificationKey = key
        )
        assertTrue(first is PaymentNotificationGateResult.Accepted)

        val duplicate = gate.evaluate(
            packageName = PaymentNotificationParser.YAPE_PACKAGE,
            fullText = text,
            notificationKey = key
        )

        assertTrue(duplicate is PaymentNotificationGateResult.Ignored)
        assertEquals("duplicate_processed", (duplicate as PaymentNotificationGateResult.Ignored).reason)
    }

    @Test
    fun `filtra promociones antes de parsear`() {
        val gate = PaymentNotificationGate()

        val result = gate.evaluate(
            packageName = PaymentNotificationParser.YAPE_PACKAGE,
            fullText = "Participa por S/.100 en este proximo sorteo",
            notificationKey = "promo-key"
        )

        assertTrue(result is PaymentNotificationGateResult.Ignored)
        assertEquals("filtered_before_parse", (result as PaymentNotificationGateResult.Ignored).reason)
    }
}
