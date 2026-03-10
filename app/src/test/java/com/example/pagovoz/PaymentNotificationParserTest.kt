package com.example.pagovoz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentNotificationParserTest {

    @Test
    fun `parse Yape confirmation with sender and amount`() {
        val text = "Confirmaci\u00F3n de pago Yape! Juan Perez te envi\u00F3 S/ 12.50 por Yape"

        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)

        requireNotNull(result)
        assertEquals("Juan Perez", result.sender)
        assertEquals(12.50, result.amount, 0.001)
    }

    @Test
    fun `parse Yape received format`() {
        val text = "Recibiste S/ 20,30 de Maria Lopez"

        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)

        requireNotNull(result)
        assertEquals("Maria Lopez", result.sender)
        assertEquals(20.30, result.amount, 0.001)
    }

    @Test
    fun `parse Plin format`() {
        val text = "Carlos te pline\u00F3 S/ 8.90"

        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)

        requireNotNull(result)
        assertEquals("Carlos", result.sender)
        assertEquals(8.90, result.amount, 0.001)
    }

    @Test
    fun `return null for unsupported package`() {
        val text = "Recibiste S/ 100 de Cliente"

        val result = PaymentNotificationParser.parse("com.fake.app", text)

        assertNull(result)
    }

    @Test
    fun `parse mojibake Yape text`() {
        val text = "ConfirmaciÃ³n de pago Yape! Rosa DÃ­az te enviÃ³ S/ 15.40 por Yape"

        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)

        requireNotNull(result)
        assertEquals("Rosa D\u00EDaz", result.sender)
        assertEquals(15.40, result.amount, 0.001)
    }

    @Test
    fun `parse mojibake Plin text`() {
        val text = "Carlos te plineÃ³ S/ 8.90"

        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)

        requireNotNull(result)
        assertEquals("Carlos", result.sender)
        assertEquals(8.90, result.amount, 0.001)
    }
}
