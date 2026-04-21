package com.example.pagovoz

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class PagoNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {
    companion object {
        private const val MAX_PENDING_ANNOUNCEMENTS = 100
        private const val FOREGROUND_NOTIFICATION_ID = 9001
        private const val FOREGROUND_CHANNEL_ID = "hablapago_listener_fg"
        private const val WATCHDOG_TIMEOUT_MS = 25_000L
    }

    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private val tag = "HablaPagoListener"
    private val notificationGate = PaymentNotificationGate()
    private val speechLock = Any()
    private val pendingAnnouncements = ArrayDeque<String>()
    private val pendingAnnouncementsBeforeTts = ArrayDeque<String>()
    private var isSpeakingAnnouncement = false
    private var utteranceCounter = 0L
    private var audioFocusRequest: AudioFocusRequest? = null
    private var speechWakeLock: PowerManager.WakeLock? = null
    private val watchdogHandler = Handler(Looper.getMainLooper())
    private val announcementWatchdog = Runnable {
        synchronized(speechLock) {
            if (isSpeakingAnnouncement) {
                logDebug("Watchdog: anuncio atascado por ${WATCHDOG_TIMEOUT_MS}ms, reseteando estado")
                isSpeakingAnnouncement = false
                releaseSpeechWakeLock()
                if (pendingAnnouncements.isNotEmpty()) {
                    speakAnnouncementLocked(pendingAnnouncements.removeFirst())
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ListenerDiagnostics.markListenerCreated(this)
        logDebug("Servicio creado")
        startListenerForeground()
        ensureTtsInitialized()
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        logDebug("onStartCommand: asegurando foreground")
        startListenerForeground()
        return START_STICKY
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        ListenerDiagnostics.markListenerConnected(this)
        logDebug("Notification listener conectado")
        ensureTtsInitialized()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        ListenerDiagnostics.markListenerDisconnected(this, "listener_callback")
        ListenerDiagnostics.markRebindAttempt(this, forceToggle = false, reason = "listener_disconnected")
        logDebug("Notification listener desconectado; solicitando rebind")
        ttsReady = false
        NotificationListenerHelper.requestRebind(this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            ListenerDiagnostics.markTtsReady(this)
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
        } else {
            ListenerDiagnostics.markTtsError(this, "init_status_$status")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!SessionManager.isActive(this)) return

        val packageName = sbn.packageName
        ListenerDiagnostics.markNotificationReceived(this, packageName)
        val fullText = buildNotificationText(sbn.notification)
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val notificationKey = buildNotificationKey(sbn)
        val gateResult = notificationGate.evaluate(
            packageName = packageName,
            fullText = fullText,
            notificationKey = notificationKey
        )

        val parsed = when (gateResult) {
            is PaymentNotificationGateResult.Accepted -> gateResult.payment
            is PaymentNotificationGateResult.Ignored -> {
                if (gateResult.reason != "duplicate_processed") {
                    ListenerDiagnostics.markPaymentIgnored(this, packageName, gateResult.reason)
                }
                if (gateResult.reason == "parse_null") {
                    logDebug(
                        "No se reconocio un formato de pago compatible para $packageName. " +
                            "title='$title' text='$text' bigText='$bigText' fullText='$fullText'"
                    )
                }
                return
            }
        }
        logDebug("Pago detectado desde $packageName")
        if (SessionManager.addPayment(this, parsed.amount, parsed.sender)) {
            ListenerDiagnostics.markPaymentCaptured(this, packageName, parsed.amount, parsed.sender)
            acquireSpeechWakeLock() // mantener CPU activo para que TTS pueda hablar en Doze mode

            val appName = PaymentSourceResolver.resolveAppName(packageName, fullText)
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
        } else {
            logDebug("Pago duplicado ignorado (ya procesado por otra vía)")
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
            cents == 1 -> "con un centimo"
            else -> "con ${numberToWords(cents)} centimos"
        }

        return "$integerText $centsText".trim()
    }

    private fun numberToWords(n: Int): String {
        val units = arrayOf("", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve")
        val specials = arrayOf("diez", "once", "doce", "trece", "catorce", "quince", "dieciseis", "diecisiete", "dieciocho", "diecinueve")
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
        ListenerDiagnostics.markListenerDisconnected(this, "service_destroyed")
        stopForeground(STOP_FOREGROUND_REMOVE)
        watchdogHandler.removeCallbacks(announcementWatchdog)
        releaseSpeechWakeLock()
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
        // If TTS is ready, nothing to do.
        if (::tts.isInitialized && ttsReady) return

        if (::tts.isInitialized) {
            // TTS object exists but ttsReady=false. This happens when Android briefly
            // disconnects and reconnects the listener — onListenerDisconnected() sets
            // ttsReady=false but onListenerConnected() skips re-init.
            // Restart TTS to trigger onInit() and flush any pending announcements.
            restartTtsEngine("reconnect_not_ready")
            return
        }

        tts = TextToSpeech(applicationContext, this)
    }

    private fun restartTtsEngine(reason: String) {
        ttsReady = false
        if (::tts.isInitialized) {
            runCatching {
                tts.stop()
                tts.shutdown()
            }.onFailure {
                logDebug("No se pudo reiniciar TTS limpiamente tras $reason: ${it.message}")
            }
        }
        logDebug("Reiniciando TTS tras $reason")
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

        logDebug("No se encontro una voz en espanol soportada; usando configuracion por defecto del motor")
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

            if (pendingAnnouncements.size >= MAX_PENDING_ANNOUNCEMENTS) {
                pendingAnnouncements.removeFirst()
            }
            pendingAnnouncements.addLast(repeatedMessage)
        }
    }

    private fun enqueuePendingAnnouncement(message: String) {
        synchronized(speechLock) {
            if (pendingAnnouncementsBeforeTts.size >= MAX_PENDING_ANNOUNCEMENTS) {
                pendingAnnouncementsBeforeTts.removeFirst()
            }
            pendingAnnouncementsBeforeTts.addLast(message)
        }
    }

    private fun flushPendingAnnouncementsAfterTtsReady() {
        val queuedMessages = synchronized(speechLock) {
            pendingAnnouncementsBeforeTts.toList().also { pendingAnnouncementsBeforeTts.clear() }
        }
        if (queuedMessages.isNotEmpty()) {
            acquireSpeechWakeLock() // pagos acumulados mientras TTS iniciaba
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
                watchdogHandler.removeCallbacks(announcementWatchdog)
                releaseSpeechWakeLock()
            }
        }
    }

    private fun speakAnnouncementLocked(message: String) {
        utteranceCounter += 1
        isSpeakingAnnouncement = true
        watchdogHandler.removeCallbacks(announcementWatchdog)
        watchdogHandler.postDelayed(announcementWatchdog, WATCHDOG_TIMEOUT_MS)
        requestSpeechAudioFocus()
        val speakParams = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        }
        val result = tts.speak(message, TextToSpeech.QUEUE_FLUSH, speakParams, "pago_id_$utteranceCounter")
        if (result == TextToSpeech.ERROR) {
            ListenerDiagnostics.markTtsError(this, "speak_error")
            logDebug("TTS devolvio ERROR al intentar anunciar el pago")
            val retryMessages = buildList {
                add(message)
                addAll(pendingAnnouncements)
            }
            pendingAnnouncements.clear()
            isSpeakingAnnouncement = false
            abandonSpeechAudioFocus()
            retryMessages.forEach(::enqueuePendingAnnouncement)
            restartTtsEngine("speak_error")
        }
    }

    private fun buildNotificationKey(sbn: StatusBarNotification): String {
        val baseKey = sbn.key.ifBlank { "${sbn.packageName}:${sbn.id}:${sbn.tag.orEmpty()}" }
        return "$baseKey:${sbn.postTime}"
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

    private fun startListenerForeground() {
        val channel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "HablaPago activo",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "HablaPago está escuchando pagos en segundo plano"
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = Notification.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_activation_mic)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Escuchando pagos...")
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun acquireSpeechWakeLock() {
        val wl = speechWakeLock
        if (wl != null && wl.isHeld) return
        speechWakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HablaPago::PaymentAnnouncement")
            .apply {
                setReferenceCounted(false)
                acquire(30_000L) // techo de seguridad: se libera solo tras 30s
            }
    }

    private fun releaseSpeechWakeLock() {
        speechWakeLock?.let { if (it.isHeld) it.release() }
        speechWakeLock = null
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
}
