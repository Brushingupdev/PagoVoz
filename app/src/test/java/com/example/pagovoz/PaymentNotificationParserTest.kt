package com.example.pagovoz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentNotificationParserTest {

    // ─────────────────────────────────────────────
    // YAPE — casos existentes
    // ─────────────────────────────────────────────

    @Test
    fun `parse Yape confirmation with sender and amount`() {
        val text = "Confirmación de pago Yape! Juan Perez te envió S/ 12.50 por Yape"
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
    fun `parse Yape sender first with yapeo wording`() {
        val text = "Lucia te yapeó S/ 22.90"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Lucia", result.sender)
        assertEquals(22.90, result.amount, 0.001)
    }

    @Test
    fun `parse Yape text coming from unknown package`() {
        val text = "Confirmación de pago Yape! Ana Torres te envió S/ 18.40 por Yape"
        val result = PaymentNotificationParser.parse("com.some.bank", text)
        requireNotNull(result)
        assertEquals("Ana Torres", result.sender)
        assertEquals(18.40, result.amount, 0.001)
    }

    @Test
    fun `parse mojibake Yape text`() {
        val text = "ConfirmaciÃ³n de pago Yape! Rosa DÃ­az te enviÃ³ S/ 15.40 por Yape"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Rosa Díaz", result.sender)
        assertEquals(15.40, result.amount, 0.001)
    }

    // ─────────────────────────────────────────────
    // YAPE — casos nuevos / edge cases
    // ─────────────────────────────────────────────

    @Test
    fun `parse Yape integer amount without decimals`() {
        val text = "Pedro te envió S/ 50 por Yape"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Pedro", result.sender)
        assertEquals(50.0, result.amount, 0.001)
    }

    @Test
    fun `parse Yape amount with comma separator`() {
        val text = "Recibiste S/ 100,00 de Jorge Mamani"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Jorge Mamani", result.sender)
        assertEquals(100.0, result.amount, 0.001)
    }

    @Test
    fun `parse Yape with compound sender name`() {
        val text = "Maria del Carmen te envió S/ 35.00 por Yape"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        // cleanSender toma max 3 palabras; "del" se normaliza en minúsculas
        assertTrue(result.sender.isNotBlank())
        assertEquals(35.0, result.amount, 0.001)
    }

    @Test
    fun `parse Yape exclamation prefix format`() {
        val text = "Yape! S/ 8.00 de Carlos Quispe"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Carlos Quispe", result.sender)
        assertEquals(8.0, result.amount, 0.001)
    }

    @Test
    fun `parse Yape received with has recibido wording`() {
        val text = "Has recibido un Yape S/ 45.50 de Ana Flores"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Ana Flores", result.sender)
        assertEquals(45.50, result.amount, 0.001)
    }

    // ─────────────────────────────────────────────
    // PLIN — casos existentes
    // ─────────────────────────────────────────────

    @Test
    fun `parse Plin format`() {
        val text = "Carlos te plineó S/ 8.90"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Carlos", result.sender)
        assertEquals(8.90, result.amount, 0.001)
    }

    @Test
    fun `parse Plin received format`() {
        val text = "Recibiste un Plin de S/ 14.20 de Carla Ramos"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Carla Ramos", result.sender)
        assertEquals(14.20, result.amount, 0.001)
    }

    @Test
    fun `parse Plin sender first with explicit plin wording`() {
        val text = "Luis te envio un Plin de S/ 5.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Luis", result.sender)
        assertEquals(5.00, result.amount, 0.001)
    }

    @Test
    fun `parse Plin wording coming from Yape package`() {
        val text = "Mario te envio un Plin de S/ 11.30"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Mario", result.sender)
        assertEquals(11.30, result.amount, 0.001)
    }

    @Test
    fun `parse Plin te llego format`() {
        val text = "Te llego un Plin de S/ 7.50 de Jose Perez"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Jose Perez", result.sender)
        assertEquals(7.50, result.amount, 0.001)
    }

    @Test
    fun `parse Plin text coming from unknown package`() {
        val text = "Recibiste un Plin de S/ 14.20 de Carla Ramos"
        val result = PaymentNotificationParser.parse("com.some.bank", text)
        requireNotNull(result)
        assertEquals("Carla Ramos", result.sender)
        assertEquals(14.20, result.amount, 0.001)
    }

    @Test
    fun `parse mojibake Plin text`() {
        val text = "Carlos te plineÃ³ S/ 8.90"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Carlos", result.sender)
        assertEquals(8.90, result.amount, 0.001)
    }

    // ─────────────────────────────────────────────
    // PLIN — casos nuevos / edge cases
    // ─────────────────────────────────────────────

    @Test
    fun `parse Plin integer amount`() {
        val text = "Sofia te plineó S/ 200"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Sofia", result.sender)
        assertEquals(200.0, result.amount, 0.001)
    }

    @Test
    fun `parse Plin prefix format`() {
        val text = "Plin recibido: S/ 30.00 de Roberto Silva"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Roberto Silva", result.sender)
        assertEquals(30.0, result.amount, 0.001)
    }

    @Test
    fun `parse Plin te transferio format`() {
        val text = "Claudia te transfirió S/ 60.00 por Plin"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        requireNotNull(result)
        assertEquals("Claudia", result.sender)
        assertEquals(60.0, result.amount, 0.001)
    }

    // ─────────────────────────────────────────────
    // Genérico + shouldInspect
    // ─────────────────────────────────────────────

    @Test
    fun `parse generic payment when only amount and sender tail are available`() {
        val text = "Pago recibido S/ 9.50 de Cliente Demo"
        val result = PaymentNotificationParser.parse("com.some.bank", text)
        requireNotNull(result)
        assertEquals("Cliente Demo", result.sender)
        assertEquals(9.50, result.amount, 0.001)
    }

    @Test
    fun `return null for unrelated text`() {
        val text = "Tu estado de cuenta está listo para revisar"
        val result = PaymentNotificationParser.parse("com.fake.app", text)
        assertNull(result)
    }

    @Test
    fun `return null when no amount is present`() {
        val text = "te envió un regalo de cumpleaños"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull(result)
    }

    @Test
    fun `inspect unknown package when payment hints are present`() {
        val shouldInspect = PaymentNotificationParser.shouldInspect(
            packageName = "com.some.bank",
            fullText = "Confirmación de pago Yape! Ana te envió S/ 8.00"
        )
        assertTrue(shouldInspect)
    }

    @Test
    fun `shouldInspect is true for supported yape package regardless of text`() {
        assertTrue(PaymentNotificationParser.shouldInspect(PaymentNotificationParser.YAPE_PACKAGE, "cualquier texto"))
    }

    @Test
    fun `shouldInspect is true for supported plin package regardless of text`() {
        assertTrue(PaymentNotificationParser.shouldInspect(PaymentNotificationParser.PLIN_PACKAGE, "cualquier texto"))
    }

    @Test
    fun `shouldInspect is false for unknown package without hints`() {
        assertFalse(PaymentNotificationParser.shouldInspect("com.fake.app", "Descuento en tu próxima compra"))
    }
}
