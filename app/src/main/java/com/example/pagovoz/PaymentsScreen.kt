package com.example.pagovoz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagovoz.ui.components.HablaPagoChevron
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    onShowReports: () -> Unit,
    onShowPremium: () -> Unit,
    onShowProfile: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var payments by remember { mutableStateOf(SessionManager.getPaymentHistory(context).reversed()) }
    val total = SessionManager.getDailyTotal(context)
    val count = SessionManager.getDailyCount(context)
    val isPremium = SessionManager.isPremium(context)
    val isListeningActive = isNotificationServiceEnabled(context)
    val latestPayment = payments.firstOrNull()

    LaunchedEffect(Unit) {
        SessionManager.updates.collectLatest {
            payments = SessionManager.getPaymentHistory(context).reversed()
        }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.payments_screen_title),
                onBack = onBack
            )
        },
        containerColor = Color(0xFF090B10),
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Payments) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> Unit
                    DashboardTab.Reports -> if (isPremium) onShowReports() else onShowPremium()
                    DashboardTab.Premium -> onShowPremium()
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090B10),
                            Color(0xFF161224),
                            Color(0xFF0F1820),
                            Color(0xFF090B10)
                        )
                    )
                )
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PaymentsOverviewCard(
                        isListeningActive = isListeningActive,
                        latestPayment = latestPayment,
                        total = total,
                        count = count,
                        lastActivity = latestPayment?.liveTimestampLabel()
                            ?: stringResource(R.string.payments_summary_waiting)
                    )
                }

                if (payments.size > 1) {
                    item {
                        DashboardSectionHeader(
                            title = stringResource(R.string.payments_feed_title)
                        )
                    }
                    item {
                        LiveFeedSection(records = payments.drop(1).take(10))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsOverviewCard(
    isListeningActive: Boolean,
    latestPayment: PaymentRecord?,
    total: Float,
    count: Int,
    lastActivity: String
) {
    val accentColor = if (isListeningActive) YapeCyan else Color(0xFFF29A38)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LiveMonitoringStrip(isListeningActive = isListeningActive)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (latestPayment == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LiveScanningAntennaGlyph()
                    Text(
                        text = if (isListeningActive) {
                            stringResource(R.string.payments_empty_title)
                        } else {
                            stringResource(R.string.payments_live_inactive)
                        },
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.payments_empty_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    LiveEnergyLine(isListeningActive = isListeningActive)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiveSignalGlyph(isListeningActive = isListeningActive)

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.payments_last_badge),
                            color = accentColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = latestPayment.sender,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = latestPayment.liveTimestampLabel(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", latestPayment.amount)),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF1FA866),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.history_status_success),
                                color = Color(0xFF1FA866),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                LiveEnergyLine(isListeningActive = isListeningActive)
            }

            // Separador sutil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                PaymentsMetricPill(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.payments_summary_total),
                    value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total))
                )
                PaymentsMetricsDivider()
                PaymentsMetricPill(
                    modifier = Modifier
                        .weight(0.8f)
                        .padding(start = 12.dp),
                    label = stringResource(R.string.payments_summary_count),
                    value = count.toString()
                )
                PaymentsMetricsDivider()
                PaymentsMetricPill(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(start = 12.dp),
                    label = stringResource(R.string.payments_summary_last_activity),
                    value = lastActivity
                )
            }
        }
    }
}

@Composable
private fun PaymentsMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun PaymentsFeedCard(records: List<PaymentRecord>) {
    LiveFeedSection(records = records)
}



