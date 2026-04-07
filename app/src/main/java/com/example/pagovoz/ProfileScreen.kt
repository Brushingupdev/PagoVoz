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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppSpacing

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
    val serviceActive = isNotificationServiceEnabled(context)
    val deviceLabel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.profile_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
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
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
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
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                    color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                accent = if (serviceActive) Color(0xFF1FA866) else MaterialTheme.colorScheme.error
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
    Column(
        modifier = Modifier.fillMaxWidth(),
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
            iconTint = MaterialTheme.colorScheme.primary,
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
            iconTint = Color(0xFF2A79C8),
            title = stringResource(R.string.profile_support_title),
            subtitle = stringResource(R.string.profile_support_subtitle),
            actionLabel = stringResource(R.string.profile_support_action),
            onClick = onSupport
        )
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
        DashboardSectionHeader(
            title = stringResource(R.string.profile_device_title),
            subtitle = stringResource(R.string.profile_device_hint)
        )

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
            accent = if (serviceActive) Color(0xFF1FA866) else MaterialTheme.colorScheme.error
        )
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
            .hablaPagoPressable(interactionSource)
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
