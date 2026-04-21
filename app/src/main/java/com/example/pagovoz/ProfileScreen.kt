package com.example.pagovoz

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowReports: () -> Unit,
    onShowPremium: () -> Unit,
    onShowVoiceSettings: () -> Unit
) {
    val context = LocalContext.current
    val isPremium = SessionManager.isPremium(context)
    val trialDays = SessionManager.getPremiumDaysLeft(context)
    val selectedVoice = SessionManager.getTtsVoiceName(context)
        ?: context.getString(R.string.voice_pro_default_voice)
    val serviceActive = NotificationListenerHelper.isNotificationServiceEnabled(context)
    val deviceLabel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    var diagnosticsSnapshot by remember { mutableStateOf(ListenerDiagnostics.readSnapshot(context)) }

    LaunchedEffect(context) {
        while (true) {
            diagnosticsSnapshot = ListenerDiagnostics.readSnapshot(context)
            delay(1500)
        }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF090B10)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090B10),
                            Color(0xFF161224),
                            Color(0xFF0F1820),
                            Color(0xFF090B10)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            ProfileOverviewSection(
                isPremium = isPremium,
                trialDays = trialDays,
                serviceActive = serviceActive,
                currentVoice = selectedVoice
            )

            ProfileActionSection(
                isPremium = isPremium,
                selectedVoice = selectedVoice,
                onShowPremium = onShowPremium,
                onShowVoiceSettings = onShowVoiceSettings,
                onSupport = {
                    val phoneNumber = "51983450723"
                    val message = context.getString(R.string.premium_whatsapp_message)
                    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                }
            )

            ProfileDeviceSection(
                deviceLabel = deviceLabel,
                serviceActive = serviceActive
            )

            ProfileDiagnosticsSection(snapshot = diagnosticsSnapshot)
        }
    }
}

@Composable
private fun ProfileOverviewSection(
    isPremium: Boolean,
    trialDays: Int,
    serviceActive: Boolean,
    currentVoice: String
) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadii.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isPremium) {
                            stringResource(R.string.profile_badge_premium)
                        } else {
                            stringResource(R.string.profile_badge_basic)
                        }.uppercase(),
                        color = if (isPremium) AppColors.PlinCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.9.sp
                    )

                    Text(
                        text = stringResource(R.string.profile_merchant_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )

                    Text(
                        text = if (isPremium) {
                            if (trialDays > 0) {
                                stringResource(R.string.profile_status_trial, trialDays)
                            } else {
                                stringResource(R.string.profile_status_premium)
                            }
                        } else {
                            stringResource(R.string.profile_status_free)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, AppColors.PlinCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.profile_hero_supporting),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileOverviewLine(
                    label = stringResource(R.string.profile_metric_plan),
                    value = if (isPremium) {
                        stringResource(R.string.profile_metric_pro)
                    } else {
                        stringResource(R.string.profile_metric_basic)
                    }
                )
                ProfileInfoDivider()
                ProfileOverviewLine(
                    label = stringResource(R.string.profile_metric_service),
                    value = if (serviceActive) {
                        stringResource(R.string.profile_metric_active)
                    } else {
                        stringResource(R.string.profile_metric_inactive)
                    },
                    accent = if (serviceActive) AppColors.PlinCyan else MaterialTheme.colorScheme.error
                )
                ProfileInfoDivider()
                ProfileOverviewLine(
                    label = stringResource(R.string.profile_voice_label),
                    value = currentVoice,
                    allowWrap = true
                )
            }
        }
    }
}

