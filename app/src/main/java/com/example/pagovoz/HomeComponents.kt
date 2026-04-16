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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }

                    // Botón de Perfil con estilo Premium Glassmorphism
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(onClick = onProfileClick),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.04f),
                        border = BorderStroke(1.dp, Brush.linearGradient(
                            colors = listOf(YapeCyan.copy(alpha = 0.4f), YapePurple.copy(alpha = 0.4f))
                        ))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.bottom_nav_profile),
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(24.dp)
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

                HomeHeroWaveform(
                    alpha = if (isListeningEnabled) 0.92f else 0.56f,
                    isAnimating = isListeningEnabled
                )
            }
        }
    }
}



@Composable
fun HomeWidgetPinCard(
    isPinned: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = if (isPinned) ({}) else onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161C26)),
        border = BorderStroke(1.dp, Color(0xFF1DB870).copy(alpha = if (isPinned) 0.1f else 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1DB870).copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = Color(0xFF1DB870),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isPinned) "Widget activado" else "¿Quieres ver tus cobros en Inicio?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (isPinned) "Tu resumen está en vivo fuera de la app" else "Añade el widget oficial de HablaPago",
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isPinned) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1DB870),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF1DB870)
                ) {
                    Text(
                        text = "Añadir",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeroWaveform(
    alpha: Float,
    isAnimating: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_time"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        val baseBars = listOf(
            0.10f, 0.16f, 0.36f, 0.62f, 0.30f, 0.18f, 0.12f, 0.18f, 0.16f, 0.12f,
            0.24f, 0.46f, 0.72f, 0.42f, 0.20f, 0.12f, 0.20f, 0.16f, 0.20f, 0.42f,
            0.64f, 0.48f, 0.18f, 0.14f, 0.10f, 0.18f, 0.34f, 0.58f, 0.34f, 0.18f,
            0.12f, 0.18f, 0.42f, 0.26f, 0.14f, 0.10f, 0.18f, 0.30f, 0.20f, 0.14f
        )
        val spacing = size.width / (baseBars.size + 1)
        val centerY = size.height / 2f
        val strokeWidth = spacing * 0.42f

        baseBars.forEachIndexed { index, baseBar ->
            val scale = if (isAnimating) {
                0.65f + 0.35f * kotlin.math.sin(time + index * 0.5f).toFloat()
            } else {
                1f
            }
            val bar = baseBar * scale
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



@Composable
fun DashboardSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
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
fun HomeActionStrip(
    onShowPayments: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onShowReports: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título de la sección
        Text(
            text = stringResource(R.string.home_quick_actions_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeQuickGridItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.bottom_nav_payments),
                subtitle = "Monitorear",
                iconRes = R.drawable.ic_nav_payments,
                iconTint = YapePurple,
                onClick = onShowPayments
            )
            HomeQuickGridItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.bottom_nav_reports),
                subtitle = "Exportar",
                iconRes = R.drawable.ic_nav_reports,
                iconTint = YapeCyan,
                onClick = onShowReports
            )
            HomeQuickGridItem(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.bottom_nav_voice),
                subtitle = "Ajustar",
                iconRes = R.drawable.ic_voice_pro,
                iconTint = YapePurple,
                onClick = onShowVoiceSettings
            )
        }
    }
}

@Composable
private fun HomeQuickGridItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconRes: Int,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Brush.linearGradient(
            colors = listOf(iconTint.copy(alpha = 0.6f), Color.Transparent)
        ))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon Header
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Text Footer
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
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


