package com.example.pagovoz

import android.app.Notification
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class PagoNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private val tag = "HablaPagoListener"
    private val recentNotifications = mutableSetOf<String>()
    private val speechLock = Any()
    private val pendingAnnouncements = ArrayDeque<String>()
    private val pendingAnnouncementsBeforeTts = ArrayDeque<String>()
    private var isSpeakingAnnouncement = false
    private var utteranceCounter = 0L
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onCreate() {
        super.onCreate()
        logDebug("Servicio creado")
        ensureTtsInitialized()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        logDebug("Notification listener conectado")
        ensureTtsInitialized()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        logDebug("Notification listener desconectado; solicitando rebind")
        ttsReady = false
        NotificationListenerHelper.requestRebind(this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    handleAnnouncementCompleted()
                }

                override fun onError(utteranceId: String?) {
                    handleAnnouncementCompleted()
                }
            })
            applySpeechSettings()
            flushPendingAnnouncementsAfterTtsReady()
            logDebug("TTS inicializado")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!SessionManager.isActive(this)) return

        val packageName = sbn.packageName
        val fullText = buildNotificationText(sbn.notification)
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        if (!PaymentNotificationParser.shouldInspect(packageName, fullText)) return

        val uniqueKey = "${packageName}_${sbn.postTime}_${sbn.id}"
        if (recentNotifications.contains(uniqueKey)) return
        recentNotifications.add(uniqueKey)
        if (recentNotifications.size > 50) recentNotifications.clear()

        val parsed = PaymentNotificationParser.parse(packageName, fullText)
        if (parsed == null) {
            logDebug(
                "No se reconocio un formato de pago compatible para $packageName. " +
                    "title='$title' text='$text' bigText='$bigText' fullText='$fullText'"
            )
            return
        }

        logDebug("Pago detectado desde $packageName")
        SessionManager.addPayment(this, parsed.amount, parsed.sender)
        PagoGlanceWidget.updateAll(this)

        val appName = resolvePaymentAppName(packageName, fullText)
        val naturalAmount = convertAmountToNatural(parsed.amount)
        val message = buildSpeechMessage(
            appName = appName,
            sender = parsed.sender,
            naturalAmount = naturalAmount
        )

        if (ttsReady) {
            applySpeechSettings()
            speakWithRepeat(message)
        } else {
            enqueuePendingAnnouncement(message)
            logDebug("Anuncio en cola mientras TTS inicializa")
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
            synchronized(speechLock) {
                pendingAnnouncements.clear()
                isSpeakingAnnouncement = false
            }
            abandonSpeechAudioFocus()
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun ensureTtsInitialized() {
        if (::tts.isInitialized) return
        tts = TextToSpeech(applicationContext, this)
    }

    private fun applySpeechSettings() {
        if (!::tts.isInitialized) return

        val speechRate = if (SessionManager.isPremium(this)) {
            SessionManager.getTtsSpeechRate(this)
        } else {
            0.9f
        }
        val speechPitch = if (SessionManager.isPremium(this)) {
            SessionManager.getTtsSpeechPitch(this)
        } else {
            0.88f
        }
        tts.setSpeechRate(speechRate)
        tts.setPitch(speechPitch)

        val selectedVoiceName = if (SessionManager.isPremium(this)) {
            SessionManager.getTtsVoiceName(this)
        } else {
            null
        }
        val selectedVoice = tts.voices?.firstOrNull { it.name == selectedVoiceName && !it.isNetworkConnectionRequired }
        if (selectedVoice != null) {
            tts.voice = selectedVoice
            return
        }

        val bestSpanishVoice = findPreferredSpanishMaleVoice(tts.voices.orEmpty())
            ?: tts.voices
                ?.filter { !it.isNetworkConnectionRequired }
                ?.filter { voice -> voice.locale?.language.equals("es", ignoreCase = true) }
                ?.sortedWith(
                    compareByDescending<Voice> { it.quality }
                        .thenBy { it.latency }
                )
                ?.firstOrNull()

        if (bestSpanishVoice != null) {
            tts.voice = bestSpanishVoice
            return
        }

        val preferredLocales = listOf(
            Locale.forLanguageTag("es-PE"),
            Locale.forLanguageTag("es-419"),
            Locale.forLanguageTag("es-US"),
            Locale("es", "MX"),
            Locale("es", "ES"),
            Locale("es")
        )

        val localeApplied = preferredLocales.any { locale ->
            val result = tts.setLanguage(locale)
            result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }

        if (localeApplied) return

        val fallbackVoice = tts.voices
            ?.filter { !it.isNetworkConnectionRequired }
            ?.firstOrNull { voice ->
                voice.locale?.language.equals("es", ignoreCase = true)
            }

        if (fallbackVoice != null) {
            tts.voice = fallbackVoice
            return
        }

        logDebug("No se encontró una voz en español soportada; usando configuración por defecto del motor")
    }

    private fun requestSpeechAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { }
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun abandonSpeechAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let(audioManager::abandonAudioFocusRequest)
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun resolvePaymentAppName(packageName: String, fullText: String): String {
        val normalized = fullText.lowercase(Locale.ROOT)
        val mentionsPlin = normalized.contains("plin") || normalized.contains("plineo") || normalized.contains("plineó")
        return if (mentionsPlin) {
            "Plin"
        } else if (packageName == PaymentNotificationParser.YAPE_PACKAGE) {
            "Yape"
        } else {
            "Plin"
        }
    }

    private fun buildSpeechMessage(
        appName: String,
        sender: String,
        naturalAmount: String
    ): String {
        val spokenSender = humanizeSenderForSpeech(sender)

        if (SessionManager.isPremium(this) && SessionManager.isTtsAmountOnly(this)) {
            return "$appName!, $naturalAmount."
        }

        val verb = when (appName.lowercase(Locale.ROOT)) {
            "yape" -> "te ha yapeado"
            "plin" -> "te ha plineado"
            else -> "te ha pagado"
        }

        return if (spokenSender.isBlank() || spokenSender.equals("Cliente", ignoreCase = true)) {
            "$appName!, te han pagado $naturalAmount."
        } else {
            "$appName!, $spokenSender $verb $naturalAmount."
        }
    }

    private fun speakWithRepeat(message: String) {
        val repeatCount = if (SessionManager.isPremium(this)) {
            SessionManager.getTtsRepeatCount(this)
        } else {
            1
        }
        val repeatedMessage = List(repeatCount) { message }.joinToString(separator = " ... ")

        synchronized(speechLock) {
            if (!isSpeakingAnnouncement) {
                speakAnnouncementLocked(repeatedMessage)
                return
            }

            // Cola total de 3 anuncios: 1 sonando + 2 pendientes.
            if (pendingAnnouncements.size >= 2) {
                pendingAnnouncements.removeFirst()
            }
            pendingAnnouncements.addLast(repeatedMessage)
        }
    }

    private fun enqueuePendingAnnouncement(message: String) {
        synchronized(speechLock) {
            if (pendingAnnouncementsBeforeTts.size >= 3) {
                pendingAnnouncementsBeforeTts.removeFirst()
            }
            pendingAnnouncementsBeforeTts.addLast(message)
        }
    }

    private fun flushPendingAnnouncementsAfterTtsReady() {
        val queuedMessages = synchronized(speechLock) {
            pendingAnnouncementsBeforeTts.toList().also { pendingAnnouncementsBeforeTts.clear() }
        }
        queuedMessages.forEach { message ->
            speakWithRepeat(message)
        }
    }

    private fun handleAnnouncementCompleted() {
        synchronized(speechLock) {
            if (pendingAnnouncements.isNotEmpty()) {
                speakAnnouncementLocked(pendingAnnouncements.removeFirst())
            } else {
                isSpeakingAnnouncement = false
                abandonSpeechAudioFocus()
            }
        }
    }

    private fun speakAnnouncementLocked(message: String) {
        utteranceCounter += 1
        isSpeakingAnnouncement = true
        requestSpeechAudioFocus()
        val speakParams = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val result = tts.speak(message, TextToSpeech.QUEUE_FLUSH, speakParams, "pago_id_$utteranceCounter")
        if (result == TextToSpeech.ERROR) {
            logDebug("TTS devolvió ERROR al intentar anunciar el pago")
            isSpeakingAnnouncement = false
            abandonSpeechAudioFocus()
        }
    }

    private fun buildNotificationText(notification: Notification): String {
        val extras = notification.extras
        val parts = mutableListOf<String>()

        fun addPart(value: CharSequence?) {
            val text = value?.toString()?.trim().orEmpty()
            if (text.isNotBlank()) {
                parts.add(text)
            }
        }

        addPart(notification.tickerText)
        addPart(extras.getCharSequence(Notification.EXTRA_TITLE))
        addPart(extras.getCharSequence(Notification.EXTRA_TITLE_BIG))
        addPart(extras.getCharSequence(Notification.EXTRA_TEXT))
        addPart(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        addPart(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        addPart(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))
        addPart(extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE))

        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.forEach(::addPart)

        return parts
            .distinct()
            .joinToString(separator = " ")
            .replace("\n", " ")
            .trim()
    }

    private fun humanizeSenderForSpeech(sender: String): String {
        val cleaned = sender
            .replace(Regex("\\b\\d{6,}\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        val words = cleaned
            .split(" ")
            .map { token -> token.trim(' ', '.', ',', ';', ':', '-', '_', '(', ')') }
            .filter { token -> token.isNotBlank() && token.any(Char::isLetter) }
            .take(2)

        if (words.isEmpty()) return cleaned

        return words.joinToString(" ") { word ->
            when (word.lowercase()) {
                "de", "del", "la", "las", "los", "y" -> word.lowercase()
                else -> word.lowercase().replaceFirstChar { it.titlecase() }
            }
        }
    }

    private fun findPreferredSpanishMaleVoice(voices: Set<Voice>): Voice? {
        val maleKeywords = listOf("male", "masc", "man", "hombre", "jorge", "carlos", "diego", "raul")

        return voices
            .filter { !it.isNetworkConnectionRequired }
            .filter { voice -> voice.locale?.language.equals("es", ignoreCase = true) }
            .sortedWith(
                compareByDescending<Voice> { voice ->
                    val descriptor = "${voice.name} ${voice.locale?.displayName.orEmpty()}".lowercase(Locale.ROOT)
                    maleKeywords.any(descriptor::contains)
                }.thenByDescending { it.quality }
                    .thenBy { it.latency }
            )
            .firstOrNull()
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
