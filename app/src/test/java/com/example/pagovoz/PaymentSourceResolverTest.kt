package com.example.pagovoz

import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentSourceResolverTest {

    @Test
    fun `detecta Yape por paquete oficial`() {
        val result = PaymentSourceResolver.resolveAppName(
            PaymentNotificationParser.YAPE_PACKAGE,
            "Notificacion generica"
        )

        assertEquals("Yape", result)
    }

    @Test
    fun `detecta Yape por texto aunque la notificacion venga desde otra app`() {
        val result = PaymentSourceResolver.resolveAppName(
            "com.example.pagovoz",
            "Recibiste un Yape S/.18.50 de Rosa Diaz"
        )

        assertEquals("Yape", result)
    }

    @Test
    fun `detecta Plin por texto`() {
        val result = PaymentSourceResolver.resolveAppName(
            "com.example.pagovoz",
            "Recibiste un Plin de S/.32.00 de parte de Luis Ramirez"
        )

        assertEquals("Plin", result)
    }

    @Test
    fun `usa Pago como respaldo cuando no hay senales claras`() {
        val result = PaymentSourceResolver.resolveAppName(
            "com.example.pagovoz",
            "Cobro recibido"
        )

        assertEquals("Pago", result)
    }
}
