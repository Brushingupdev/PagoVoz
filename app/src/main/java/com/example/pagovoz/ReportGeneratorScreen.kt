package com.example.pagovoz

import android.content.Context
import android.content.Intent
import android.content.ClipData
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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
    var exportMode by remember { mutableStateOf(ReportExportMode.Pdf) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { generatePdfDirect(context, it, uiState.reportDate, uiState.reportTotal, uiState.reportHistory) }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.bottom_nav_reports),
                onBack = onBack,
                actionIcon = Icons.Default.Search,
                onAction = onShowHistory,
                actionContentDescription = stringResource(R.string.history_search_label)
            )
        },
        containerColor = Color(0xFF090B10),
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Reports) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> Unit
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
            ReportFiltersSection(
                selectedRange = uiState.selectedRange,
                rangeLabel = uiState.reportDate,
                rangeNotice = uiState.rangeNotice,
                onSelect = viewModel::onRangeSelected
            )

            ReportMetricCards(uiState = uiState)

            ReportAnalyticsOverview(uiState = uiState)

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ReportExportModeSelector(
                    selectedMode = exportMode,
                    onSelect = { exportMode = it }
                )

                ReportPrimaryExportButton(
                    exportMode = exportMode,
                    onClick = {
                        when (exportMode) {
                            ReportExportMode.Pdf -> {
                                val fileName = "Reporte_Pagos_${uiState.selectedRange.fileKey}_${uiState.reportDate.toSafeFileToken()}.pdf"
                                createDocumentLauncher.launch(fileName)
                            }
                            ReportExportMode.WhatsApp -> {
                                sharePdfCustom(context, uiState.reportDate, uiState.reportTotal, uiState.reportHistory)
                            }
                        }
                    }
                )
            }
        }
        }
    }
}

private enum class ReportExportMode {
    Pdf,
    WhatsApp
}

@Composable
private fun ReportFiltersSection(
    selectedRange: ReportRangeFilter,
    rangeLabel: String,
    rangeNotice: String?,
    onSelect: (ReportRangeFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReportFilterChip(
                label = ReportRangeFilter.Today.label,
                selected = selectedRange == ReportRangeFilter.Today,
                onClick = { onSelect(ReportRangeFilter.Today) },
                modifier = Modifier.weight(1f)
            )
            ReportFilterChip(
                label = ReportRangeFilter.Week.label,
                selected = selectedRange == ReportRangeFilter.Week,
                onClick = { onSelect(ReportRangeFilter.Week) },
                modifier = Modifier.weight(1f)
            )
            ReportFilterChip(
                label = ReportRangeFilter.Month.label,
                selected = selectedRange == ReportRangeFilter.Month,
                onClick = { onSelect(ReportRangeFilter.Month) },
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
            ))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Periodo activo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (rangeNotice != null) {
                        Surface(
                            shape = CircleShape,
                            color = YapeCyan.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = rangeNotice,
                                color = YapeCyan,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = rangeLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}

@Composable
private fun ReportFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.pill),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ReportAnalyticsOverview(uiState: ReportsUiState) {
    val buckets = buildReportTimeBuckets(
        history = uiState.reportHistory,
        filter = uiState.selectedRange
    )
    val trendPoints = buildCumulativeTrendPoints(buckets)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Análisis de Rendimiento",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Resumen visual del ${uiState.selectedRange.summaryLabel}",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelMedium
            )
        }

        ReportMiniBarChart(
            buckets = buckets,
            maxAmount = buckets.maxOfOrNull { it.total } ?: 0f,
            selectedRange = uiState.selectedRange
        )

        ReportTrendLineChart(
            points = trendPoints,
            selectedRange = uiState.selectedRange
        )
    }
}