@Composable
private fun LiveScanningAntennaGlyph() {
    val transition = rememberInfiniteTransition(label = "live-scanning-antenna")

    // Rotación continua 360° del barrido radar
    val radarSweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar-sweep-rotation"
    )

    // Pulso de las ondas concéntricas (escala)
    val waveScale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-scale"
    )

    // Opacidad de las ondas (se desvanecen al expandirse)
    val waveAlpha by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-alpha"
    )

    // Segunda onda desfasada
    val waveScale2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-scale-2"
    )
    val waveAlpha2 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-alpha-2"
    )

    // Tercera onda desfasada
    val waveScale3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-scale-3"
    )
    val waveAlpha3 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-alpha-3"
    )

    // Glow pulsante del centro
    val centerGlow by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "center-glow"
    )

    Box(
        modifier = Modifier
            .size(110.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        YapeCyan.copy(alpha = 0.10f),
                        YapePurple.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val maxRadius = size.minDimension * 0.42f

            // === Ondas concéntricas pulsantes (3 desfasadas) ===
            // Onda 1
            drawCircle(
                color = YapeCyan.copy(alpha = waveAlpha * 0.5f),
                radius = maxRadius * waveScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Onda 2
            drawCircle(
                color = YapeCyan.copy(alpha = waveAlpha2 * 0.5f),
                radius = maxRadius * waveScale2,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Onda 3
            drawCircle(
                color = YapeCyan.copy(alpha = waveAlpha3 * 0.5f),
                radius = maxRadius * waveScale3,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // === Barrido (sweep) del radar giratorio ===
            rotate(degrees = radarSweep, pivot = center) {
                // Línea de barrido principal
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            YapeCyan.copy(alpha = 0.9f),
                            YapeCyan.copy(alpha = 0.0f)
                        ),
                        start = center,
                        end = Offset(cx, cy - maxRadius)
                    ),
                    start = center,
                    end = Offset(cx, cy - maxRadius),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Estela / cola del radar (arco más ancho semi-transparente)
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.08f to YapeCyan.copy(alpha = 0.22f),
                        0.15f to YapeCyan.copy(alpha = 0.08f),
                        0.2f to Color.Transparent,
                        1f to Color.Transparent
                    ),
                    startAngle = -90f,
                    sweepAngle = -55f,
                    useCenter = true,
                    topLeft = Offset(cx - maxRadius, cy - maxRadius),
                    size = Size(maxRadius * 2, maxRadius * 2)
                )
            }

            // === Mástil de la antena (línea vertical desde centro hacia abajo) ===
            drawLine(
                color = Color(0xFF5EA8D4),
                start = center,
                end = Offset(cx, cy + maxRadius * 0.72f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Base de la antena
            drawLine(
                color = Color(0xFF5EA8D4),
                start = Offset(cx - 8.dp.toPx(), cy + maxRadius * 0.72f),
                end = Offset(cx + 8.dp.toPx(), cy + maxRadius * 0.72f),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // === Arcos de señal superiores (3 arcos captando) ===
            val arcOffsetY = cy - maxRadius * 0.15f
            // Arco interno
            drawArc(
                color = YapeCyan.copy(alpha = centerGlow * 0.85f),
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(cx - 8.dp.toPx(), arcOffsetY - 8.dp.toPx()),
                size = Size(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
            )
            // Arco medio
            drawArc(
                color = YapeCyan.copy(alpha = centerGlow * 0.62f),
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(cx - 13.dp.toPx(), arcOffsetY - 13.dp.toPx()),
                size = Size(26.dp.toPx(), 26.dp.toPx()),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            // Arco externo
            drawArc(
                color = YapeCyan.copy(alpha = centerGlow * 0.40f),
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(cx - 18.dp.toPx(), arcOffsetY - 18.dp.toPx()),
                size = Size(36.dp.toPx(), 36.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // === Punto central luminoso ===
            drawCircle(
                color = YapeCyan.copy(alpha = centerGlow * 0.3f),
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                color = YapeCyan.copy(alpha = centerGlow),
                radius = 4.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = centerGlow * 0.8f),
                radius = 2.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
private fun LiveMonitoringStrip(isListeningActive: Boolean) {
    val transition = rememberInfiniteTransition(label = "live-strip")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live-strip-alpha"
    )
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live-strip-scale"
    )

    val dotColor = if (isListeningActive) Color(0xFF30D46C) else Color(0xFFF29A38)
    val title = if (isListeningActive) {
        stringResource(R.string.payments_live_strip_active)
    } else {
        stringResource(R.string.payments_live_strip_inactive)
    }
    val badge = if (isListeningActive) {
        stringResource(R.string.payments_live_strip_badge_active)
    } else {
        stringResource(R.string.payments_live_strip_badge_inactive)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Dot con anillo pulsante
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha * 0.4f)
                        .background(dotColor.copy(alpha = 0.15f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )
            }

            Text(
                text = title,
                color = dotColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = badge,
            color = dotColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LiveSignalGlyph(isListeningActive: Boolean) {
    val accentColor = if (isListeningActive) YapeCyan else Color(0xFFF29A38)
    val transition = rememberInfiniteTransition(label = "signal-glyph")

    // Mini-radar rotando
    val miniRadar by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mini-radar-rotation"
    )
    // Pulso de glow
    val glowPulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow-pulse"
    )

    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        // Fondo con glow animado
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isListeningActive) listOf(
                            accentColor.copy(alpha = glowPulse * 0.28f),
                            YapePurple.copy(alpha = 0.10f),
                            Color.Transparent
                        ) else listOf(
                            accentColor.copy(alpha = glowPulse * 0.20f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            accentColor.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        )

        // Mini radar Canvas
        Canvas(modifier = Modifier.size(40.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val r = size.minDimension * 0.40f

            // Círculos concéntricos
            drawCircle(
                color = accentColor.copy(alpha = 0.15f),
                radius = r,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.10f),
                radius = r * 0.6f,
                center = center,
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Línea de escaneo rotando
            rotate(degrees = miniRadar, pivot = center) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.85f),
                            accentColor.copy(alpha = 0.0f)
                        ),
                        start = center,
                        end = Offset(cx, cy - r)
                    ),
                    start = center,
                    end = Offset(cx, cy - r),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Centro brillante
            drawCircle(
                color = accentColor.copy(alpha = glowPulse),
                radius = 3.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = glowPulse * 0.7f),
                radius = 1.5.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
private fun LiveEnergyLine(isListeningActive: Boolean) {
    val accentColor = if (isListeningActive) YapeCyan else Color(0xFFF29A38)
    val transition = rememberInfiniteTransition(label = "energy-line")

    // Desplazamiento horizontal continuo para efecto de onda viva
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2832f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave-offset"
    )
    // Pulso de amplitud
    val ampPulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "amp-pulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val segments = 120
        val stepX = w / segments.toFloat()

        // Dibujar línea de onda sinusoidal animada con gradiente
        val path = Path()
        for (i in 0..segments) {
            val x = i * stepX
            val norm = i.toFloat() / segments
            // Combinación de frecuencias para efecto más orgánico
            val y = centerY + (
                sin((norm * 12f) + waveOffset) * h * 0.22f * ampPulse +
                sin((norm * 20f) + waveOffset * 1.5f) * h * 0.10f * ampPulse +
                sin((norm * 6f) + waveOffset * 0.7f) * h * 0.08f
            ).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Onda principal
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.0f),
                    accentColor.copy(alpha = if (isListeningActive) 0.7f else 0.4f),
                    accentColor.copy(alpha = if (isListeningActive) 0.9f else 0.5f),
                    accentColor.copy(alpha = if (isListeningActive) 0.7f else 0.4f),
                    accentColor.copy(alpha = 0.0f)
                )
            ),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Brillo central sutil (línea base)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.08f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, centerY),
            end = Offset(w, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun LatestLivePaymentCard(record: PaymentRecord?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.md)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (record == null) {
                Text(
                    text = stringResource(R.string.payments_last_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF19B96A), CircleShape)
                    )
                    Text(
                        text = stringResource(R.string.payments_last_badge),
                        color = Color(0xFF19B96A),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

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
                            text = record.sender,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = record.liveTimestampLabel(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.displaySmall
                        )
                        Surface(
                            shape = RoundedCornerShape(AppRadii.pill),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = stringResource(R.string.history_status_success),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = Color(0xFF1FA866),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsMetricsStrip(
    total: Float,
    count: Int,
    lastActivity: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = AppElevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentsMetricColumn(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.payments_summary_total),
                value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total))
            )
            PaymentsMetricsDivider()
            PaymentsMetricColumn(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.payments_summary_count),
                value = count.toString()
            )
            PaymentsMetricsDivider()
            PaymentsMetricColumn(
                modifier = Modifier.weight(1.25f),
                label = stringResource(R.string.payments_summary_last_activity),
                value = lastActivity,
                emphasize = false
            )
        }
    }
}

