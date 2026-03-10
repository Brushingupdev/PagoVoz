package com.example.pagovoz

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class PagoNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private val tag = "PagoVozListener"
    private val recentNotifications = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        logDebug("Servicio creado")
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.forLanguageTag("es-PE")
            ttsReady = true
            logDebug("TTS inicializado")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!SessionManager.isActive(this)) return

        val packageName = sbn.packageName
        if (!PaymentNotificationParser.isSupportedPackage(packageName)) return

        val uniqueKey = "${packageName}_${sbn.postTime}_${sbn.id}"
        if (recentNotifications.contains(uniqueKey)) return
        recentNotifications.add(uniqueKey)
        if (recentNotifications.size > 50) recentNotifications.clear()

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val fullText = "$title $text $bigText".replace("\n", " ").trim()

        val parsed = PaymentNotificationParser.parse(packageName, fullText)
        if (parsed == null) {
            logDebug("No se reconocio un formato de pago compatible para $packageName")
            return
        }

        logDebug("Pago detectado desde $packageName")
        SessionManager.addPayment(this, parsed.amount, parsed.sender)

        if (ttsReady) {
            val appName = if (packageName == PaymentNotificationParser.YAPE_PACKAGE) "Yape" else "Plin"
            val naturalAmount = convertAmountToNatural(parsed.amount)
            val message = "${parsed.sender} te ha enviado un pago de $naturalAmount por $appName."
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "pago_id")
        }
    }

    private fun convertAmountToNatural(amount: Double): String {
        val integerPart = amount.toInt()
        val cents = Math.round((amount - integerPart) * 100).toInt()

        val integerText = when {
            integerPart == 0 -> ""
            integerPart == 1 -> "un sol"
            else -> "${numberToWords(integerPart)} soles"
        }

        val centsText = when {
            cents == 0 -> ""
            cents == 1 -> "con un céntimo"
            else -> "con ${numberToWords(cents)} céntimos"
        }

        return "$integerText $centsText".trim()
    }

    private fun numberToWords(n: Int): String {
        val units = arrayOf("", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve")
        val specials = arrayOf("diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve")
        val tens = arrayOf("", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa")

        if (n < 10) return units[n]
        if (n < 20) return specials[n - 10]
        if (n < 100) {
            val t = tens[n / 10]
            val u = if (n % 10 == 0) "" else " y ${units[n % 10]}"
            return "$t$u"
        }
        return n.toString()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