@Composable
private fun ReportMiniBarChart(
    buckets: List<ReportTimeBucket>,
    maxAmount: Float,
    selectedRange: ReportRangeFilter
) {
    val iconTint = YapePurple
    val primaryBarColor = MaterialTheme.colorScheme.primary
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    var selectedIndex by remember(buckets) {
        mutableStateOf(
            buckets.indexOfLast { it.count > 0 }
                .takeIf { it >= 0 }
                ?: if (buckets.isNotEmpty()) 0 else -1
        )
    }
    var tooltipAnchor by remember(buckets) { mutableStateOf<Offset?>(null) }
    val chartTitle = if (selectedRange == ReportRangeFilter.Today) {
        "Ingresos por tramo horario"
    } else {
        "Ingresos por día"
    }
    val selectedBucket = buckets.getOrNull(selectedIndex)
    val totalAmount = buckets.sumOf { it.total.toDouble() }.toFloat().coerceAtLeast(0f)
    val averageAmount = if (buckets.isNotEmpty()) totalAmount / buckets.size else 0f
    val animatedTotals = buckets.map { bucket ->
        animateFloatAsState(
            targetValue = bucket.total,
            animationSpec = tween(durationMillis = 700),
            label = "report-bar-${bucket.label}"
        ).value
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = chartTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Toca una barra para comparar monto, volumen y participación.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        selectedBucket?.let { bucket ->
            val share = if (totalAmount > 0f) (bucket.total / totalAmount) * 100f else 0f
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Brush.linearGradient(
                    colors = listOf(iconTint.copy(alpha = 0.4f), Color.Transparent)
                ))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = bucket.label,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${bucket.detailLabel} · ${bucket.count} cobros",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", bucket.total)),
                            color = iconTint,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${share.formatOneDecimal()}% del total",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        if (selectedBucket != null && totalAmount > 0f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportMiniInsightPill(
                    modifier = Modifier.weight(1f),
                    label = "Tramo activo",
                    value = selectedBucket.detailLabel
                )
                ReportMiniInsightPill(
                    modifier = Modifier.weight(1f),
                    label = "Promedio",
                    value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", averageAmount))
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
                .pointerInput(buckets) {
                    fun updateSelection(offset: Offset) {
                        if (buckets.isEmpty()) return
                        val slotWidth = size.width / buckets.size.toFloat()
                        val nextIndex = (offset.x / slotWidth)
                            .toInt()
                            .coerceIn(0, buckets.lastIndex)
                        if (nextIndex != selectedIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        selectedIndex = nextIndex
                        tooltipAnchor = offset
                    }

                    detectTapGestures { offset ->
                        updateSelection(offset)
                    }
                }
                .pointerInput(buckets) {
                    fun updateSelection(offset: Offset) {
                        if (buckets.isEmpty()) return
                        val slotWidth = size.width / buckets.size.toFloat()
                        val nextIndex = (offset.x / slotWidth)
                            .toInt()
                            .coerceIn(0, buckets.lastIndex)
                        if (nextIndex != selectedIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        selectedIndex = nextIndex
                        tooltipAnchor = offset
                    }

                    detectDragGestures(
                        onDragStart = { offset -> updateSelection(offset) },
                        onDrag = { change, _ ->
                            updateSelection(change.position)
                            change.consume()
                        }
                    )
                }
        ) {
            val tooltipWidthPx = with(density) { 154.dp.toPx() }
            val tooltipHeightPx = with(density) { 72.dp.toPx() }
            val maxWidthPx = with(density) { maxWidth.toPx() }

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val leftPadding = 8.dp.toPx()
                val topPadding = 12.dp.toPx()
                val bottomPadding = 28.dp.toPx()
                val baseY = size.height - bottomPadding
                val slotWidth = (size.width - leftPadding) / buckets.size.coerceAtLeast(1)
                val barWidth = slotWidth * 0.52f
                val averageY = if (maxAmount > 0f) {
                    baseY - ((averageAmount / maxAmount) * (baseY - topPadding))
                } else {
                    baseY
                }

                repeat(3) { index ->
                    val y = baseY - ((index + 1) * ((baseY - topPadding) / 4f))
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (averageAmount > 0f) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.18f),
                        start = Offset(leftPadding, averageY),
                        end = Offset(size.width, averageY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                buckets.forEachIndexed { index, _ ->
                    val x = leftPadding + index * slotWidth + (slotWidth * 0.24f)
                    val barHeight = if (maxAmount > 0f) {
                        (animatedTotals[index] / maxAmount) * (baseY - topPadding)
                    } else {
                        0f
                    }
                    val topLeft = Offset(x, baseY - barHeight)
                    val selected = index == selectedIndex

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                iconTint.copy(alpha = if (selected) 1f else 0.85f),
                                YapeCyan.copy(alpha = if (selected) 0.95f else 0.65f)
                            ),
                            startY = topLeft.y,
                            endY = baseY
                        ),
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(100f, 100f) // Ultra redondeado
                    )

                    if (selected) {
                        val guideX = x + (barWidth / 2f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.30f),
                            start = Offset(guideX, topPadding),
                            end = Offset(guideX, baseY),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(guideX, topLeft.y)
                        )
                    }
                }
            }

            val clampedTooltipX = ((tooltipAnchor?.x ?: 0f) - (tooltipWidthPx / 2f))
                .coerceIn(0f, (maxWidthPx - tooltipWidthPx).coerceAtLeast(0f))
            val tooltipY = ((tooltipAnchor?.y ?: 0f) - tooltipHeightPx - with(density) { 8.dp.toPx() })
                .coerceAtLeast(0f)

            selectedBucket?.let { bucket ->
                tooltipAnchor?.let {
                    ReportFingerTooltip(
                        title = bucket.label,
                        value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", bucket.total)),
                        supporting = "${bucket.detailLabel} · ${bucket.count} cobros",
                        modifier = Modifier.offset {
                            IntOffset(clampedTooltipX.roundToInt(), tooltipY.roundToInt())
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            buckets.forEachIndexed { index, bucket ->
                Text(
                    text = bucket.axisLabel(),
                    color = if (index == selectedIndex) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ReportMiniInsightPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
private fun ReportTrendLineChart(
    points: List<ReportTrendPoint>,
    selectedRange: ReportRangeFilter
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val trendPrimaryColor = MaterialTheme.colorScheme.primary
    var selectedIndex by remember(points) {
        mutableStateOf(if (points.isNotEmpty()) points.lastIndex else -1)
    }
    var tooltipAnchor by remember(points) { mutableStateOf<Offset?>(null) }
    val selectedPoint = points.getOrNull(selectedIndex)
    val maxValue = points.maxOfOrNull { it.value } ?: 0f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (selectedRange == ReportRangeFilter.Today) {
                    "Evolucion del ingreso"
                } else {
                    "Tendencia acumulada"
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Desliza para ver como se fue acumulando el total.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (points.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "Aun no hay puntos suficientes para la tendencia.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            selectedPoint?.let { point ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = point.detailLabel,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Acumulado al cierre de este tramo",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", point.value)),
                            color = AppColors.PlinCyan,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .pointerInput(points) {
                        fun updateSelection(offset: Offset) {
                            if (points.isEmpty()) return
                            val slotWidth = size.width / points.size.toFloat()
                            val nextIndex = (offset.x / slotWidth)
                                .toInt()
                                .coerceIn(0, points.lastIndex)
                            if (nextIndex != selectedIndex) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            selectedIndex = nextIndex
                            tooltipAnchor = offset
                        }

                        detectTapGestures { offset ->
                            updateSelection(offset)
                        }
                    }
                    .pointerInput(points) {
                        fun updateSelection(offset: Offset) {
                            if (points.isEmpty()) return
                            val slotWidth = size.width / points.size.toFloat()
                            val nextIndex = (offset.x / slotWidth)
                                .toInt()
                                .coerceIn(0, points.lastIndex)
                            if (nextIndex != selectedIndex) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            selectedIndex = nextIndex
                            tooltipAnchor = offset
                        }

                        detectDragGestures(
                            onDragStart = { offset -> updateSelection(offset) },
                            onDrag = { change, _ ->
                                updateSelection(change.position)
                                change.consume()
                            }
                        )
                    }
            ) {
                val tooltipWidthPx = with(density) { 170.dp.toPx() }
                val tooltipHeightPx = with(density) { 72.dp.toPx() }
                val maxWidthPx = with(density) { maxWidth.toPx() }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val horizontalPadding = 18.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val baseY = size.height - 28.dp.toPx()
                    val chartWidth = size.width - (horizontalPadding * 2)
                    val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f

                    repeat(3) { index ->
                        val y = baseY - ((index + 1) * ((baseY - topPadding) / 4f))
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(horizontalPadding, y),
                            end = Offset(size.width - horizontalPadding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val pointOffsets = points.mapIndexed { index, point ->
                        val normalized = if (maxValue > 0f) point.value / maxValue else 0f
                        val x = horizontalPadding + (stepX * index)
                        val y = baseY - (normalized * (baseY - topPadding))
                        Offset(x, y)
                    }

                    val linePath = Path()
                    val fillPath = Path()

                    if (pointOffsets.isNotEmpty()) {
                        fillPath.moveTo(pointOffsets.first().x, baseY)
                        linePath.moveTo(pointOffsets.first().x, pointOffsets.first().y)
                        fillPath.lineTo(pointOffsets.first().x, pointOffsets.first().y)

                        if (pointOffsets.size > 1) {
                            for (i in 0 until pointOffsets.size - 1) {
                                val p1 = pointOffsets[i]
                                val p2 = pointOffsets[i + 1]
                                val conX1 = p1.x + (p2.x - p1.x) / 2f
                                linePath.cubicTo(
                                    conX1, p1.y,
                                    conX1, p2.y,
                                    p2.x, p2.y
                                )
                                fillPath.cubicTo(
                                    conX1, p1.y,
                                    conX1, p2.y,
                                    p2.x, p2.y
                                )
                            }
                        }

                        val last = pointOffsets.last()
                        fillPath.lineTo(last.x, baseY)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    YapeCyan.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                startY = topPadding,
                                endY = baseY
                            )
                        )
                        drawPath(
                            path = linePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    trendPrimaryColor,
                                    YapeCyan
                                )
                            ),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        pointOffsets.forEachIndexed { index, offset ->
                            if (index == selectedIndex) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.30f),
                                    start = Offset(offset.x, topPadding),
                                    end = Offset(offset.x, baseY),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                            drawCircle(
                                color = if (index == selectedIndex) Color.White else AppColors.PlinCyan,
                                radius = if (index == selectedIndex) 5.dp.toPx() else 3.5.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }

                val clampedTooltipX = ((tooltipAnchor?.x ?: 0f) - (tooltipWidthPx / 2f))
                    .coerceIn(0f, (maxWidthPx - tooltipWidthPx).coerceAtLeast(0f))
                val tooltipY = ((tooltipAnchor?.y ?: 0f) - tooltipHeightPx - with(density) { 8.dp.toPx() })
                    .coerceAtLeast(0f)

                selectedPoint?.let { point ->
                    tooltipAnchor?.let {
                        ReportFingerTooltip(
                            title = point.detailLabel,
                            value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", point.value)),
                            supporting = "Ingreso acumulado",
                            modifier = Modifier.offset {
                                IntOffset(clampedTooltipX.roundToInt(), tooltipY.roundToInt())
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { index, point ->
                    Text(
                        text = point.label.replace("/", "\n"),
                        color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportFingerTooltip(
    title: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF111615),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
            shadowElevation = AppElevation.md
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = supporting,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Box(
            modifier = Modifier
                .size(10.dp)
                .rotate(45f)
                .background(Color(0xFF111615))
        )
    }
}

@Composable
private fun ReportMetricCards(uiState: ReportsUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            ReportMetricHighlightCard(
                iconRes = R.drawable.ic_metric_money,
                title = "Ingresos Totales",
                value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", uiState.reportTotal))
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            ReportMetricHighlightCard(
                iconRes = R.drawable.ic_metric_receipt,
                title = "Total de pagos",
                value = uiState.reportCount.toString()
            )
        }
    }
}

@Composable
private fun ReportMetricHighlightCard(
    iconRes: Int,
    title: String,
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
        ))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp
                )
            }
        }
    }
}

@Composable
private fun ReportPrimaryExportButton(
    exportMode: ReportExportMode,
    onClick: () -> Unit
) {
    val iconRes = if (exportMode == ReportExportMode.Pdf) {
        R.drawable.ic_benefit_pdf
    } else {
        R.drawable.ic_benefit_whatsapp
    }
    val buttonLabel = if (exportMode == ReportExportMode.Pdf) {
        "Descargar Reporte PDF"
    } else {
        "Enviar por WhatsApp"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(YapePurple, YapeCyan)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = buttonLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ReportExportModeSelector(
    selectedMode: ReportExportMode,
    onSelect: (ReportExportMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(AppRadii.pill),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReportModeChip(
                    label = "PDF",
                    iconRes = R.drawable.ic_benefit_pdf,
                    selected = selectedMode == ReportExportMode.Pdf,
                    onClick = { onSelect(ReportExportMode.Pdf) }
                )
                ReportModeChip(
                    label = "WhatsApp",
                    iconRes = R.drawable.ic_benefit_whatsapp,
                    selected = selectedMode == ReportExportMode.WhatsApp,
                    onClick = { onSelect(ReportExportMode.WhatsApp) }
                )
            }
        }
    }
}

@Composable
private fun ReportModeChip(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(AppRadii.pill),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}


private data class ReportTimeBucket(
    val label: String,
    val total: Float,
    val count: Int,
    val shortLabel: String = label,
    val detailLabel: String = label
)



private data class ReportTrendPoint(
    val label: String,
    val value: Float,
    val detailLabel: String = label
)

private data class ReportTimeRange(
    val label: String,
    val detailLabel: String,
    val shortLabel: String,
    val startHour: Int,
    val endHourExclusive: Int
)

private fun ReportTimeBucket.axisLabel(): String =
    shortLabel.replace(" ", "\n")

private fun buildReportTimeBuckets(
    history: List<PaymentRecord>,
    filter: ReportRangeFilter
): List<ReportTimeBucket> {
    return if (filter == ReportRangeFilter.Today) {
        buildTodayTimeBuckets(history)
    } else {
        buildDailyBuckets(history)
    }
}

private fun buildTodayTimeBuckets(history: List<PaymentRecord>): List<ReportTimeBucket> {
    val ranges = listOf(
        ReportTimeRange(
            label = "Madrugada",
            detailLabel = "12 a.m. - 6 a.m.",
            shortLabel = "12-6a",
            startHour = 0,
            endHourExclusive = 6
        ),
        ReportTimeRange(
            label = "Temprano",
            detailLabel = "6 a.m. - 9 a.m.",
            shortLabel = "6-9a",
            startHour = 6,
            endHourExclusive = 9
        ),
        ReportTimeRange(
            label = "Mañana",
            detailLabel = "9 a.m. - 12 p.m.",
            shortLabel = "9-12p",
            startHour = 9,
            endHourExclusive = 12
        ),
        ReportTimeRange(
            label = "Mediodía",
            detailLabel = "12 p.m. - 3 p.m.",
            shortLabel = "12-3p",
            startHour = 12,
            endHourExclusive = 15
        ),
        ReportTimeRange(
            label = "Tarde",
            detailLabel = "3 p.m. - 7 p.m.",
            shortLabel = "3-7p",
            startHour = 15,
            endHourExclusive = 19
        ),
        ReportTimeRange(
            label = "Noche",
            detailLabel = "7 p.m. - 12 a.m.",
            shortLabel = "7-12a",
            startHour = 19,
            endHourExclusive = 24
        )
    )
    val totals = MutableList(ranges.size) { 0f }
    val counts = MutableList(ranges.size) { 0 }

    history.forEach { record ->
        val hour = Instant.ofEpochMilli(record.timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .hour
        val bucketIndex = ranges.indexOfFirst { range ->
            hour >= range.startHour && hour < range.endHourExclusive
        }
        if (bucketIndex == -1) return@forEach
        totals[bucketIndex] += record.amount.toFloat()
        counts[bucketIndex] += 1
    }

    return ranges.mapIndexed { index, range ->
        ReportTimeBucket(
            label = range.label,
            total = totals[index],
            count = counts[index],
            shortLabel = range.shortLabel,
            detailLabel = range.detailLabel
        )
    }
}

private fun buildDailyBuckets(history: List<PaymentRecord>): List<ReportTimeBucket> {
    val formatter = DateTimeFormatter.ofPattern("EEE-dd", Locale("es", "PE"))
    return history
        .groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        }
        .toSortedMap()
        .map { (date, records) ->
            ReportTimeBucket(
                label = date.format(formatter).replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale("es", "PE")) else char.toString()
                },
                total = records.sumOf { it.amount }.toFloat(),
                count = records.size,
                shortLabel = date.format(DateTimeFormatter.ofPattern("dd/MM", Locale("es", "PE"))),
                detailLabel = date.format(DateTimeFormatter.ofPattern("EEEE dd MMM", Locale("es", "PE")))
            )
        }
}



private fun buildCumulativeTrendPoints(buckets: List<ReportTimeBucket>): List<ReportTrendPoint> {
    var runningTotal = 0f
    return buckets.map { bucket ->
        runningTotal += bucket.total
        ReportTrendPoint(
            label = bucket.shortLabel,
            value = runningTotal,
            detailLabel = bucket.detailLabel
        )
    }
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

private fun Float.formatOneDecimal(): String =
    String.format(Locale.US, "%.1f", this)

private fun String.toFriendlyReportDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", Locale("es", "PE")))
    }.getOrElse { this }
}

private fun String.toSafeFileToken(): String =
    lowercase(Locale.ROOT)
        .replace("[^a-z0-9]+".toRegex(), "_")
        .trim('_')
        .ifBlank { "reporte" }

private fun uiStateFriendlyToday(timestamp: Long): Boolean {
    val now = LocalDate.now()
    val recordDate = Date(timestamp).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    return recordDate == now
}

fun sharePdfCustom(context: Context, date: String, total: Float, history: List<PaymentRecord>) {
    try {
        val fileName = "Reporte_Pagos_${date.toSafeFileToken()}.pdf"
        val cacheFile = File(context.cacheDir, fileName)
        generatePdfToFileCustom(cacheFile, date, total, history)

        val packageManager = context.packageManager
        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
        val totalText = String.format(Locale.US, "%.2f", total)
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.reports_share_text, date, totalText))
            clipData = ClipData.newUri(context.contentResolver, fileName, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val resolvedPackages = packageManager
            .queryIntentActivities(baseIntent, 0)
            .map { it.activityInfo.packageName }
            .distinct()

        resolvedPackages.forEach { packageName ->
            context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val preferredPackage = resolveWhatsAppPackage(context, baseIntent)
        if (preferredPackage != null) {
            context.grantUriPermission(preferredPackage, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val targetIntent = if (preferredPackage != null) {
            Intent(baseIntent).apply { `package` = preferredPackage }
        } else {
            baseIntent
        }

        if (preferredPackage != null) {
            context.startActivity(targetIntent)
        } else {
            context.startActivity(Intent.createChooser(targetIntent, context.getString(R.string.reports_share_chooser)))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.reports_share_error, e.message ?: ""), Toast.LENGTH_LONG).show()
    }
}

private fun resolveWhatsAppPackage(context: Context, baseIntent: Intent): String? {
    val packageManager = context.packageManager
    val candidates = listOf("com.whatsapp", "com.whatsapp.w4b")

    return candidates.firstOrNull { packageName ->
        Intent(baseIntent).apply { `package` = packageName }
            .resolveActivity(packageManager) != null
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


