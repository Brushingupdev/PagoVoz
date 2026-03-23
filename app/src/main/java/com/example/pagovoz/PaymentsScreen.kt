package com.example.pagovoz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagovoz.ui.components.HablaPagoChevron
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Payments) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> Unit
                    DashboardTab.Reports -> if (isPremium) onShowReports() else onShowPremium()
                    DashboardTab.Profile -> onShowProfile()
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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

            if (payments.isEmpty()) {
                item {
                    PaymentsEmptyState()
                }
            } else {
                item {
                    DashboardSectionHeader(
                        title = stringResource(R.string.payments_feed_title)
                    )
                }
                item {
                    PaymentsFeedCard(records = payments.take(10))
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
    val accentColor = if (isListeningActive) Color(0xFF19B96A) else Color(0xFFF29A38)
    val accentContainer = if (isListeningActive) Color(0xFFE9F8F0) else Color(0xFFFFF2E2)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.md
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.SurfaceBrand.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface,
                            accentContainer.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveMonitoringStrip(isListeningActive = isListeningActive)
                Surface(
                    shape = RoundedCornerShape(AppRadii.pill),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.summary_payments_count_compact, count),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (latestPayment == null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.payments_last_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.payments_last_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_nav_payments),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.payments_last_badge),
                            color = accentColor,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = latestPayment.sender,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmall
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
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Surface(
                            shape = RoundedCornerShape(AppRadii.pill),
                            color = accentContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = stringResource(R.string.history_status_success),
                                    color = accentColor,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaymentsMetricPill(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.payments_summary_total),
                    value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total))
                )
                PaymentsMetricPill(
                    modifier = Modifier.weight(0.8f),
                    label = stringResource(R.string.payments_summary_count),
                    value = count.toString()
                )
                PaymentsMetricPill(
                    modifier = Modifier.weight(1.2f),
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun PaymentsFeedCard(records: List<PaymentRecord>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            LiveFeedSection(records = records)
        }
    }
}

@Composable
private fun PaymentsEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HablaPagoIconTile(
                iconRes = R.drawable.ic_nav_payments,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                size = AppIconSizes.tileLg,
                iconSize = AppIconSizes.xl,
                shape = RoundedCornerShape(18.dp)
            )
            Text(
                text = stringResource(R.string.payments_feed_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.payments_last_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LiveMonitoringStrip(isListeningActive: Boolean) {
    val transition = rememberInfiniteTransition(label = "live-strip")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live-strip-alpha"
    )

    val dotColor = if (isListeningActive) Color(0xFF19B96A) else Color(0xFFF29A38)
    val chipColor = if (isListeningActive) Color(0xFFE9F8F0) else Color(0xFFFFF2E2)
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

    Surface(
        modifier = Modifier.clickable(onClick = {}),
        shape = RoundedCornerShape(AppRadii.pill),
        color = chipColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .alpha(pulseAlpha)
                        .background(dotColor.copy(alpha = 0.18f), CircleShape)
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
                style = MaterialTheme.typography.labelMedium
            )
        }
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
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        records.forEachIndexed { index, record ->
            LiveFeedRow(
                record = record,
                highlight = index == 0
            )
            if (index != records.lastIndex) {
                LiveFeedDivider()
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
            .background(
                color = if (highlight) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(if (highlight) AppRadii.md else 0.dp)
            )
            .padding(horizontal = AppSpacing.xs, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HablaPagoIconTile(
            iconRes = R.drawable.ic_nav_payments,
            tint = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = if (highlight) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            size = AppIconSizes.tileSm,
            iconSize = AppIconSizes.md,
            shape = RoundedCornerShape(14.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = record.sender,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                size = AppIconSizes.sm
            )
        }
    }
}

@Composable
private fun LiveFeedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

private fun PaymentRecord.liveTimestampLabel(): String =
    SimpleDateFormat("HH:mm 'de' dd MMM", Locale("es", "PE"))
        .format(Date(timestamp))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "PE")) else it.toString() }
