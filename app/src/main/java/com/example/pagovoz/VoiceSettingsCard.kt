package com.example.pagovoz

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.YapePurple



private data class VoiceOption(
    val name: String,
    val title: String,
    val subtitle: String,
    val speakingStyle: String,
    val accentStart: Color,
    val accentEnd: Color,
    val avatarUrl: String
)

private data class VoicePersona(
    val displayName: String,
    val speakingStyle: String,
    val accentStart: Color,
    val accentEnd: Color,
    val isFemale: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowReports: () -> Unit,
    onShowPremium: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
    ) {
        // Fondo con resplandores dinámicos sutiles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 2000f
                    )
                )
        )
        
        Scaffold(
            topBar = {
                AppSectionTopBar(
                    title = "Configuración Pro",
                    onBack = onBack
                )
            },
            containerColor = Color.Transparent,
            bottomBar = {
                DashboardBottomBar(selectedTab = DashboardTab.Premium) { tab ->
                    when (tab) {
                        DashboardTab.Home -> onBack()
                        DashboardTab.History -> onShowHistory()
                        DashboardTab.Payments -> onShowPayments()
                        DashboardTab.Reports -> onShowReports()
                        DashboardTab.Premium -> onShowPremium()
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp) // Altura fija para que la sección ocupe espacio pero esté oculta
                ) {
                    // Capa de Bloqueo Total - Estilo "En Construcción" Premium
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF090B10), // Sólido para ocultar el diseño
                        shape = RoundedCornerShape(32.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            // Patrón de fondo sutil o decorativo (opcional, pero ayuda a la estética)
                            
                            Column(
                                modifier = Modifier.rotate(-15f), // El mensaje en diagonal
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "PRÓXIMAMENTE",
                                    color = Color(0xFFFFD700), // Dorado
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp,
                                    letterSpacing = 4.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFD700),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ESTAMOS EN CONSTRUCCIÓN",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProVoiceSettingsCard() {
    val context = LocalContext.current
    var voiceOptions by remember { mutableStateOf<List<VoiceOption>>(emptyList()) }
    var selectedVoiceName by remember { mutableStateOf(SessionManager.getTtsVoiceName(context)) }
    var speechRate by remember { mutableStateOf(SessionManager.getTtsSpeechRate(context)) }
    var speechPitch by remember { mutableStateOf(SessionManager.getTtsSpeechPitch(context)) }
    var amountOnly by remember { mutableStateOf(SessionManager.isTtsAmountOnly(context)) }
    var isLoading by remember { mutableStateOf(true) }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }



    DisposableEffect(context) {
        val engineHolder = arrayOfNulls<TextToSpeech>(1)
        val tts = TextToSpeech(context) { status ->
            val currentEngine = engineHolder[0] ?: return@TextToSpeech
            if (status != TextToSpeech.SUCCESS) {
                isLoading = false
                return@TextToSpeech
            }

            val availableVoices = buildVoiceOptions(currentEngine.voices.orEmpty())
            voiceOptions = availableVoices
            if (selectedVoiceName == null || availableVoices.none { it.name == selectedVoiceName }) {
                selectedVoiceName = availableVoices.firstOrNull()?.name
                SessionManager.setTtsVoiceName(context, selectedVoiceName)
            }

            applyVoiceConfig(
                engine = currentEngine,
                voiceName = selectedVoiceName,
                speechRate = speechRate,
                speechPitch = speechPitch
            )
            isLoading = false
        }
        engineHolder[0] = tts
        engine = tts

        onDispose {
            engine = null
            tts.stop()
            tts.shutdown()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = YapePurple)
        }
        return
    }
    val currentVoice = voiceOptions.firstOrNull { it.name == selectedVoiceName }

    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        
        // --- SECCIÓN 1: EL PROTAGONISTA (VOZ SELECCIONADA) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            ActiveVoiceImmersiveCard(
                option = currentVoice,
                onClickTest = {
                    engine?.let {
                        applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch)
                        it.speak(
                            "¡Hola! Soy tu asistente de voz. Estoy listo para anunciar tus pagos.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "voice_test"
                        )
                    }
                }
            )
        }

        // --- SECCIÓN 2: SELECTOR DE MODELOS ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modelos Disponibles",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "${voiceOptions.size} tipos",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(voiceOptions) { option ->
                    ModernVoiceThumbnail(
                        option = option,
                        selected = selectedVoiceName == option.name,
                        onClick = { 
                            selectedVoiceName = option.name
                            SessionManager.setTtsVoiceName(context, option.name)
                            engine?.let { applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch) }
                        }
                    )
                }
            }
        }

        // --- SECCIÓN 3: CONTROL DE PRECISIÓN ---
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White.copy(alpha = 0.03f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text(
                    text = "Personalizar Lectura",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                VoiceControlSlider(
                    label = "Velocidad",
                    value = speechRate,
                    onValueChange = { 
                        speechRate = it
                        SessionManager.setTtsSpeechRate(context, it)
                        engine?.let { e -> applyVoiceConfig(e, selectedVoiceName, speechRate, speechPitch) }
                    },
                    valueRange = 0.5f..1.5f,
                    valueLabel = String.format(Locale.US, "%.1fx", speechRate),
                    icon = Icons.Default.Settings
                )
                
                VoiceControlSlider(
                    label = "Tono (Pitch)",
                    value = speechPitch,
                    onValueChange = { 
                        speechPitch = it
                        SessionManager.setTtsSpeechPitch(context, it)
                        engine?.let { e -> applyVoiceConfig(e, selectedVoiceName, speechRate, speechPitch) }
                    },
                    valueRange = 0.5f..1.15f,
                    valueLabel = pitchLabel(speechPitch),
                    icon = Icons.Default.Info
                )
            }
        }

        // --- SECCIÓN 4: SIMULADOR INTERACTIVO ---
        Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Simulador de Pagos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernSimulationCard(
                    modifier = Modifier.weight(1f),
                    appName = "Yape",
                    accent = Color(0xFFB46BF6),
                    icon = R.drawable.ic_nav_payments,
                    onClick = {
                        engine?.let {
                            applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch)
                            it.speak(
                                buildPreviewMessage("Yape", "Micaela", "doce soles con cincuenta céntimos", amountOnly),
                                TextToSpeech.QUEUE_FLUSH, null, "v_y"
                            )
                        }
                    }
                )
                ModernSimulationCard(
                    modifier = Modifier.weight(1f),
                    appName = "Plin",
                    accent = Color(0xFF34B7D7),
                    icon = R.drawable.ic_nav_payments,
                    onClick = {
                        engine?.let {
                            applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch)
                            it.speak(
                                buildPreviewMessage("Plin", "Roberto", "cinco soles con veinte céntimos", amountOnly),
                                TextToSpeech.QUEUE_FLUSH, null, "v_p"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveVoiceImmersiveCard(
    option: VoiceOption?,
    onClickTest: () -> Unit
) {
    if (option == null) return
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Avatar Gigante con Efecto de Pulsación/Halo
        Box(contentAlignment = Alignment.Center) {
            // Halo de fondo animado/difuso
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(option.accentEnd.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            
            VoiceAvatar(
                name = option.title,
                isFemale = option.avatarUrl == "female",
                accentStart = option.accentStart,
                accentEnd = option.accentEnd,
                modifier = Modifier.size(120.dp)
            )
        }

        // Información de la Voz
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = option.title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = (-1).sp
            )
            Text(
                text = option.speakingStyle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Botón de Prueba Flotante
        Surface(
            shape = CircleShape,
            color = option.accentEnd,
            modifier = Modifier
                .height(56.dp)
                .width(180.dp)
                .hablaPagoPressable(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClickTest),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Escuchar Voz", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModernVoiceThumbnail(
    option: VoiceOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) option.accentEnd.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) option.accentEnd else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .width(110.dp)
            .height(130.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            VoiceAvatar(
                name = option.title,
                isFemale = option.avatarUrl == "female",
                accentStart = option.accentStart,
                accentEnd = option.accentEnd,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = option.title,
                color = Color.White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = option.subtitle,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ModernSimulationCard(
    modifier: Modifier = Modifier,
    appName: String,
    accent: Color,
    icon: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.15f)),
        modifier = modifier
            .height(100.dp)
            .hablaPagoPressable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = appName,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun VoiceControlSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    icon: ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = valueLabel,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
        
        Box(contentAlignment = Alignment.Center) {
            // Track background con glow sutil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun VoiceAvatar(
    name: String,
    isFemale: Boolean,
    accentStart: Color,
    accentEnd: Color,
    modifier: Modifier = Modifier
) {
    val brush = Brush.linearGradient(listOf(accentStart, accentEnd))
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.04f))
            .border(2.dp, brush, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Fondo con gradiente sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.15f)
                .background(brush)
        )
        
        // Icono de Avatar estilizado usando los recursos creados
        Icon(
            painter = painterResource(id = if (isFemale) R.drawable.ic_voice_female else R.drawable.ic_voice_male),
            contentDescription = name,
            tint = Color.White,
            modifier = Modifier.fillMaxSize(0.6f)
        )
    }
}

private fun voicePersonaFor(localeLabel: String, voiceName: String, index: Int): VoicePersona {
    val descriptor = "$voiceName $localeLabel".lowercase(Locale.ROOT)
    val isFemale = listOf("female", "mujer", "femenina", " f", "clara", "lucia", "1-", "3-").any { descriptor.contains(it) } 
        || (!listOf("male", "hombre", "masculino", " m", "diego", "sergio", "2-", "4-").any { descriptor.contains(it) } && index % 2 == 0)

    val names = if (isFemale) {
        listOf("Valeria", "Clara", "Lucía", "Elena", "Camila", "Sofía", "Martina", "Valentina")
    } else {
        listOf("Sergio", "Diego", "Mateo", "Bruno", "Álvaro", "Lucas", "Martín", "Hugo")
    }

    val style = if (isFemale) "Voz cálida, ágil y muy amable." else "Suena directo, confiable y con energía."
    val colorStart = if (isFemale) Color(0xFFC43E94) else Color(0xFF11627A)
    val colorEnd = if (isFemale) Color(0xFFF07DA0) else Color(0xFF28B7BA)

    return VoicePersona(names[(index - 1).mod(names.size)], style, colorStart, colorEnd, isFemale)
}

private fun buildVoiceOptions(voices: Set<Voice>): List<VoiceOption> {
    val spanishVoices = voices.filter { it.locale?.language.equals("es", ignoreCase = true) }
    val candidates = if (spanishVoices.isNotEmpty()) spanishVoices else voices.toList()
    val countersByLocale = linkedMapOf<String, Int>()

    return candidates
        .filter { !it.isNetworkConnectionRequired }
        .sortedWith(compareBy({ it.locale?.displayName ?: "" }, { it.name }))
        .map { voice ->
            val localeKey = voice.locale?.toLanguageTag().orEmpty()
            val voiceIndex = (countersByLocale[localeKey] ?: 0) + 1
            countersByLocale[localeKey] = voiceIndex
            val localeLabel = friendlyLocaleLabel(voice.locale)
            val persona = voicePersonaFor(localeLabel, voice.name, voiceIndex)

            VoiceOption(
                name = voice.name,
                title = persona.displayName,
                subtitle = localeLabel,
                speakingStyle = persona.speakingStyle,
                accentStart = persona.accentStart,
                accentEnd = persona.accentEnd,
                avatarUrl = if (persona.isFemale) "female" else "male"
            )
        }
}
private fun buildPreviewMessage(
    appName: String,
    sender: String,
    naturalAmount: String,
    amountOnly: Boolean
): String {
    if (amountOnly) {
        return "Pago de $naturalAmount por $appName."
    }
    return "$sender te ha enviado un pago de $naturalAmount por $appName."
}

private fun friendlyLocaleLabel(locale: Locale?): String {
    if (locale == null) return "Espanol Latinoamericano"

    val country = locale.getDisplayCountry(Locale("es", "PE")).ifBlank {
        locale.getDisplayLanguage(Locale("es", "PE"))
    }

    return when {
        country.contains("Per", ignoreCase = true) -> "Espanol Latinoamericano"
        country.contains("Mex", ignoreCase = true) -> "Espanol Latinoamericano"
        country.contains("Espa", ignoreCase = true) -> "Espanol Peninsular"
        country.isNotBlank() -> country
        else -> "Espanol Latinoamericano"
    }
}

private fun applyVoiceConfig(
    engine: TextToSpeech,
    voiceName: String?,
    speechRate: Float,
    speechPitch: Float
) {
    engine.setSpeechRate(speechRate)
    engine.setPitch(speechPitch)

    val selectedVoice = engine.voices?.firstOrNull { it.name == voiceName }
    if (selectedVoice != null) {
        engine.voice = selectedVoice
    } else {
        val fallbackVoice = engine.voices
            ?.filter { !it.isNetworkConnectionRequired }
            ?.filter { it.locale?.language.equals("es", ignoreCase = true) }
            ?.sortedWith(
                compareByDescending<Voice> { voice ->
                    val descriptor = "${voice.name} ${voice.locale?.displayName.orEmpty()}".lowercase(Locale.ROOT)
                    listOf("male", "masc", "man", "hombre", "jorge", "carlos", "diego", "raul")
                        .any(descriptor::contains)
                }.thenByDescending { it.quality }
                    .thenBy { it.latency }
            )
            ?.firstOrNull()

        if (fallbackVoice != null) {
            engine.voice = fallbackVoice
        } else {
            engine.language = Locale.forLanguageTag("es-PE")
        }
    }
}

private fun pitchLabel(pitch: Float): String {
    return when {
        pitch < 0.9f -> "Grave"
        pitch > 1.15f -> "Agudo"
        else -> "Normal"
    }
}


