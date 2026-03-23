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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.YapePurple
import java.util.Locale

private data class VoiceOption(
    val name: String,
    val title: String,
    val subtitle: String,
    val speakingStyle: String,
    val accentStart: Color,
    val accentEnd: Color
)

private data class VoicePersona(
    val displayName: String,
    val speakingStyle: String,
    val accentStart: Color,
    val accentEnd: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsScreen(
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowReports: () -> Unit,
    onShowProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.voice_screen_title),
                onBack = onBack,
                badgeText = stringResource(R.string.voice_pro_badge)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Profile) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> onShowReports()
                    DashboardTab.Profile -> onShowProfile()
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ProVoiceSettingsCard()
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
    var repeatCount by remember { mutableStateOf(SessionManager.getTtsRepeatCount(context)) }
    var isLoading by remember { mutableStateOf(true) }
    var showVoicePicker by remember { mutableStateOf(false) }
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
    val currentVoiceTitle = currentVoice?.title ?: stringResource(R.string.voice_pro_default_voice)
    val currentVoiceSubtitle = currentVoice?.subtitle ?: stringResource(R.string.voice_pro_default_locale)
    val currentVoiceStyle = currentVoice?.speakingStyle ?: stringResource(R.string.voice_pro_default_style)
    val currentVoiceAccentStart = currentVoice?.accentStart ?: Color(0xFF7F3698)
    val currentVoiceAccentEnd = currentVoice?.accentEnd ?: YapePurple
    val playbackModeLabel = if (amountOnly) {
        stringResource(R.string.voice_pro_amount_only_title)
    } else {
        stringResource(R.string.voice_pro_mode_off)
    }
    val repeatLabel = if (repeatCount == 1) {
        context.getString(R.string.voice_pro_repeat_times, repeatCount)
    } else {
        context.getString(R.string.voice_pro_repeat_times_plural, repeatCount)
    }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        ActiveVoiceCard(
            voiceTitle = currentVoiceTitle,
            voiceSubtitle = currentVoiceSubtitle,
            voiceStyle = currentVoiceStyle,
            playbackModeLabel = playbackModeLabel,
            repeatLabel = repeatLabel,
            accentStart = currentVoiceAccentStart,
            accentEnd = currentVoiceAccentEnd,
            onChangeVoice = { showVoicePicker = true }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE8E0F0)),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.section, vertical = AppSpacing.section),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VoiceSettingsSectionHeader(
                    title = stringResource(R.string.voice_audio_controls_title),
                    subtitle = stringResource(R.string.voice_audio_controls_subtitle)
                )

                AudioSliderRow(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.voice_pro_speed),
                    subtitle = stringResource(R.string.voice_pro_speed_subtitle),
                    valueLabel = "%.1fx".format(Locale.US, speechRate),
                    value = speechRate,
                    range = 0.6f..1.8f,
                    startLabel = stringResource(R.string.voice_pro_speed_start),
                    endLabel = stringResource(R.string.voice_pro_speed_end),
                    onValueChange = {
                        speechRate = it
                        SessionManager.setTtsSpeechRate(context, it)
                    }
                )

                VoicePanelDivider(modifier = Modifier.padding(vertical = 2.dp))

                AudioSliderRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.voice_pro_tone_pitch),
                    subtitle = stringResource(R.string.voice_pro_tone_subtitle),
                    valueLabel = pitchLabel(speechPitch),
                    value = speechPitch,
                    range = 0.7f..1.6f,
                    startLabel = stringResource(R.string.voice_pro_tone_start),
                    endLabel = stringResource(R.string.voice_pro_tone_end),
                    onValueChange = {
                        speechPitch = it
                        SessionManager.setTtsSpeechPitch(context, it)
                    }
                )

                VoicePanelDivider(modifier = Modifier.padding(vertical = 2.dp))

                VoiceSettingsSectionHeader(
                    title = stringResource(R.string.voice_playback_options_title),
                    subtitle = stringResource(R.string.voice_playback_options_subtitle)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF9F7FC)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF4ECFE)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = YapePurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.voice_pro_amount_only_title),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF353D4C)
                            )
                            Text(
                                text = stringResource(R.string.voice_pro_amount_only_subtitle),
                                color = Color(0xFF9DA6B3),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (amountOnly) Color(0xFFEAF8EE) else Color(0xFFF1ECF7)
                            ) {
                                Text(
                                    text = if (amountOnly) stringResource(R.string.voice_pro_mode_on) else stringResource(R.string.voice_pro_mode_off),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = if (amountOnly) Color(0xFF1FA866) else Color(0xFF7C8594),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Switch(
                                checked = amountOnly,
                                onCheckedChange = {
                                    amountOnly = it
                                    SessionManager.setTtsAmountOnly(context, it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = YapePurple
                                )
                            )
                        }
                    }
                }

                VoicePanelDivider(modifier = Modifier.padding(vertical = 16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF9F7FC)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                        Text(
                            text = stringResource(R.string.voice_pro_repeat_title),
                            color = Color(0xFF3B4251),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.voice_pro_repeat_subtitle),
                            color = Color(0xFF8E97A5),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            (1..3).forEach { option ->
                                RepeatOptionChip(
                                    modifier = Modifier.weight(1f),
                                    text = if (option == 1) {
                                        context.getString(R.string.voice_pro_repeat_times, option)
                                    } else {
                                        context.getString(R.string.voice_pro_repeat_times_plural, option)
                                    },
                                    selected = repeatCount == option,
                                    onClick = {
                                        repeatCount = option
                                        SessionManager.setTtsRepeatCount(context, option)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE8E0F0)),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.section, vertical = AppSpacing.section),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VoiceSettingsSectionHeader(
                        title = stringResource(R.string.voice_preview_title),
                        subtitle = stringResource(R.string.voice_preview_subtitle),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        onClick = {
                            SessionManager.resetTtsSettings(context)
                            selectedVoiceName = SessionManager.getTtsVoiceName(context)
                            speechRate = SessionManager.getTtsSpeechRate(context)
                            speechPitch = SessionManager.getTtsSpeechPitch(context)
                            amountOnly = SessionManager.isTtsAmountOnly(context)
                            repeatCount = SessionManager.getTtsRepeatCount(context)
                            engine?.let { applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = YapePurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.voice_pro_restore_short),
                            color = YapePurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                PreviewVoiceCard(
                    title = stringResource(R.string.voice_preview_yape_title),
                    sampleLine = stringResource(R.string.voice_preview_yape_sample),
                    sampleHighlight = stringResource(R.string.voice_preview_yape_badge),
                    accent = Color(0xFFB46BF6),
                    background = Color(0xFFF4ECFE),
                    onClick = {
                        engine?.let {
                            applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch)
                            it.speak(
                                buildPreviewMessage(
                                    appName = "Yape",
                                    sender = "Maria",
                                    naturalAmount = "doce soles con cincuenta centimos",
                                    amountOnly = amountOnly
                                ),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "voice_preview_yape"
                            )
                        }
                    }
                )
                PreviewVoiceCard(
                    title = stringResource(R.string.voice_preview_plin_title),
                    sampleLine = stringResource(R.string.voice_preview_plin_sample),
                    sampleHighlight = stringResource(R.string.voice_preview_plin_badge),
                    accent = Color(0xFF34B7D7),
                    background = Color(0xFFEAFBFF),
                    onClick = {
                        engine?.let {
                            applyVoiceConfig(it, selectedVoiceName, speechRate, speechPitch)
                            it.speak(
                                buildPreviewMessage(
                                    appName = "Plin",
                                    sender = "Carlos",
                                    naturalAmount = "ocho soles",
                                    amountOnly = amountOnly
                                ),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "voice_preview_plin"
                            )
                        }
                    }
                )
            }
        }
    }

    if (showVoicePicker) {
        AlertDialog(
            onDismissRequest = { showVoicePicker = false },
            title = { Text(stringResource(R.string.voice_pro_pick_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    voiceOptions.forEach { option ->
                        VoicePersonaOptionCard(
                            option = option,
                            selected = selectedVoiceName == option.name,
                            onClick = {
                                selectedVoiceName = option.name
                                SessionManager.setTtsVoiceName(context, option.name)
                                engine?.let { applyVoiceConfig(it, option.name, speechRate, speechPitch) }
                                showVoicePicker = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoicePicker = false }) {
                    Text(stringResource(R.string.voice_pro_close))
                }
            }
        )
    }
}

@Composable
private fun ActiveVoiceCard(
    voiceTitle: String,
    voiceSubtitle: String,
    voiceStyle: String,
    playbackModeLabel: String,
    repeatLabel: String,
    accentStart: Color,
    accentEnd: Color,
    onChangeVoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentStart, accentEnd)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoiceAvatar(
                    name = voiceTitle,
                    accentStart = Color.White.copy(alpha = 0.22f),
                    accentEnd = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(58.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.voice_active_label),
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "$voiceTitle (${stringResource(R.string.voice_premium_suffix)})",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp / 1.2f
                    )
                    Text(
                        text = voiceSubtitle,
                        color = Color.White.copy(alpha = 0.78f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = voiceStyle,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceHeroPill(
                    text = playbackModeLabel,
                    modifier = Modifier.weight(1f)
                )
                VoiceHeroPill(
                    text = repeatLabel,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onChangeVoice,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = YapePurple
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = YapePurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.voice_pro_choose_voice),
                    fontWeight = FontWeight.Bold,
                    color = YapePurple
                )
            }
        }
    }
}