@Composable
private fun ProfileOverviewLine(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.onSurface,
    allowWrap: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (allowWrap) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 16.dp),
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            maxLines = if (allowWrap) 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileActionSection(
    isPremium: Boolean,
    selectedVoice: String,
    onShowPremium: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onSupport: () -> Unit
) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadii.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ProfileActionRow(
                eyebrow = stringResource(R.string.profile_action_plan_label),
                icon = Icons.Default.Star,
                iconTint = Color(0xFFC98716),
                title = stringResource(R.string.profile_plan_title),
                subtitle = if (isPremium) {
                    stringResource(R.string.profile_plan_active)
                } else {
                    stringResource(R.string.profile_plan_inactive)
                },
                actionLabel = if (isPremium) {
                    stringResource(R.string.profile_plan_manage)
                } else {
                    stringResource(R.string.profile_plan_upgrade)
                },
                onClick = onShowPremium
            )
            ProfileInfoDivider()
            ProfileActionRow(
                eyebrow = stringResource(R.string.profile_action_voice_label),
                icon = Icons.Default.Settings,
                iconTint = AppColors.PlinCyan,
                title = stringResource(R.string.profile_voice_title),
                subtitle = stringResource(R.string.profile_voice_subtitle, selectedVoice),
                actionLabel = if (isPremium) {
                    stringResource(R.string.profile_voice_open)
                } else {
                    stringResource(R.string.profile_plan_upgrade)
                },
                onClick = {
                    if (isPremium) onShowVoiceSettings() else onShowPremium()
                }
            )
            ProfileInfoDivider()
            ProfileActionRow(
                eyebrow = stringResource(R.string.profile_action_support_label),
                icon = Icons.Default.Info,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.profile_support_title),
                subtitle = stringResource(R.string.profile_support_subtitle),
                actionLabel = stringResource(R.string.profile_support_action),
                onClick = onSupport
            )
        }
    }
}

@Composable
private fun ProfileDeviceSection(
    deviceLabel: String,
    serviceActive: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileSectionHeader(
            title = stringResource(R.string.profile_device_title),
            subtitle = stringResource(R.string.profile_device_hint)
        )

        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadii.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProfileInfoRow(
                    label = stringResource(R.string.profile_device_label),
                    value = deviceLabel
                )
                ProfileInfoDivider()
                ProfileInfoRow(
                    label = stringResource(R.string.profile_listener_label),
                    value = if (serviceActive) {
                        stringResource(R.string.profile_listener_active)
                    } else {
                        stringResource(R.string.profile_listener_inactive)
                    },
                    accent = if (serviceActive) AppColors.PlinCyan else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ProfileDiagnosticsSection(snapshot: ListenerDiagnosticsSnapshot) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileSectionHeader(
            title = "Diagnostico",
            subtitle = "Ultimos eventos del listener y la voz para detectar por que un cobro no se anuncio."
        )

        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadii.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoRow(
                    label = "Listener",
                    value = if (snapshot.connected) "Conectado" else "Desconectado",
                    accent = if (snapshot.connected) AppColors.PlinCyan else MaterialTheme.colorScheme.error
                )
                ProfileInfoDivider()
                ProfileInfoRow(
                    label = "Ultima notificacion",
                    value = formatDiagnosticTime(snapshot.lastNotificationAt)
                )
                ProfileInfoDivider()
                ProfileInfoRow(
                    label = "Ultimo pago captado",
                    value = formatDiagnosticTime(snapshot.lastPaymentAt)
                )
                ProfileInfoDivider()
                ProfileInfoRow(
                    label = "Ultimo rebind",
                    value = buildString {
                        append(formatDiagnosticTime(snapshot.lastRebindAt))
                        if (snapshot.lastRebindAt > 0L) {
                            append(if (snapshot.lastRebindForce) " (force)" else " (soft)")
                        }
                    }
                )
                ProfileInfoDivider()
                ProfileInfoRow(
                    label = "Ultima conexion",
                    value = formatDiagnosticTime(snapshot.lastConnectAt)
                )
                if (snapshot.lastDisconnectAt > 0L) {
                    ProfileInfoDivider()
                    ProfileInfoRow(
                        label = "Ultima desconexion",
                        value = formatDiagnosticTime(snapshot.lastDisconnectAt),
                        accent = MaterialTheme.colorScheme.error
                    )
                }

                ProfileInfoDivider()
                Text(
                    text = "Eventos recientes",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                if (snapshot.eventLines.isEmpty()) {
                    Text(
                        text = "Aun no hay eventos registrados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        snapshot.eventLines.take(10).forEach { line ->
                            Text(
                                text = line,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )

        Text(
            text = value,
            modifier = Modifier.padding(start = 16.dp),
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileInfoDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    )
}

@Composable
private fun ProfileActionRow(
    eyebrow: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AppSpacing.sm, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = eyebrow.uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.7.sp
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = actionLabel,
                color = iconTint,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.End
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileSectionHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDiagnosticTime(timestamp: Long): String {
    if (timestamp <= 0L) return "---"
    return SimpleDateFormat("dd/MM HH:mm:ss", Locale.US).format(Date(timestamp))
}
