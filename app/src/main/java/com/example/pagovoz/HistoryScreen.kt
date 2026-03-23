package com.example.pagovoz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.HablaPagoSearchField
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private enum class HistoryFilter {
    Today,
    Yesterday,
    Week,
    Month
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onShowPayments: () -> Unit,
    onShowReports: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onShowPremium: () -> Unit,
    onShowProfile: () -> Unit,
    openedFromRecentActivity: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilter.Today) }
    var todayHistory by remember { mutableStateOf(SessionManager.getPaymentHistory(context).reversed()) }
    var yesterdayHistory by remember { mutableStateOf(SessionManager.getYesterdayHistory(context).reversed()) }
    var multiDayHistory by remember { mutableStateOf(SessionManager.getMultiDayHistory(context)) }
    val isPremium = SessionManager.isPremium(context)

    LaunchedEffect(Unit) {
        SessionManager.updates.collectLatest {
            todayHistory = SessionManager.getPaymentHistory(context).reversed()
            yesterdayHistory = SessionManager.getYesterdayHistory(context).reversed()
            multiDayHistory = SessionManager.getMultiDayHistory(context)
        }
    }

    LaunchedEffect(openedFromRecentActivity) {
        if (openedFromRecentActivity) {
            selectedFilter = HistoryFilter.Today
            searchQuery = ""
        }
    }

    BackHandler { onBack() }

    val filteredHistory = remember(todayHistory, yesterdayHistory, multiDayHistory, selectedFilter, searchQuery) {
        val source = when (selectedFilter) {
            HistoryFilter.Today -> todayHistory
            HistoryFilter.Yesterday -> yesterdayHistory
            HistoryFilter.Week, HistoryFilter.Month -> multiDayHistory
        }

        if (searchQuery.isBlank()) source
        else source.filter { record ->
            record.sender.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    val groupedHistory = remember(filteredHistory) {
        filteredHistory.groupBy { record ->
            record.toLocalDate().format(DateTimeFormatter.ofPattern("dd 'DE' MMMM", Locale("es", "PE")))
        }
    }
    val totalAmount = remember(filteredHistory) { filteredHistory.sumOf { it.amount } }
    val totalCount = filteredHistory.size

    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.history_title_new),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.History) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> Unit
                    DashboardTab.Payments -> onShowPayments()
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
            contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HistoryOverviewCard(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    total = totalAmount,
                    count = totalCount,
                    openedFromRecentActivity = openedFromRecentActivity
                )
            }

            item {
                HistoryFiltersBar(
                    selectedFilter = selectedFilter,
                    onSelectToday = { selectedFilter = HistoryFilter.Today },
                    onSelectYesterday = { selectedFilter = HistoryFilter.Yesterday }
                )
            }

            if (filteredHistory.isEmpty()) {
                item {
                    HistoryEmptyState()
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.history_recent_section),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                groupedHistory.forEach { (dateLabel, records) ->
                    item {
                        HistoryDateSection(
                            dateLabel = dateLabel,
                            records = records
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryOverviewCard(
    query: String,
    onQueryChange: (String) -> Unit,
    total: Double,
    count: Int,
    openedFromRecentActivity: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SearchHistoryField(
                query = query,
                onQueryChange = onQueryChange
            )

            if (openedFromRecentActivity) {
                Text(
                    text = stringResource(R.string.history_hero_subtitle_recent),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HistoryInlineMetric(
                        label = stringResource(R.string.history_summary_total_label),
                        value = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", total))
                    )
                }
                Box(modifier = Modifier.weight(0.8f)) {
                    HistoryInlineMetric(
                        label = stringResource(R.string.history_summary_count_label, count),
                        value = count.toString()
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFiltersBar(
    selectedFilter: HistoryFilter,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = AppElevation.sm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryFilterChip(
                label = stringResource(R.string.history_filter_today),
                selected = selectedFilter == HistoryFilter.Today,
                onClick = onSelectToday,
                modifier = Modifier.weight(1f)
            )
            HistoryFilterChip(
                label = stringResource(R.string.history_filter_yesterday),
                selected = selectedFilter == HistoryFilter.Yesterday,
                onClick = onSelectYesterday,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HistoryInlineMetric(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
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
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SearchHistoryField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    HablaPagoSearchField(
        query = query,
        onQueryChange = onQueryChange,
        label = stringResource(R.string.history_search_label),
        placeholder = stringResource(R.string.history_search_placeholder)
    )
}

@Composable
private fun HistoryFilterChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "history-filter-container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 180),
        label = "history-filter-content"
    )
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.985f,
        animationSpec = tween(durationMillis = 180),
        label = "history-filter-scale"
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = selectedScale
                scaleY = selectedScale
            }
            .hablaPagoPressable(interactionSource, pressedScale = 0.97f)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(AppRadii.lg),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.md)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_nav_history),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.history_empty_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.history_empty_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistoryDateHeader(dateLabel: String) {
    Text(
        text = dateLabel.uppercase(Locale("es", "PE")),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        letterSpacing = 0.7.sp
    )
}

@Composable
private fun HistoryDateSection(
    dateLabel: String,
    records: List<PaymentRecord>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.xl),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = AppElevation.sm
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.item),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HistoryDateHeader(dateLabel = dateLabel)
            Column {
                records.forEachIndexed { index, record ->
                    PaymentHistoryRow(record = record)
                    if (index != records.lastIndex) {
                        HistoryItemDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryRow(record: PaymentRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HablaPagoIconTile(
            iconRes = R.drawable.ic_nav_history,
            tint = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            size = AppIconSizes.tileSm,
            iconSize = AppIconSizes.md,
            shape = RoundedCornerShape(14.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AppSpacing.sm, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = record.sender,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.history_item_source),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = record.formattedTimestamp(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    )
}

private fun PaymentRecord.toLocalDate(): LocalDate =
    Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

private fun PaymentRecord.formattedTimestamp(): String {
    val format = SimpleDateFormat("HH:mm â€¢ dd MMM", Locale("es", "PE"))
    return format.format(Date(timestamp)).uppercase(Locale("es", "PE"))
}
