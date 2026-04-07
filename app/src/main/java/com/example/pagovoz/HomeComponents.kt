package com.example.pagovoz

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import com.example.pagovoz.ui.components.HablaPagoChevron
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class DashboardTab {
    Home,
    History,
    Payments,
    Reports,
    Premium
}

@Composable
fun DashboardHeader(
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = AppElevation.md
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_header_full_title),
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clickable(onClick = onSettingsClick),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.a11y_open_settings),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ListeningStatusPill(
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.pill),
        color = if (isEnabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(
            1.dp,
            if (isEnabled) Color(0xFF1FA866).copy(alpha = 0.22f) else MaterialTheme.colorScheme.error.copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isEnabled) YapeCyan else MaterialTheme.colorScheme.error)
            )
            Text(
                text = if (isEnabled) {
                    stringResource(R.string.summary_live_badge)
                } else {
                    stringResource(R.string.home_status_pill_inactive)
                },
                color = if (isEnabled) Color(0xFF1FA866) else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun SummaryCard(
    total: Float,
    count: Int,
    yesterdayTotal: Float,
    onClick: () -> Unit
) {
    val percentageLabel = buildDailyComparisonLabel(total, yesterdayTotal)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.lg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-22).dp, y = 36.dp)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-20).dp)
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.summary_balance_badge),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = Color(0xFFD5FFF3)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1FA866))
                            )
                            Text(
                                text = stringResource(R.string.summary_payments_count_stacked, count),
                                color = Color(0xFF1FA866),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.summary_title_editorial),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        lineHeight = 32.sp
                    )
                    Text(
                        text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total)),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.summary_payments_count_compact, count),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Text(
                        text = percentageLabel,
                        color = if (yesterdayTotal > 0f) Color(0xFF1FA866) else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.summary_last_update),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        modifier = Modifier.clickable(onClick = onClick),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.summary_view_detail),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.summary_view_detail),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    isEnabled: Boolean,
    onConfigClick: () -> Unit
) {
    Card(
        onClick = onConfigClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isEnabled) Color(0xFFEFFFF2) else Color(0xFFFFF1F1)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isEnabled) Color(0xFF6CDD2E) else Color(0xFFE05A4F)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.status_card_title),
                    color = Color(0xFF263245),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isEnabled) stringResource(R.string.status_active_subtitle) else stringResource(R.string.status_inactive_subtitle),
                    color = if (isEnabled) Color(0xFF6CDD2E) else Color(0xFFE05A4F),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onConfigClick() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6CDD2E),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFFFA08C)
                )
            )
        }
    }
}

@Composable
fun DashboardSectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DashboardCompactPanel(
    isEnabled: Boolean,
    isPremium: Boolean,
    trialDays: Int,
    onStatusClick: () -> Unit,
    onPremiumClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.md)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    CompactInfoCard(
                        eyebrow = stringResource(R.string.home_control_service_label),
                        title = if (isEnabled) {
                            stringResource(R.string.home_control_service_active_title)
                        } else {
                            stringResource(R.string.home_control_service_inactive_title)
                        },
                        supporting = if (isEnabled) {
                            stringResource(R.string.home_control_service_active_support)
                        } else {
                            stringResource(R.string.home_control_service_inactive_support)
                        },
                        actionLabel = stringResource(R.string.home_control_service_action),
                        accent = if (isEnabled) Color(0xFF1FA866) else MaterialTheme.colorScheme.error,
                        background = if (isEnabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer,
                        icon = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        onClick = onStatusClick
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    CompactInfoCard(
                        eyebrow = stringResource(R.string.home_control_premium_label),
                        title = if (isPremium || trialDays > 0) {
                            stringResource(R.string.home_control_premium_active_title)
                        } else {
                            stringResource(R.string.home_control_premium_inactive_title)
                        },
                        supporting = when {
                            trialDays > 0 -> stringResource(R.string.home_control_premium_trial_support, trialDays)
                            isPremium -> stringResource(R.string.home_control_premium_active_support)
                            else -> stringResource(R.string.home_control_premium_inactive_support)
                        },
                        actionLabel = if (isPremium || trialDays > 0) {
                            stringResource(R.string.premium_banner_manage)
                        } else {
                            stringResource(R.string.premium_banner_upgrade)
                        },
                        accent = Color(0xFF9B6A00),
                        background = Color(0xFFFFF4D8),
                        icon = Icons.Default.Star,
                        onClick = onPremiumClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactInfoCard(
    eyebrow: String,
    title: String,
    supporting: String,
    actionLabel: String,
    accent: Color,
    background: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.md),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.14f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(background)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 14.dp, y = (-8).dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.24f))
            )

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.86f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = eyebrow,
                        color = accent.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = supporting,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        lineHeight = 17.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(AppRadii.pill),
                    color = Color.White.copy(alpha = 0.72f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            color = accent,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = actionLabel,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumBannerCard(
    isPremium: Boolean,
    trialDays: Int,
    onClick: () -> Unit
) {
    val bannerTitle = if (isPremium || trialDays > 0) {
        stringResource(R.string.premium_banner_active_title)
    } else {
        stringResource(R.string.premium_banner_upgrade_title)
    }
    val bannerSubtitle = if (isPremium || trialDays > 0) {
        stringResource(R.string.premium_banner_active_subtitle)
    } else {
        stringResource(R.string.premium_banner_upgrade_subtitle)
    }
    val buttonLabel = if (isPremium || trialDays > 0) {
        stringResource(R.string.premium_banner_manage)
    } else {
        stringResource(R.string.premium_banner_upgrade)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC400)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.35f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF8C5900),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = bannerTitle,
                    color = Color(0xFF523400),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = bannerSubtitle,
                    color = Color(0xFF6A4A0F),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2430)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = buttonLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun WarningCard(
    title: String,
    body: String,
    actionLabel: String,
    accentColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(AppRadii.md),
            color = backgroundColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(AppRadii.pill),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actionLabel,
                    color = accentColor,
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = actionLabel,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HomeToolListItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    tint: Color,
    backgroundTint: Color,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(AppRadii.md),
                color = backgroundTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (iconRes != null) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(AppRadii.pill),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.tool_open_label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FeaturedToolCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.65f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.65f)
                    ) {
                        Text(
                        text = stringResource(R.string.premium_chip_title),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = stringResource(R.string.tool_open_label),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun ToolGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    badgeText: String? = null,
    tint: Color,
    backgroundTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.sm)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 16.dp, y = (-10).dp)
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(backgroundTint.copy(alpha = 0.45f))
            )

            Column(
                modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = backgroundTint
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (iconRes != null) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(21.dp)
                                    )
                                } else if (icon != null) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = tint,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                            }
                        }

                        if (badgeText != null) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = backgroundTint
                            ) {
                                Text(
                                    text = badgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = tint,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = subtitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.tool_open_label),
                    color = tint,
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

@Composable
fun ToolWideCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    backgroundTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = backgroundTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    lineHeight = 16.sp
                )
            }

            Text(
                text = stringResource(R.string.tool_open_label),
                color = tint,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun HomeTopBar(
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090B10),
                            Color(0xFF14101F),
                            Color(0xFF0E161D)
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BrandLogo(
                            modifier = Modifier.size(42.dp),
                            backgroundColor = Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(54.dp)
                            .clickable(onClick = onProfileClick),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, YapeCyan.copy(alpha = 0.38f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.bottom_nav_profile),
                                tint = YapePurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Resumen de hoy",
                    modifier = Modifier.fillMaxWidth(),
                    color = YapeCyan,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun buildTodayDeltaLabel(
    total: Float,
    yesterdayTotal: Float
): String {
    return if (total > 0f) {
        "Cobrado hoy ${stringResourceSafe("S/ ")}${String.format(Locale.US, "%.2f", total)}"
    } else {
        "Sin cobros hoy"
    }
}

@Composable
fun HomeSetupAccessSection(
    notificationEnabled: Boolean,
    restrictedSettingsReady: Boolean,
    batteryOptimizationDisabled: Boolean,
    onOpenNotifications: () -> Unit,
    onOpenRestrictedSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        HomeSetupAccessShortcut(
            modifier = Modifier.weight(1f),
            title = "Notificaciones",
            icon = Icons.Default.Notifications,
            accentColor = if (notificationEnabled) YapePurple else Color(0xFFE1802F),
            isReady = notificationEnabled,
            pendingStatus = "Necesario activar",
            onClick = onOpenNotifications
        )
        HomeSetupAccessShortcut(
            modifier = Modifier.weight(1f),
            title = "Ajustes",
            icon = Icons.Default.Settings,
            accentColor = if (restrictedSettingsReady) YapeCyan else Color(0xFFE1802F),
            isReady = restrictedSettingsReady,
            pendingStatus = "Necesario activar",
            onClick = onOpenRestrictedSettings
        )
        HomeSetupAccessShortcut(
            modifier = Modifier.weight(1f),
            title = "Bater\u00eda",
            iconRes = R.drawable.ic_battery_status,
            accentColor = if (batteryOptimizationDisabled) Color(0xFF30D46C) else Color(0xFFE1802F),
            isReady = batteryOptimizationDisabled,
            pendingStatus = "Revisar",
            onClick = onOpenBatterySettings
        )
    }
}

@Composable
private fun HomeSetupAccessShortcut(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    accentColor: Color,
    isReady: Boolean,
    pendingStatus: String,
    onClick: () -> Unit
) {
    val readyColor = Color(0xFF30D46C)
    val pendingColor = Color(0xFFE1802F)
    val containerColor = if (isReady) {
        accentColor.copy(alpha = 0.16f)
    } else {
        pendingColor.copy(alpha = 0.12f)
    }
    val statusText = if (isReady) "Listo" else pendingStatus
    val statusColor = if (isReady) readyColor else pendingColor

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = title,
            color = if (isReady) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.96f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
            },
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isReady) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeListenerStatusCard(
    isListeningEnabled: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (isListeningEnabled) MaterialTheme.colorScheme.primary else Color(0xFFE1802F)
    val pulseTransition = rememberInfiniteTransition(label = "listener_status_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listener_status_scale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = if (isListeningEnabled) 0.16f else 0.10f,
        targetValue = if (isListeningEnabled) 0.30f else 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listener_status_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = pulseAlpha))
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (!isListeningEnabled) {
                Text(
                    text = stringResource(R.string.home_listener_status_inactive_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(
                text = if (isListeningEnabled) {
                    stringResource(R.string.home_listener_status_active_body)
                } else {
                    stringResource(R.string.home_listener_status_inactive_body)
                },
                color = accentColor,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isListeningEnabled) "Activo" else stringResource(R.string.tool_open_label),
                color = accentColor,
                style = MaterialTheme.typography.labelLarge
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isListeningEnabled) {
                    stringResource(R.string.home_listener_status_action_active)
                } else {
                    stringResource(R.string.home_listener_status_action_inactive)
                },
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun HomeBalanceHeroCard(
    total: Float,
    yesterdayTotal: Float,
    count: Int,
    isListeningEnabled: Boolean,
    onClick: () -> Unit = {}
) {
    val pulseTransition = rememberInfiniteTransition(label = "home_hero_pulse")
    val heroScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home_hero_scale"
    )
    val statusDotScale by pulseTransition.animateFloat(
        initialValue = 0.84f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720),
            repeatMode = RepeatMode.Reverse
        ),
        label = "home_hero_status_dot_scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                if (isListeningEnabled) {
                    scaleX = heroScale
                    scaleY = heroScale
                }
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.lg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            YapePurple,
                            YapeCyan
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 18.dp, y = (-20).dp)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-22).dp, y = 24.dp)
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.10f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total)),
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = buildTodayDeltaLabel(total, yesterdayTotal),
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = Color.Black.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = stringResource(R.string.summary_payments_count_compact, count),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = Color.Black.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (isListeningEnabled) {
                                stringResource(R.string.home_feature_badge_ready)
                            } else {
                                stringResource(R.string.home_feature_badge_pending)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                HomeHeroWaveform(alpha = if (isListeningEnabled) 0.92f else 0.56f)
            }
        }
    }
}