@Composable
private fun PaymentsMetricColumn(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    emphasize: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PaymentsMetricsDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    )
}

@Composable
private fun LiveFeedSection(records: List<PaymentRecord>) {
    Column {
        records.forEachIndexed { index, record ->
            LiveFeedRow(
                record = record,
                highlight = index == 0
            )
            if (index != records.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(YapeCyan.copy(alpha = 0.10f))
                )
            }
        }
    }
}

@Composable
private fun LiveFeedRow(
    record: PaymentRecord,
    highlight: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Ícono con mini radar para highlight, ícono normal para el resto
            if (highlight) {
                LiveFeedPulseIcon()
            } else {
                HablaPagoIconTile(
                    iconRes = R.drawable.ic_nav_payments,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    containerColor = YapeCyan.copy(alpha = 0.08f),
                    size = AppIconSizes.tileSm,
                    iconSize = AppIconSizes.md,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.sender,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF19B96A), CircleShape)
                    )
                    Text(
                        text = record.liveTimestampLabel(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
                    color = if (highlight) YapeCyan else Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF1FA866),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.payments_live_title),
                        color = Color(0xFF1FA866),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (highlight) {
                Spacer(modifier = Modifier.width(6.dp))
                HablaPagoChevron(
                    tint = YapeCyan.copy(alpha = 0.65f),
                    size = AppIconSizes.sm
                )
        }
    }
}

@Composable
private fun LiveFeedPulseIcon() {
    val transition = rememberInfiniteTransition(label = "feed-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "feed-icon-pulse"
    )

    Box(
        modifier = Modifier.size(AppIconSizes.tileSm),
        contentAlignment = Alignment.Center
    ) {
        // Glow exterior
        Box(
            modifier = Modifier
                .size(AppIconSizes.tileSm)
                .scale(1f + (pulse - 0.6f) * 0.5f)
                .alpha(pulse * 0.3f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            YapeCyan.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
        )
        HablaPagoIconTile(
            iconRes = R.drawable.ic_nav_payments,
            tint = YapeCyan,
            containerColor = YapeCyan.copy(alpha = 0.18f),
            size = AppIconSizes.tileSm,
            iconSize = AppIconSizes.md,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

private fun PaymentRecord.liveTimestampLabel(): String =
    SimpleDateFormat("HH:mm 'de' dd MMM", Locale("es", "PE"))
        .format(Date(timestamp))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }
