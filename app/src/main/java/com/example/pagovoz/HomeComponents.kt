package com.example.pagovoz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
    Profile
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(AppRadii.md),
                color = Color.White.copy(alpha = 0.75f)
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
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    color = accentColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = body,
                    color = accentColor.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = accentColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = actionLabel,
                        tint = Color.White
                    )
                }
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
fun HomeTopBar() {
    val accentTitle = stringResource(R.string.home_greeting_title_accent)

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
                            MaterialTheme.colorScheme.primary,
                            AppColors.BrandPrimaryStrong
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name).uppercase(Locale("es", "PE")),
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)
                        )
                        Text(
                            text = stringResource(R.string.home_greeting_title),
                            color = Color.White,
                            style = if (accentTitle.isBlank()) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            lineHeight = 28.sp
                        )
                        if (accentTitle.isNotBlank()) {
                            Text(
                                text = accentTitle,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                lineHeight = 28.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White.copy(alpha = 0.14f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_voice_pro),
                                contentDescription = stringResource(R.string.home_quick_action_voice),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeListenerStatusCard(
    isListeningEnabled: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (isListeningEnabled) Color(0xFF1FA866) else Color(0xFFE1802F)
    val backgroundColor = if (isListeningEnabled) AppColors.SuccessContainer else AppColors.WarningContainer

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.16f)),
        shadowElevation = AppElevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = backgroundColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isListeningEnabled) {
                        stringResource(R.string.home_listener_status_active_title)
                    } else {
                        stringResource(R.string.home_listener_status_inactive_title)
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (isListeningEnabled) {
                        stringResource(R.string.home_listener_status_action_active)
                    } else {
                        stringResource(R.string.home_listener_status_action_inactive)
                    },
                    color = accentColor,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Surface(
                shape = RoundedCornerShape(AppRadii.pill),
                color = backgroundColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tool_open_label),
                        color = accentColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = if (isListeningEnabled) {
                            stringResource(R.string.home_listener_status_action_active)
                        } else {
                            stringResource(R.string.home_listener_status_action_inactive)
                        },
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeBalanceHeroCard(
    total: Float,
    count: Int,
    isListeningEnabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.lg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AppColors.BrandPrimaryStrong, MaterialTheme.colorScheme.primary)
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
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-22).dp, y = 24.dp)
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = Color.White.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (isListeningEnabled) {
                                stringResource(R.string.home_live_badge_active)
                            } else {
                                stringResource(R.string.home_live_badge_inactive)
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(AppRadii.pill),
                        color = Color.White.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = stringResource(R.string.summary_payments_count_compact, count),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.summary_title_editorial).uppercase(Locale("es", "PE")),
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 0.9.sp
                    )
                    Text(
                        text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total)),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = if (isListeningEnabled) {
                            stringResource(R.string.home_listener_status_active_title)
                        } else {
                            stringResource(R.string.home_listener_status_inactive_title)
                        },
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                HomeHeroTrendLine()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeHeroMetricPill(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.payments_summary_count),
                        value = count.toString()
                    )
                    HomeHeroMetricPill(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.home_feature_listening_title),
                        value = if (isListeningEnabled) {
                            stringResource(R.string.home_feature_badge_ready)
                        } else {
                            stringResource(R.string.home_feature_badge_pending)
                        }
                    )
                }
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
private fun HomeHeroTrendLine() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val linePath = Path().apply {
            moveTo(0f, size.height * 0.72f)
            cubicTo(
                size.width * 0.14f,
                size.height * 0.46f,
                size.width * 0.24f,
                size.height * 0.88f,
                size.width * 0.36f,
                size.height * 0.52f
            )
            cubicTo(
                size.width * 0.48f,
                size.height * 0.16f,
                size.width * 0.58f,
                size.height * 0.80f,
                size.width * 0.70f,
                size.height * 0.44f
            )
            cubicTo(
                size.width * 0.80f,
                size.height * 0.22f,
                size.width * 0.90f,
                size.height * 0.62f,
                size.width,
                size.height * 0.12f
            )
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                endY = size.height
            )
        )
        drawPath(
            path = linePath,
            color = Color.White.copy(alpha = 0.92f),
            style = Stroke(
                width = 6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.cornerPathEffect(18f)
            )
        )
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
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .hablaPagoPressable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
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
                    iconRes = iconRes,
                    contentDescription = label,
                    tint = iconTint,
                    containerColor = iconBackground
                )

                HablaPagoChevron(
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
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
fun HomeRecentActivityCard(
    payments: List<PaymentRecord>,
    onViewAll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_recent_activity_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(R.string.home_recent_activity_view_all),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }

            if (payments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HablaPagoIconTile(
                        iconRes = R.drawable.ic_nav_payments,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        size = AppIconSizes.tileLg,
                        iconSize = AppIconSizes.xl,
                        shape = CircleShape
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.home_recent_activity_empty_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_recent_activity_empty_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    payments.forEachIndexed { index, record ->
                        HomeRecentActivityRow(record = record)
                        if (index != payments.lastIndex) {
                            HomeSectionDivider()
                        }
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
private fun HomeRecentActivityRow(record: PaymentRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.home_recent_activity_payment_prefix, record.sender),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = record.homeActivityTimestamp(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1FA866),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.home_recent_activity_status),
                    color = Color(0xFF1FA866),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
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
        Button(
            onClick = onClearToday,
            shape = RoundedCornerShape(AppRadii.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(text = stringResource(R.string.home_secondary_action_clear))
        }
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
    Box {
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = AppElevation.lg,
            shadowElevation = 14.dp,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    BottomBarItem(
                        label = stringResource(R.string.bottom_nav_home),
                        iconRes = R.drawable.ic_nav_home,
                        selected = selectedTab == DashboardTab.Home,
                        onClick = { onSelect(DashboardTab.Home) }
                    )
                    BottomBarItem(
                        label = stringResource(R.string.bottom_nav_history),
                        iconRes = R.drawable.ic_nav_history,
                        selected = selectedTab == DashboardTab.History,
                        onClick = { onSelect(DashboardTab.History) }
                    )
                    Spacer(modifier = Modifier.width(72.dp))
                    BottomBarItem(
                        label = stringResource(R.string.bottom_nav_reports),
                        iconRes = R.drawable.ic_nav_reports,
                        selected = selectedTab == DashboardTab.Reports,
                        onClick = { onSelect(DashboardTab.Reports) }
                    )
                    BottomBarItem(
                        label = stringResource(R.string.bottom_nav_profile),
                        iconRes = R.drawable.ic_nav_profile,
                        selected = selectedTab == DashboardTab.Profile,
                        onClick = { onSelect(DashboardTab.Profile) }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        CenterBottomBarItem(
            label = stringResource(R.string.bottom_nav_payments),
            selected = selectedTab == DashboardTab.Payments,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp),
            onClick = { onSelect(DashboardTab.Payments) }
        )
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(if (selected) 18.dp else 4.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
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
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 14.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_payments),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
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
                            MaterialTheme.colorScheme.primary,
                            AppColors.BrandPrimaryStrong
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.a11y_navigate_back),
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(AppRadii.pill),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = badgeText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
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
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    }
}