@Composable
private fun HomeHeroMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun HomeHeroWaveform(
    alpha: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val bars = listOf(
            0.10f, 0.16f, 0.36f, 0.62f, 0.30f, 0.18f, 0.12f, 0.18f, 0.16f, 0.12f,
            0.24f, 0.46f, 0.72f, 0.42f, 0.20f, 0.12f, 0.20f, 0.16f, 0.20f, 0.42f,
            0.64f, 0.48f, 0.18f, 0.14f, 0.10f, 0.18f, 0.34f, 0.58f, 0.34f, 0.18f,
            0.12f, 0.18f, 0.42f, 0.26f, 0.14f, 0.10f, 0.18f, 0.30f, 0.20f, 0.14f
        )
        val spacing = size.width / (bars.size + 1)
        val centerY = size.height / 2f
        val strokeWidth = spacing * 0.42f

        bars.forEachIndexed { index, bar ->
            val x = spacing * (index + 1)
            val halfHeight = size.height * bar / 2f
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = androidx.compose.ui.geometry.Offset(x, centerY - halfHeight),
                end = androidx.compose.ui.geometry.Offset(x, centerY + halfHeight),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

data class HomeQuickActionItem(
    val label: String,
    val iconRes: Int,
    val iconTint: Color,
    val iconBackground: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeQuickActionsGrid(
    actions: List<HomeQuickActionItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowActions.forEach { action ->
                    HomeQuickActionButton(
                        action = action,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun HomeQuickActionButton(
    action: HomeQuickActionItem,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 130),
        label = "home_quick_action_scale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    action.onClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .padding(horizontal = AppSpacing.item, vertical = AppSpacing.item),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HablaPagoIconTile(
                    iconRes = action.iconRes,
                    contentDescription = action.label,
                    tint = action.iconTint,
                    containerColor = action.iconBackground
                )

                HablaPagoChevron(
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = action.label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.tool_open_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun HomeQuickAction(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    HomeQuickActionButton(
        action = HomeQuickActionItem(
            label = label,
            iconRes = iconRes,
            iconTint = iconTint,
            iconBackground = iconBackground,
            onClick = onClick
        ),
        modifier = modifier
    )
}

@Composable
fun HomeRecentActivityCard(
    title: String,
    payments: List<PaymentRecord>,
    onViewAll: () -> Unit,
    onPaymentClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.home_recent_activity_view_all),
                color = YapeCyan,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
        }

        if (payments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_recent_activity_empty_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.home_recent_activity_empty_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                payments.forEachIndexed { index, record ->
                    HomeRecentActivityRow(
                        record = record,
                        onClick = onPaymentClick
                    )
                    if (index != payments.lastIndex) {
                        HomeSectionDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun HomeFeatureCard(
    title: String,
    body: String,
    badgeText: String,
    iconRes: Int,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hablaPagoPressable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = AppSpacing.item),
        verticalAlignment = Alignment.Top
    ) {
        HablaPagoIconTile(
            iconRes = iconRes,
            tint = iconTint,
            containerColor = iconBackground,
            size = 42.dp
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AppSpacing.item, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(AppRadii.pill),
                    color = iconBackground
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = iconTint,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                maxLines = 2
            )
        }

        HablaPagoChevron(
            modifier = Modifier.padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            size = AppIconSizes.sm,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun HomeSectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    )
}

@Composable
private fun HomeRecentActivityRow(
    record: PaymentRecord,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HablaPagoIconTile(
            iconRes = R.drawable.ic_nav_payments,
            contentDescription = record.sender,
            tint = MaterialTheme.colorScheme.primary,
            containerColor = AppColors.SurfaceBrand,
            size = 44.dp
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = record.sender,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = record.homeActivityTimestamp(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun HomeSecondaryActions(
    onClearToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(
            onClick = onClearToday,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.home_secondary_action_clear),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun HomeReferenceBottomBar(
    onShowHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowReports: () -> Unit,
    onShowPremium: () -> Unit
) {
    Box {
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 18.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    HomeReferenceBottomItem(
                        label = stringResource(R.string.bottom_nav_home),
                        iconRes = R.drawable.ic_nav_home,
                        selected = true,
                        onClick = {}
                    )
                    HomeReferenceBottomItem(
                        label = stringResource(R.string.bottom_nav_history),
                        iconRes = R.drawable.ic_nav_history,
                        selected = false,
                        onClick = onShowHistory
                    )
                    Spacer(modifier = Modifier.width(64.dp))
                    HomeReferenceBottomItem(
                        label = stringResource(R.string.bottom_nav_reports),
                        iconRes = R.drawable.ic_nav_reports,
                        selected = false,
                        onClick = onShowReports
                    )
                    HomeReferenceBottomItem(
                        label = "Premium",
                        iconRes = R.drawable.ic_nav_premium,
                        selected = false,
                        onClick = onShowPremium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        CenterBottomBarItem(
            label = stringResource(R.string.bottom_nav_payments),
            selected = false,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp),
            onClick = onShowPayments
        )
    }
}

@Composable
private fun HomeReferenceBottomItem(
    label: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun HomeActionStrip(
    onShowPayments: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onShowReports: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.home_quick_actions_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        HomeFeatureCard(
            title = stringResource(R.string.bottom_nav_payments),
            body = stringResource(R.string.home_feature_listening_body),
            badgeText = stringResource(R.string.home_feature_badge_ready),
            iconRes = R.drawable.ic_nav_payments,
            iconTint = YapePurple,
            iconBackground = YapePurple.copy(alpha = 0.14f),
            onClick = onShowPayments
        )
        HomeSectionDivider()
        HomeFeatureCard(
            title = stringResource(R.string.bottom_nav_reports),
            body = stringResource(R.string.home_feature_reports_body),
            badgeText = "Premium",
            iconRes = R.drawable.ic_nav_reports,
            iconTint = YapeCyan,
            iconBackground = YapeCyan.copy(alpha = 0.14f),
            onClick = onShowReports
        )
        HomeSectionDivider()
        HomeFeatureCard(
            title = stringResource(R.string.bottom_nav_voice),
            body = stringResource(R.string.home_feature_voice_body),
            badgeText = "Premium",
            iconRes = R.drawable.ic_voice_pro,
            iconTint = YapePurple,
            iconBackground = YapePurple.copy(alpha = 0.14f),
            onClick = onShowVoiceSettings
        )
    }
}

private fun PaymentRecord.homeActivityTimestamp(): String {
    val paymentDate = Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when (paymentDate) {
        today -> SimpleDateFormat("hh:mm a", Locale("es", "PE")).format(Date(timestamp)).uppercase(Locale("es", "PE"))
        today.minusDays(1) -> "Ayer"
        else -> SimpleDateFormat("dd MMM", Locale("es", "PE")).format(Date(timestamp)).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString()
        }
    }
}

@Composable
fun LicenseFooter() {
    Text(
        text = stringResource(R.string.premium_linked_device),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DashboardBottomBar(
    selectedTab: DashboardTab,
    onSelect: (DashboardTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-36).dp, y = 8.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            YapePurple.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 34.dp, y = 10.dp)
                .size(148.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            YapeCyan.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(102.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF101219).copy(alpha = 0.74f),
                            Color(0xFF101515).copy(alpha = 0.90f)
                        )
                    )
                )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = 1.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = AppElevation.flat,
            shadowElevation = 10.dp,
            color = Color(0xFF101515),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.035f),
                        Color.White.copy(alpha = 0.025f)
                    )
                )
            )
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 18.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    BottomBarItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.bottom_nav_home),
                        iconRes = R.drawable.ic_nav_home,
                        selected = selectedTab == DashboardTab.Home,
                        onClick = { onSelect(DashboardTab.Home) }
                    )
                    BottomBarItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.bottom_nav_history),
                        iconRes = R.drawable.ic_nav_history,
                        selected = selectedTab == DashboardTab.History,
                        onClick = { onSelect(DashboardTab.History) }
                    )
                    Spacer(modifier = Modifier.width(82.dp))
                    BottomBarItem(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.bottom_nav_reports),
                        iconRes = R.drawable.ic_nav_reports,
                        selected = selectedTab == DashboardTab.Reports,
                        onClick = { onSelect(DashboardTab.Reports) }
                    )
                    BottomBarItem(
                        modifier = Modifier.weight(1f),
                        label = "Premium",
                        iconRes = R.drawable.ic_nav_premium,
                        selected = selectedTab == DashboardTab.Premium,
                        onClick = { onSelect(DashboardTab.Premium) }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        CenterBottomBarItem(
            label = stringResource(R.string.bottom_nav_payments),
            selected = selectedTab == DashboardTab.Payments,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 6.dp),
            onClick = { onSelect(DashboardTab.Payments) }
        )
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    val itemIconSize = if (label == "Premium") 16.dp else 18.dp
    val itemFontSize = if (label == "Premium") 9.sp else 10.sp

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val activeColor = when (label) {
            stringResourceSafe("Reportes") -> YapeCyan
            stringResourceSafe("Premium") -> YapePurple.copy(alpha = 0.95f)
            stringResourceSafe("Inicio") -> Color(0xFF66F0A4)
            stringResourceSafe("Historial") -> Color(0xFF8DEBFF)
            else -> Color(0xFF57E3FF)
        }
        val inactiveColor = Color(0xFF98A2AF)

        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.size(itemIconSize)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.size(itemIconSize)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = itemFontSize,
            maxLines = 1,
            letterSpacing = if (label == "Premium") (-0.2).sp else 0.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (selected) 18.dp else 4.dp)
                .clip(CircleShape)
                .background(
                    brush = if (selected) {
                        when (label) {
                            stringResourceSafe("Premium") -> {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        YapePurple.copy(alpha = 0.92f),
                                        Color(0xFFB56BFF)
                                    )
                                )
                            }
                            stringResourceSafe("Reportes") -> {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        YapeCyan.copy(alpha = 0.95f),
                                        Color(0xFF8DEBFF)
                                    )
                                )
                            }
                            else -> {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF66F0A4),
                                        Color(0xFF8DEBFF)
                                    )
                                )
                            }
                        }
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    }
                )
        )
    }
}

