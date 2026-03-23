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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
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
            DashboardBottomBar(selectedTab = DashboardTab.Profile) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> onShowReports()
                    DashboardTab.Profile -> Unit
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileHeroCard(
                isPremium = isPremium,
                trialDays = trialDays,
                serviceActive = serviceActive,
                currentVoice = selectedVoice
            )

            ProfileAccessBlock(
                planContent = {
                    ProfileActionRow(
                        eyebrow = stringResource(R.string.profile_action_plan_label),
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFC98716),
                        iconBackground = Color(0xFFFFF1D6),
                        actionBackground = Color(0xFFFFF8E8),
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
                },
                voiceContent = {
                    ProfileActionRow(
                        eyebrow = stringResource(R.string.profile_action_voice_label),
                        icon = Icons.Default.Settings,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        actionBackground = MaterialTheme.colorScheme.primaryContainer,
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
                },
                supportContent = {
                    ProfileActionRow(
                        eyebrow = stringResource(R.string.profile_action_support_label),
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF2A79C8),
                        iconBackground = Color(0xFFEAF5FF),
                        actionBackground = Color(0xFFF3F9FF),
                        title = stringResource(R.string.profile_support_title),
                        subtitle = stringResource(R.string.profile_support_subtitle),
                        actionLabel = stringResource(R.string.profile_support_action),
                        onClick = {
                            val phoneNumber = "51983450723"
                            val message = context.getString(R.string.premium_whatsapp_message)
                            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    )
                }
            )

            DeviceStatusCard(
                deviceLabel = deviceLabel,
                serviceActive = serviceActive
            )
        }
    }
}

@Composable
private fun ProfileAccessBlock(
    planContent: @Composable () -> Unit,
    voiceContent: @Composable () -> Unit,
    supportContent: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            planContent()
            ProfileBlockDivider()
            voiceContent()
            ProfileBlockDivider()
            supportContent()
        }
    }
}

@Composable
private fun ProfileBlockDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    )
}

@Composable
private fun ProfileHeroCard(
    isPremium: Boolean,
    trialDays: Int,
    serviceActive: Boolean,
    currentVoice: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.md)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.SurfaceBrand,
                            MaterialTheme.colorScheme.surface,
                            Color(0xFFF2FBF8)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(AppRadii.pill),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isPremium) {
                                        stringResource(R.string.profile_badge_premium)
                                    } else {
                                        stringResource(R.string.profile_badge_basic)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.7.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(AppRadii.pill),
                                color = if (serviceActive) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                            ) {
                                Text(
                                    text = if (serviceActive) {
                                        stringResource(R.string.profile_metric_active)
                                    } else {
                                        stringResource(R.string.profile_metric_inactive)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if (serviceActive) Color(0xFF1FA866) else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.7.sp
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.profile_merchant_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
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
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(AppColors.BrandPrimaryStrong, MaterialTheme.colorScheme.primary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.profile_hero_supporting),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileMetricChip(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.profile_metric_service),
                        value = if (serviceActive) {
                            stringResource(R.string.profile_metric_active)
                        } else {
                            stringResource(R.string.profile_metric_inactive)
                        }
                    )
                    ProfileMetricChip(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.profile_metric_plan),
                        value = if (isPremium) {
                            stringResource(R.string.profile_metric_pro)
                        } else {
                            stringResource(R.string.profile_metric_basic)
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HablaPagoIconTile(
                            icon = Icons.Default.Settings,
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            size = 38.dp,
                            iconSize = AppIconSizes.md,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.profile_voice_label),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = currentVoice,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceStatusCard(
    deviceLabel: String,
    serviceActive: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                DashboardSectionHeader(
                    title = stringResource(R.string.profile_device_title),
                    subtitle = stringResource(R.string.profile_device_hint)
                )

                Surface(
                    shape = RoundedCornerShape(AppRadii.pill),
                    color = if (serviceActive) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = if (serviceActive) {
                            stringResource(R.string.profile_listener_active)
                        } else {
                            stringResource(R.string.profile_listener_inactive)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (serviceActive) Color(0xFF1FA866) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }

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
                accent = if (serviceActive) Color(0xFF1FA866) else MaterialTheme.colorScheme.error,
                valueContainer = if (serviceActive) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    accent: Color = Color.Unspecified,
    valueContainer: Color? = null
) {
    val resolvedAccent = if (accent == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        accent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )

        if (valueContainer != null) {
            Surface(
                shape = RoundedCornerShape(AppRadii.pill),
                color = valueContainer
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = resolvedAccent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        } else {
            Text(
                text = value,
                modifier = Modifier.padding(start = 12.dp),
                color = resolvedAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileMetricChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title.uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.7.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ProfileInfoDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    )
}

@Composable
private fun ProfileActionRow(
    eyebrow: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBackground: Color,
    actionBackground: Color,
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
            .padding(vertical = AppSpacing.item),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HablaPagoIconTile(
            icon = icon,
            tint = iconTint,
            containerColor = iconBackground,
            size = 46.dp,
            iconSize = 22.dp
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AppSpacing.sm, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = eyebrow,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.7.sp
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
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

        Surface(
            shape = RoundedCornerShape(AppRadii.pill),
            color = actionBackground
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    color = iconTint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
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
}