@Composable
private fun AudioSliderRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    startLabel: String,
    endLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HablaPagoIconTile(
                icon = icon,
                tint = YapePurple,
                containerColor = Color(0xFFF5EEFF),
                size = 36.dp,
                iconSize = AppIconSizes.md,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color(0xFF495160),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF3ECFB)
            ) {
                Text(
                    text = valueLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = YapePurple,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = Color(0xFF8E97A5),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = startLabel,
                color = Color(0xFF98A1AF),
                fontSize = 11.sp
            )
            Text(
                text = endLabel,
                color = Color(0xFF98A1AF),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun VoiceHeroPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RepeatOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) YapePurple else Color.White,
        border = BorderStroke(1.dp, if (selected) YapePurple else Color(0xFFE3DCEB))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) Color.White else Color(0xFF96A0AE),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PreviewVoiceCard(
    title: String,
    sampleLine: String,
    sampleHighlight: String,
    accent: Color,
    background: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .hablaPagoPressable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = background.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = sampleHighlight,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                HablaPagoIconTile(
                    icon = Icons.Default.PlayArrow,
                    tint = accent,
                    containerColor = Color.White.copy(alpha = 0.92f),
                    size = AppIconSizes.tileLg,
                    iconSize = 26.dp,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    color = Color(0xFF2F3746),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = sampleLine,
                    color = Color(0xFF6E7786),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.88f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .width(if (index == 1) 18.dp else 10.dp)
                                    .height(6.dp)
                                    .background(
                                        color = accent.copy(alpha = if (index == 1) 0.9f else 0.5f),
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.voice_preview_cta),
                        color = accent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VoicePersonaOptionCard(
    option: VoiceOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFF2E8FF) else Color.White,
        border = BorderStroke(
            1.dp,
            if (selected) option.accentEnd else Color(0xFFE6DFF0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VoiceAvatar(
                name = option.title,
                accentStart = option.accentStart,
                accentEnd = option.accentEnd,
                modifier = Modifier.size(58.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = option.title,
                    color = Color(0xFF2E3444),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = option.subtitle,
                    color = Color(0xFF7C8594),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Text(
                    text = option.speakingStyle,
                    color = Color(0xFF5E6675),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selected) option.accentEnd else Color(0xFFF4EFFA)
            ) {
                Text(
                    text = if (selected) stringResource(R.string.voice_option_selected) else stringResource(R.string.voice_option_choose),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    color = if (selected) Color.White else Color(0xFF6A7180),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun VoiceAvatar(
    name: String,
    accentStart: Color,
    accentEnd: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(accentStart, accentEnd)))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 9.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.88f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 7.dp)
                    .width(34.dp)
                    .height(20.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 10.dp,
                            bottomEnd = 10.dp
                        )
                    )
                    .background(Color.White.copy(alpha = 0.88f))
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 2.dp, bottom = 2.dp)
                    .size(22.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.take(1).uppercase(),
                        color = accentEnd,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceSettingsSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF232A36),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
        Text(
            text = subtitle,
            color = Color(0xFF8C95A4),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun VoicePanelDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFECE5F3))
    )
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
                accentEnd = persona.accentEnd
            )
        }
}