@Composable
private fun CenterBottomBarItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                YapePurple.copy(alpha = 0.18f),
                                YapeCyan.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clickable(onClick = onClick),
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = 8.dp,
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            YapePurple.copy(alpha = 0.42f),
                            YapeCyan.copy(alpha = 0.42f)
                        )
                    )
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF7E31C6),
                                        Color(0xFF37C6E7)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nav_payments),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (selected) Color(0xFFF4F7FA) else Color(0xFF98A2AF),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}
private fun buildDailyComparisonLabel(total: Float, yesterdayTotal: Float): String {
    if (yesterdayTotal <= 0f) return stringResourceSafe("Sin referencia de ayer")

    val percentage = (((total - yesterdayTotal) / yesterdayTotal) * 100f).roundToInt()
    val prefix = if (percentage >= 0) "+" else ""
    return "$prefix$percentage% vs ayer"
}

private fun stringResourceSafe(fallback: String): String = fallback

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppSectionTopBar(
    title: String,
    onBack: () -> Unit,
    actionIcon: ImageVector? = null,
    actionTint: Color = Color.White,
    badgeText: String? = null,
    actionContentDescription: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            AppColors.BackgroundAccent
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.a11y_navigate_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(AppRadii.pill),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    } else if (actionIcon != null && onAction != null) {
                        IconButton(onClick = onAction) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = actionContentDescription ?: title,
                                tint = actionTint
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}


