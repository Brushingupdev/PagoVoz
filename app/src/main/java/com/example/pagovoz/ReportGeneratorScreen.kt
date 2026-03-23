package com.example.pagovoz

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagovoz.ui.components.HablaPagoChevron
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.YapePurple
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportGeneratorScreen(
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onShowPremium: () -> Unit,
    onShowProfile: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModelFactory(
            sessionRepository = defaultSessionRepository(context)
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPremium = SessionManager.isPremium(context)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { generatePdfDirect(context, it, uiState.reportDate, uiState.reportTotal, uiState.reportHistory) }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.reports_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Reports) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> Unit
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ReportTabSelector(
                selectedTab = uiState.selectedTab,
                onSelect = viewModel::onTabSelected
            )

            ReportSummaryCard(uiState = uiState)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ReportActionCard(
                        title = stringResource(R.string.reports_share_whatsapp_action),
                        iconRes = R.drawable.ic_benefit_whatsapp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF1FA866),
                        onClick = {
                            sharePdfCustom(context, uiState.reportDate, uiState.reportTotal, uiState.reportHistory)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ReportActionCard(
                        title = stringResource(R.string.reports_download_pdf_action),
                        iconRes = R.drawable.ic_benefit_pdf,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            val fileName = "Reporte_Pagos_${uiState.reportDate.replace("-", "")}.pdf"
                            createDocumentLauncher.launch(fileName)
                        }
                    )
                }
            }

            ReportChartsSection(uiState = uiState)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Text(
                        text = stringResource(R.string.reports_recent_payments_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.reports_view_all),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clickable(onClick = onShowHistory)
                    )
                }

            if (uiState.selectedTab == 1 && !uiState.hasYesterdayData) {
                EmptyReportState()
            } else if (uiState.reportHistory.isEmpty()) {
                EmptyReportState()
            } else {
                ReportRecentPaymentsCard(records = uiState.reportHistory.takeLast(3).reversed())
            }
        }
    }
}

@Composable
private fun ReportTabSelector(
    selectedTab: Int,
    onSelect: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = AppElevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReportTabChip(
                label = stringResource(R.string.reports_tab_today_title),
                selected = selectedTab == 0,
                onClick = { onSelect(0) },
                modifier = Modifier.weight(1f)
            )
            ReportTabChip(
                label = stringResource(R.string.reports_tab_yesterday_title),
                selected = selectedTab == 1,
                onClick = { onSelect(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReportTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.lg),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ReportSummaryCard(uiState: ReportsUiState) {
    val isToday = uiState.selectedTab == 0
    val badgeText = if (isToday) {
        stringResource(R.string.reports_summary_badge_today)
    } else {
        stringResource(R.string.reports_summary_badge_yesterday)
    }
    val titleText = if (isToday) {
        stringResource(R.string.reports_summary_title_today)
    } else {
        stringResource(R.string.reports_summary_title_yesterday)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AppColors.SurfaceBrand,
                            MaterialTheme.colorScheme.surface,
                            Color(0xFFF2FBF8)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
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
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Surface(
                    shape = RoundedCornerShape(AppRadii.pill),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.reports_completed_count_short, uiState.reportCount),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFF1FA866),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = titleText,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    lineHeight = 30.sp
                )
                Text(
                    text = uiState.reportDate.toFriendlyReportDate(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", uiState.reportTotal)),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.displaySmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportSummaryMetaPill(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.reports_completed_count_short, uiState.reportCount),
                    value = uiState.reportDate.toFriendlyReportDate()
                )
                ReportSummaryMetaPill(
                    modifier = Modifier.weight(0.8f),
                    label = stringResource(R.string.reports_title),
                    value = if (isToday) {
                        stringResource(R.string.reports_tab_today_title)
                    } else {
                        stringResource(R.string.reports_tab_yesterday_title)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReportSummaryMetaPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
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
private fun ReportChartsSection(uiState: ReportsUiState) {
    val timeBuckets = buildReportTimeBuckets(uiState.reportHistory)
    val trendPoints = buildRecentTrendPoints(uiState.reportHistory)
    val averageAmount = if (uiState.reportCount > 0) {
        uiState.reportTotal / uiState.reportCount
    } else {
        0f
    }
    val peakAmount = uiState.reportHistory.maxOfOrNull { it.amount.toFloat() } ?: 0f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ReportSectionHeader(
                title = stringResource(R.string.reports_charts_title),
                subtitle = stringResource(R.string.reports_charts_subtitle)
            )

            ReportChartMetricStrip(
                averageAmount = compactCurrencyLabel(averageAmount),
                peakAmount = compactCurrencyLabel(peakAmount)
            )

            ReportVolumeChartCard(
                buckets = timeBuckets,
                hasData = uiState.reportHistory.isNotEmpty()
            )

            ReportTrendChartCard(
                points = trendPoints,
                hasData = uiState.reportHistory.isNotEmpty()
            )
        }
    }
}

@Composable
private fun ReportSectionHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}

@Composable
private fun ReportChartMetricStrip(
    averageAmount: String,
    peakAmount: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportMetricCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.reports_chart_average),
                value = averageAmount
            )
            ReportMetricDivider()
            ReportMetricCell(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.reports_chart_peak),
                value = peakAmount
            )
        }
    }
}