private fun voicePersonaFor(localeLabel: String, voiceName: String, index: Int): VoicePersona {
    val descriptor = "$voiceName $localeLabel".lowercase(Locale.ROOT)
    val pool = when {
        descriptor.contains("peninsular") || descriptor.contains("espa") -> listOf(
            VoicePersona("Sergio", "Habla firme, clara y con ritmo pausado.", Color(0xFF4958C7), Color(0xFF7D63E8)),
            VoicePersona("Clara", "Suena elegante, cercana y muy nítida.", Color(0xFFB14D8C), Color(0xFFE36CA8)),
            VoicePersona("Álvaro", "Tiene una presencia seria y segura.", Color(0xFF2F6F9E), Color(0xFF4DA6C8))
        )
        descriptor.contains("mex") -> listOf(
            VoicePersona("Valeria", "Habla cálida, ágil y muy amable.", Color(0xFFAF5C2C), Color(0xFFE89C49)),
            VoicePersona("Diego", "Suena directo, confiable y con energía.", Color(0xFF11627A), Color(0xFF28B7BA)),
            VoicePersona("Camila", "Tiene un tono dulce y muy claro.", Color(0xFF8240A8), Color(0xFFC86CE5))
        )
        else -> listOf(
            VoicePersona("Mateo", "Suena grave, clara y con buena presencia.", Color(0xFF5D37A6), Color(0xFF9B5DE5)),
            VoicePersona("Lucía", "Habla suave, cercana y muy natural.", Color(0xFFCB4F78), Color(0xFFF07DA0)),
            VoicePersona("Bruno", "Tiene un estilo firme y pausado.", Color(0xFF1A6E6D), Color(0xFF2EC4B6)),
            VoicePersona("Elena", "Suena amable, limpia y profesional.", Color(0xFF4C57C5), Color(0xFF8A7BFF))
        )
    }

    return pool[(index - 1).mod(pool.size)]
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


