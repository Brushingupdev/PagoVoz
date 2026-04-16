package com.example.pagovoz

import org.junit.Assert.*
import org.junit.Test

class PaymentNotificationParserTest {

    // ═══════════════════════════════════════════════════════════════
    //  PAGOS REALES DE YAPE — deben ser detectados correctamente
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `Yape - Confirmacion de pago clasica`() {
        val text = "Confirmación de pago Yape! Juan Perez te envió S/.50.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar pago Yape clásico", result)
        assertEquals(50.0, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Recibiste un Yape`() {
        val text = "Recibiste un Yape S/.25.00 de Maria Garcia"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'Recibiste un Yape'", result)
        assertEquals(25.0, result!!.amount, 0.01)
        assertTrue(result.sender.contains("Maria", ignoreCase = true))
    }

    @Test
    fun `Yape - Has recibido un Yape`() {
        val text = "Has recibido un Yape S/.10.50 de Carlos Lopez"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'Has recibido un Yape'", result)
        assertEquals(10.5, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Te llego un Yape`() {
        val text = "Te llegó un Yape S/.100 de Ana Torres"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'Te llegó un Yape'", result)
        assertEquals(100.0, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Te yapearon`() {
        val text = "Pedro te yapeó S/.15.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'te yapeó'", result)
        assertEquals(15.0, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Yape recibido con monto`() {
        val text = "Yape recibido: S/.75.00 de Luis Ramirez"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'Yape recibido:'", result)
        assertEquals(75.0, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Yape! con monto`() {
        val text = "Yape! S/.30.00 de Rosa Mendez"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'Yape! S/...'", result)
        assertEquals(30.0, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Monto con coma decimal`() {
        val text = "Recibiste un Yape S/.5,50 de Diego Flores"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería aceptar coma como decimal", result)
        assertEquals(5.5, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Monto sin punto antes de cifras`() {
        val text = "Recibiste un Yape S/ 200 de Carmen Rios"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería parsear S/ 200 sin punto", result)
        assertEquals(200.0, result!!.amount, 0.01)
    }

    @Test
    fun `Yape - Ha enviado un pago`() {
        val text = "Miguel ha enviado S/.42.00 por Yape"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería detectar 'ha enviado'", result)
        assertEquals(42.0, result!!.amount, 0.01)
    }

    // ═══════════════════════════════════════════════════════════════
    //  PAGOS REALES DE PLIN — deben ser detectados correctamente
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `Plin - Recibiste un Plin`() {
        val text = "Recibiste un Plin de S/.30.00 de parte de Jose Vargas"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNotNull("Debería detectar 'Recibiste un Plin'", result)
        assertEquals(30.0, result!!.amount, 0.01)
    }

    @Test
    fun `Plin - Te plinearon`() {
        val text = "Laura te plineó S/.20.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNotNull("Debería detectar 'te plineó'", result)
        assertEquals(20.0, result!!.amount, 0.01)
    }

    @Test
    fun `Plin - Plin recibido`() {
        val text = "Plin recibido: S/.45.00 de Martha Salazar"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNotNull("Debería detectar 'Plin recibido:'", result)
        assertEquals(45.0, result!!.amount, 0.01)
    }

    @Test
    fun `Plin - Has recibido un Plin`() {
        val text = "Has recibido un Plin de S/.60 de Ricardo Navarro"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNotNull("Debería detectar 'Has recibido un Plin'", result)
        assertEquals(60.0, result!!.amount, 0.01)
    }

    @Test
    fun `Plin - Te llego un Plin`() {
        val text = "Te llegó un Plin de S/.12.50 de Fernanda Cruz"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNotNull("Debería detectar 'Te llegó un Plin'", result)
        assertEquals(12.5, result!!.amount, 0.01)
    }

    @Test
    fun `Plin - Te enviaron por Plin`() {
        val text = "Andres te envió S/.80 por Plin"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNotNull("Debería detectar 'te envió ... por Plin'", result)
        assertEquals(80.0, result!!.amount, 0.01)
    }

    // ═══════════════════════════════════════════════════════════════
    //  NOTIFICACIONES PROMOCIONALES — NO deben ser captadas
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `Promo - Participa por sorteo BCP`() {
        val text = "Participa por S/.100 en este proximo sorteo"
        val result = PaymentNotificationParser.parse("com.bcp.banking", text)
        assertNull("NO debe captar promo de sorteo BCP", result)
    }

    @Test
    fun `Promo - Participa sorteo desde paquete Yape`() {
        val text = "Participa por S/.100 en este proximo sorteo"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar promo de sorteo aunque sea paquete Yape", result)
    }

    @Test
    fun `Promo - Gana cashback`() {
        val text = "¡Gana hasta S/.500 de cashback con tus compras!"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar oferta de cashback", result)
    }

    @Test
    fun `Promo - Descuento disponible`() {
        val text = "Descuento de S/.20 disponible para ti en tu próxima compra"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar descuento", result)
    }

    @Test
    fun `Promo - Cupon de regalo`() {
        val text = "Tienes un cupón de S/.15 de regalo. Úsalo hoy"
        val result = PaymentNotificationParser.parse("com.bcp.banking", text)
        assertNull("NO debe captar cupón de regalo", result)
    }

    @Test
    fun `Promo - Prestamo aprobado`() {
        val text = "Tu préstamo de S/.5000 está aprobado. Solicita ahora"
        val result = PaymentNotificationParser.parse("com.bcp.banking", text)
        assertNull("NO debe captar oferta de préstamo", result)
    }

    @Test
    fun `Promo - Oferta tarjeta de credito`() {
        val text = "Solicita tu tarjeta de crédito y obtén S/.200 de bienvenida"
        val result = PaymentNotificationParser.parse("com.bcp.banking", text)
        assertNull("NO debe captar oferta de tarjeta", result)
    }

    @Test
    fun `Promo - Acumula puntos`() {
        val text = "Acumula puntos y canjea premios por S/.50"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar programa de puntos", result)
    }

    @Test
    fun `Promo - Invita amigos Yape`() {
        val text = "Invita a tus amigos a Yape y gana S/.10 por cada uno"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar invitar amigos", result)
    }

    @Test
    fun `Promo - Registrate y gana`() {
        val text = "Regístrate y gana S/.30 de bienvenida con Yape"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar promo de registro", result)
    }

    @Test
    fun `Promo - Promocion Plin`() {
        val text = "Promoción Plin: Envía S/.1 y participa por S/.1000"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.PLIN_PACKAGE, text)
        assertNull("NO debe captar promo de Plin", result)
    }

    @Test
    fun `Promo - Deposito a plazo`() {
        val text = "Abre tu depósito a plazo desde S/.100 y gana intereses"
        val result = PaymentNotificationParser.parse("com.bcp.banking", text)
        assertNull("NO debe captar oferta de depósito a plazo", result)
    }

    @Test
    fun `Promo - Encuesta con premio`() {
        val text = "Califica tu experiencia y gana hasta S/.50 en premios"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar encuesta con premio", result)
    }

    @Test
    fun `Promo - Actualiza tu app`() {
        val text = "Actualiza tu app y descubre las nuevas funciones. Gana S/.5"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar actualización de app", result)
    }

    @Test
    fun `Promo - Ganaste un premio`() {
        val text = "¡Ganaste S/.200! Reclama tu premio ahora"
        val result = PaymentNotificationParser.parse("com.bcp.banking", text)
        assertNull("NO debe captar 'ganaste' falso", result)
    }

    @Test
    fun `Promo - Cashback Yape`() {
        val text = "Hoy tienes cashback de S/.3.00 por tus compras en Yape"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar cashback Yape", result)
    }

    @Test
    fun `Promo - Codigo de descuento`() {
        val text = "Usa el código YAPE10 y obtén S/.10 de descuento"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar código de descuento", result)
    }

    // ═══════════════════════════════════════════════════════════════
    //  NOTIFICACIONES DE OTROS PAQUETES SIN YAPE/PLIN
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `Otro paquete - sin mencion Yape ni Plin rechaza`() {
        val text = "Te enviaron S/.100 de tu cuenta de ahorros"
        val result = PaymentNotificationParser.parse("com.otro.banco", text)
        assertNull("NO debe captar pagos de apps desconocidas sin mención Yape/Plin", result)
    }

    @Test
    fun `Otro paquete - con mencion Yape acepta`() {
        val text = "Recibiste un Yape S/.35 de Fernando Diaz"
        val result = PaymentNotificationParser.parse("com.samsung.messages", text)
        assertNotNull("Debería captar si menciona Yape aunque sea otro paquete", result)
        assertEquals(35.0, result!!.amount, 0.01)
    }

    @Test
    fun `Otro paquete - con mencion Plin acepta`() {
        val text = "Te llegó un Plin de S/.22 de Sandra Vega"
        val result = PaymentNotificationParser.parse("com.whatsapp", text)
        assertNotNull("Debería captar si menciona Plin aunque sea otro paquete", result)
        assertEquals(22.0, result!!.amount, 0.01)
    }

    @Test
    fun `Otro paquete - SMS bancario generico con monto rechaza`() {
        val text = "Se realizó un cargo de S/.150.00 en tu tarjeta terminada en 4532"
        val result = PaymentNotificationParser.parse("com.android.mms", text)
        assertNull("NO debe captar cargos bancarios genéricos", result)
    }

    // ═══════════════════════════════════════════════════════════════
    //  shouldInspect — filtro previo
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `shouldInspect - paquete Yape con pago real pasa`() {
        val text = "Recibiste un Yape S/.10 de Juan"
        assertTrue(
            "shouldInspect debe pasar para paquete Yape con pago real",
            PaymentNotificationParser.shouldInspect(PaymentNotificationParser.YAPE_PACKAGE, text)
        )
    }

    @Test
    fun `shouldInspect - paquete Yape con promo rechaza`() {
        val text = "Participa por S/.100 en este sorteo"
        assertFalse(
            "shouldInspect debe rechazar promo incluso de paquete Yape",
            PaymentNotificationParser.shouldInspect(PaymentNotificationParser.YAPE_PACKAGE, text)
        )
    }

    @Test
    fun `shouldInspect - paquete Plin con promo rechaza`() {
        val text = "Gana cashback de S/.5 con Plin"
        assertFalse(
            "shouldInspect debe rechazar promo de Plin",
            PaymentNotificationParser.shouldInspect(PaymentNotificationParser.PLIN_PACKAGE, text)
        )
    }

    @Test
    fun `shouldInspect - paquete desconocido sin senal de pago rechaza`() {
        val text = "Tu saldo es S/.500.00"
        assertFalse(
            "shouldInspect debe rechazar notificaciones de saldo sin señal de pago",
            PaymentNotificationParser.shouldInspect("com.bcp.banking", text)
        )
    }

    @Test
    fun `shouldInspect - paquete desconocido con senal de pago pasa`() {
        val text = "Recibiste un Yape S/.50 de Maria"
        assertTrue(
            "shouldInspect debe pasar si hay señal de pago real",
            PaymentNotificationParser.shouldInspect("com.samsung.messages", text)
        )
    }

    @Test
    fun `shouldInspect - paquete Yape sin senal de pago ni promo pasa`() {
        // Paquete Yape siempre pasa shouldInspect si no es promo,
        // luego parse() verifica señal de pago.
        val text = "Mira las nuevas funciones de Yape"
        assertTrue(
            "shouldInspect debe pasar para paquete Yape sin promo (parse filtrará después)",
            PaymentNotificationParser.shouldInspect(PaymentNotificationParser.YAPE_PACKAGE, text)
        )
    }

    // ═══════════════════════════════════════════════════════════════
    //  PAQUETE YAPE SIN SEÑAL DE PAGO — parse debe rechazar
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `Yape - Notificacion sin senal de pago rechaza`() {
        val text = "Mira las nuevas funciones de Yape. Transfiere a S/.0 de comisión"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar notificaciones Yape informativas sin señal de pago", result)
    }

    @Test
    fun `Yape - Solo monto sin contexto rechaza`() {
        val text = "S/.100.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNull("NO debe captar un monto suelto sin señal de pago", result)
    }

    // ═══════════════════════════════════════════════════════════════
    //  EDGE CASES
    // ═══════════════════════════════════════════════════════════════

    @Test
    fun `Edge - Texto vacio`() {
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, "")
        assertNull("NO debe parsear texto vacío", result)
    }

    @Test
    fun `Edge - Solo espacios`() {
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, "   ")
        assertNull("NO debe parsear espacios", result)
    }

    @Test
    fun `Edge - Monto cero`() {
        val text = "Recibiste un Yape S/.0.00 de Test"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        // Monto 0 técnicamente parsea, depende del regex
        // Lo importante es que no crashee
        assertNotNull(result)
        assertEquals(0.0, result!!.amount, 0.01)
    }

    @Test
    fun `Edge - Monto con multiples lineas`() {
        val text = "Confirmación de pago Yape!\nJuan te envió S/.50.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería manejar saltos de línea", result)
        assertEquals(50.0, result!!.amount, 0.01)
    }

    @Test
    fun `Edge - Caracteres mal codificados`() {
        // Simula encoding roto común en Android
        val text = "ConfirmaciÃ³n de pago Yape! Pedro te enviÃ³ S/.20.00"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería manejar encoding roto (Ã³ → ó)", result)
        assertEquals(20.0, result!!.amount, 0.01)
    }

    @Test
    fun `Edge - Muchos espacios extra`() {
        val text = "Recibiste   un   Yape   S/.  15   de   Ana"
        val result = PaymentNotificationParser.parse(PaymentNotificationParser.YAPE_PACKAGE, text)
        assertNotNull("Debería manejar múltiples espacios", result)
        assertEquals(15.0, result!!.amount, 0.01)
    }
}