@Composable
private fun ReportMetricCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ReportMetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

@Composable
private fun ReportVolumeChartCard(
    buckets: List<ReportTimeBucket>,
    hasData: Boolean
) {
    val maxAmount = buckets.maxOfOrNull { it.total } ?: 0f
    val peakIndex = buckets.indexOfFirst { it.total == maxAmount }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportChartHeader(
                title = stringResource(R.string.reports_chart_volume_title),
                subtitle = stringResource(R.string.reports_chart_volume_subtitle)
            )

            if (!hasData) {
                Text(
                    text = stringResource(R.string.reports_chart_no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    buckets.forEachIndexed { index, bucket ->
                        val fraction = if (maxAmount > 0f) bucket.total / maxAmount else 0f
                        val barHeight = if (bucket.total > 0f) 30.dp + (88.dp * fraction) else 12.dp
                        val barColor = if (index == peakIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = compactCurrencyLabel(bucket.total),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(104.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth(0.56f)
                                        .height(barHeight),
                                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                                    color = barColor
                                ) {}
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = bucket.label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = if (bucket.count > 0) {
                                    stringResource(R.string.reports_chart_bucket_count, bucket.count)
                                } else {
                                    stringResource(R.string.reports_chart_bucket_empty)
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
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
private fun ReportTrendChartCard(
    points: List<ReportTrendPoint>,
    hasData: Boolean
) {
    val maxValue = points.maxOfOrNull { it.value } ?: 0f
    val chartLineColor = MaterialTheme.colorScheme.primary
    val chartFillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val chartGuideColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ReportChartHeader(
                title = stringResource(R.string.reports_chart_trend_title),
                subtitle = stringResource(R.string.reports_chart_trend_subtitle)
            )

            if (!hasData || points.isEmpty()) {
                Text(
                    text = stringResource(R.string.reports_chart_no_data),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp)
                ) {
                    val horizontalPadding = 18.dp.toPx()
                    val chartWidth = size.width - (horizontalPadding * 2)
                    val chartHeight = size.height - 34.dp.toPx()
                    val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f
                    val bottomY = chartHeight

                    repeat(3) { index ->
                        val y = bottomY * (index + 1) / 4f
                        drawLine(
                            color = chartGuideColor,
                            start = Offset(horizontalPadding, y),
                            end = Offset(size.width - horizontalPadding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val pointOffsets = points.mapIndexed { index, point ->
                        val normalized = if (maxValue > 0f) point.value / maxValue else 0f
                        val x = horizontalPadding + (stepX * index)
                        val y = bottomY - (normalized * (chartHeight - 24.dp.toPx()))
                        Offset(x, y)
                    }

                    val linePath = Path()
                    val fillPath = Path()

                    pointOffsets.forEachIndexed { index, offset ->
                        if (index == 0) {
                            linePath.moveTo(offset.x, offset.y)
                            fillPath.moveTo(offset.x, bottomY)
                            fillPath.lineTo(offset.x, offset.y)
                        } else {
                            linePath.lineTo(offset.x, offset.y)
                            fillPath.lineTo(offset.x, offset.y)
                        }
                    }

                    if (pointOffsets.isNotEmpty()) {
                        val lastPoint = pointOffsets.last()
                        fillPath.lineTo(lastPoint.x, bottomY)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            color = chartFillColor
                        )
                        drawPath(
                            path = linePath,
                            color = chartLineColor,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        pointOffsets.forEach { offset ->
                            drawCircle(
                                color = chartLineColor,
                                radius = 4.5.dp.toPx(),
                                center = offset
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }

                ReportTrendLegend(points = points)
            }
        }
    }
}

@Composable
private fun ReportChartHeader(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ReportTrendLegend(points: List<ReportTrendPoint>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        points.forEachIndexed { index, point ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = point.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = compactCurrencyLabel(point.value),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (index != points.lastIndex) {
                ReportTrendLegendDivider()
            }
        }
    }
}

@Composable
private fun ReportTrendLegendDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .height(34.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    )
}

@Composable
private fun ReportActionCard(
    modifier: Modifier = Modifier,
    title: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .hablaPagoPressable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(AppRadii.xl),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.item, vertical = AppSpacing.item),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HablaPagoIconTile(
                    iconRes = iconRes,
                    icon = icon,
                    tint = contentColor,
                    containerColor = contentColor.copy(alpha = 0.12f),
                    iconSize = 22.dp
                )

                HablaPagoChevron(
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ReportRecentPaymentsCard(records: List<PaymentRecord>) {
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
            records.forEachIndexed { index, record ->
                ReportPaymentRow(record = record)
                if (index != records.lastIndex) {
                    ReportPaymentDivider()
                }
            }
        }
    }
}

@Composable
private fun ReportPaymentRow(record: PaymentRecord) {
    val time = SimpleDateFormat("HH:mm a", Locale.US).format(Date(record.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.item),
        verticalAlignment = Alignment.Top
    ) {
        HablaPagoIconTile(
            iconRes = R.drawable.ic_nav_payments,
            tint = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            size = 42.dp,
            iconSize = AppIconSizes.md,
            shape = RoundedCornerShape(14.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = record.sender,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (uiStateFriendlyToday(record.timestamp)) {
                    stringResource(R.string.reports_today_time, time)
                } else {
                    time
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
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

@Composable
private fun ReportPaymentDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

@Composable
private fun EmptyReportState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HablaPagoIconTile(
                iconRes = R.drawable.ic_benefit_pdf,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                size = AppIconSizes.tileLg,
                iconSize = 22.dp,
                shape = RoundedCornerShape(18.dp)
            )
            Text(
                text = stringResource(R.string.reports_empty_transactions),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.reports_empty_supporting),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class ReportTimeBucket(
    val label: String,
    val total: Float,
    val count: Int
)

private data class ReportTrendPoint(
    val label: String,
    val value: Float
)

private fun buildReportTimeBuckets(history: List<PaymentRecord>): List<ReportTimeBucket> {
    val labels = listOf("00-06", "06-12", "12-18", "18-24")
    val totals = MutableList(labels.size) { 0f }
    val counts = MutableList(labels.size) { 0 }

    history.forEach { record ->
        val hour = Instant.ofEpochMilli(record.timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .hour
        val bucketIndex = (hour / 6).coerceIn(0, labels.lastIndex)
        totals[bucketIndex] += record.amount.toFloat()
        counts[bucketIndex] += 1
    }

    return labels.mapIndexed { index, label ->
        ReportTimeBucket(
            label = label,
            total = totals[index],
            count = counts[index]
        )
    }
}

private fun buildRecentTrendPoints(history: List<PaymentRecord>): List<ReportTrendPoint> {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    var cumulative = 0f

    return history
        .sortedBy { it.timestamp }
        .map { record ->
            cumulative += record.amount.toFloat()
            ReportTrendPoint(
                label = Instant.ofEpochMilli(record.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime()
                    .format(formatter),
                value = cumulative
            )
        }
        .takeLast(4)
}

private fun compactCurrencyLabel(amount: Float): String {
    return when {
        amount >= 1000f -> {
            val rounded = ((amount / 100f).toInt()) / 10f
            "S/ ${rounded}k"
        }
        amount > 0f -> "S/ ${amount.toInt()}"
        else -> "S/ 0"
    }
}

private fun String.toFriendlyReportDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "PE")))
    }.getOrElse { this }
}

private fun uiStateFriendlyToday(timestamp: Long): Boolean {
    val now = LocalDate.now()
    val recordDate = Date(timestamp).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return recordDate == now
}

fun sharePdfCustom(context: Context, date: String, total: Float, history: List<PaymentRecord>) {
    try {
        val fileName = "Reporte_Pagos_${date.replace("-", "")}.pdf"
        val cacheFile = File(context.cacheDir, fileName)
        generatePdfToFileCustom(cacheFile, date, total, history)

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
        val totalText = String.format(Locale.US, "%.2f", total)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.reports_share_text, date, totalText))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.reports_share_chooser)))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.reports_share_error, e.message ?: ""), Toast.LENGTH_LONG).show()
    }
}

fun generatePdfToFileCustom(file: File, date: String, total: Float, history: List<PaymentRecord>) {
    val document = PdfDocument()
    val paint = Paint()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 50f

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas = page.canvas

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Reporte de Ventas - HablaPago Pro", margin, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("Fecha del reporte: $date", margin, 80f, paint)
    canvas.drawText("Total Neto Recaudado: S/ ${String.format(Locale.US, "%.2f", total)}", margin, 100f, paint)
    canvas.drawLine(margin, 120f, pageWidth - margin, 120f, paint)

    var y = 150f
    paint.textSize = 12f
    for (record in history) {
        if (y > 800f) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin

            paint.isUnderlineText = true
            canvas.drawText("(Continuación reporte $date - pág $pageNumber)", margin, 30f, paint)
            paint.isUnderlineText = false
            y += 30f
        }

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
        canvas.drawText("$time - ${record.sender}", margin, y, paint)
        canvas.drawText("S/ ${String.format(Locale.US, "%.2f", record.amount)}", pageWidth - 150f, y, paint)
        y += 25f
    }

    document.finishPage(page)
    FileOutputStream(file).use { out -> document.writeTo(out) }
    document.close()
}

fun generatePdfDirect(context: Context, uri: Uri, date: String, total: Float, history: List<PaymentRecord>) {
    val document = PdfDocument()
    val paint = Paint()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 50f

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas = page.canvas

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Reporte de Ventas - HablaPago Pro", margin, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("Fecha del reporte: $date", margin, 80f, paint)
    canvas.drawText("Total Neto Recaudado: S/ ${String.format(Locale.US, "%.2f", total)}", margin, 100f, paint)
    canvas.drawLine(margin, 120f, pageWidth - margin, 120f, paint)

    var y = 150f
    paint.textSize = 12f
    for (record in history) {
        if (y > 800f) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin

            paint.isUnderlineText = true
            canvas.drawText("(Continuación reporte $date - pág $pageNumber)", margin, 30f, paint)
            paint.isUnderlineText = false
            y += 30f
        }

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
        canvas.drawText("$time - ${record.sender}", margin, y, paint)
        canvas.drawText("S/ ${String.format(Locale.US, "%.2f", record.amount)}", pageWidth - 150f, y, paint)
        y += 25f
    }

    document.finishPage(page)

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream -> document.writeTo(outputStream) }
        Toast.makeText(context, context.getString(R.string.reports_pdf_download_ok), Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.reports_pdf_download_error), Toast.LENGTH_LONG).show()
    } finally {
        document.close()
    }
}
