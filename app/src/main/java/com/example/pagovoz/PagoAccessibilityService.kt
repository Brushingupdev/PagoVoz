package com.example.pagovoz

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log
import android.os.Bundle
import android.media.AudioAttributes
import android.media.AudioManager
import java.util.Locale

class PagoAccessibilityService : AccessibilityService(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private val tag = "PagoAccessibility"
    private val parser = PaymentNotificationParser

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "Accessibility Service conectado")
        tts = TextToSpeech(applicationContext, this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!SessionManager.isActive(this)) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                handleNotificationEvent(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED, AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowEvent(event)
            }
        }
    }

    private fun handleNotificationEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val parcelableData = event.parcelableData
        
        if (parcelableData is Notification) {
            val extras = parcelableData.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val fullText = "$title $text $bigText"

            processText(packageName, fullText)
        } else {
            // Might be a Toast
            val text = event.text.joinToString(" ")
            if (text.isNotBlank()) {
                processText(packageName, text)
            }
        }
    }

    private fun handleWindowEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName != PaymentNotificationParser.YAPE_PACKAGE && packageName != PaymentNotificationParser.PLIN_PACKAGE) return

        val rootNode = rootInActiveWindow ?: return
        val textList = mutableListOf<String>()
        extractText(rootNode, textList)
        
        val fullContent = textList.distinct().joinToString(" ")
        if (fullContent.isNotBlank()) {
            processText(packageName, fullContent)
        }
    }

    private fun extractText(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            list.add(text)
        }
        for (i in 0 until node.childCount) {
            extractText(node.getChild(i), list)
        }
    }

    private fun processText(packageName: String, fullText: String) {
        if (parser.shouldInspect(packageName, fullText)) {
            val parsed = parser.parse(packageName, fullText)
            if (parsed != null) {
                if (SessionManager.addPayment(this, parsed.amount, parsed.sender)) {
                    Log.d(tag, "Pago capturado via Accessibility: ${parsed.amount} de ${parsed.sender}")
                    announcePayment(packageName, parsed.amount, parsed.sender, fullText)
                }
            }
        }
    }

    private fun announcePayment(packageName: String, amount: Double, sender: String, fullText: String) {
        if (!ttsReady) return
        
        applySpeechSettings()
        val appName = PaymentSourceResolver.resolveAppName(packageName, fullText)
        val naturalAmount = convertAmountToNatural(amount)
        
        val message = if (SessionManager.isPremium(this) && SessionManager.isTtsAmountOnly(this)) {
            "$appName!, $naturalAmount."
        } else {
            val spokenSender = humanizeSenderForSpeech(sender)
            val verb = if (appName.lowercase() == "yape") "te ha yapeado" else "te ha pagado"
            "$appName!, $spokenSender $verb $naturalAmount."
        }

        val repeatCount = if (SessionManager.isPremium(this)) SessionManager.getTtsRepeatCount(this) else 1
        val repeatedMessage = List(repeatCount) { message }.joinToString(" ... ")

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        tts.speak(repeatedMessage, TextToSpeech.QUEUE_FLUSH, params, "acc_pago_${System.currentTimeMillis()}")
    }

    private fun applySpeechSettings() {
        val rate = SessionManager.getTtsSpeechRate(this)
        val pitch = SessionManager.getTtsSpeechPitch(this)
        tts.setSpeechRate(rate)
        tts.setPitch(pitch)
        
        val voiceName = SessionManager.getTtsVoiceName(this)
        val selectedVoice = tts.voices?.firstOrNull { it.name == voiceName }
        if (selectedVoice != null) {
            tts.voice = selectedVoice
        } else {
            tts.language = Locale("es", "PE")
        }
    }

    private fun convertAmountToNatural(amount: Double): String {
        val integerPart = amount.toInt()
        val cents = Math.round((amount - integerPart) * 100).toInt()
        val solesText = if (integerPart == 1) "un sol" else "${numberToWords(integerPart)} soles"
        val centsText = if (cents == 0) "" else "con ${numberToWords(cents)} céntimos"
        return "$solesText $centsText".trim()
    }

    private fun numberToWords(n: Int): String {
        val units = arrayOf("", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve")
        val specials = arrayOf("diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve")
        val tens = arrayOf("", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa")
        if (n < 10) return units[n]
        if (n < 20) return specials[n - 10]
        if (n < 100) return tens[n / 10] + (if (n % 10 == 0) "" else " y ${units[n % 10]}")
        return n.toString()
    }

    private fun humanizeSenderForSpeech(sender: String): String {
        return sender.split(" ").take(2).joinToString(" ")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            applySpeechSettings()
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
