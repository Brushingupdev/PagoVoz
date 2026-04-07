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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle

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
import androidx.compose.ui.text.style.TextOverflow
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.HablaPagoSearchField
import com.example.pagovoz.ui.components.hablaPagoPressable

import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

private enum class HistoryFilter {
    Today,
    Yesterday,
    Week,
    Month
}

private data class HistoryDateGroup(
    val date: LocalDate,
    val label: String,
    val records: List<PaymentRecord>
)

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
        val today = LocalDate.now()
        val source = when (selectedFilter) {
            HistoryFilter.Today -> todayHistory
            HistoryFilter.Yesterday -> yesterdayHistory
            HistoryFilter.Week -> multiDayHistory.filter { record ->
                val recordDate = record.toLocalDate()
                !recordDate.isBefore(today.minusDays(6)) && !recordDate.isAfter(today)
            }
            HistoryFilter.Month -> multiDayHistory.filter { record ->
                val recordDate = record.toLocalDate()
                !recordDate.isBefore(today.minusDays(29)) && !recordDate.isAfter(today)
            }
        }

        if (searchQuery.isBlank()) source
        else source.filter { record ->
            record.sender.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    val groupedHistory = remember(filteredHistory) {
        filteredHistory
            .groupBy { it.toLocalDate() }
            .toSortedMap(compareByDescending { it })
            .map { (date, records) ->
                HistoryDateGroup(
                    date = date,
                    label = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("es", "PE"))),
                    records = records
                )
            }
    }
    Scaffold(
        topBar = {
            AppSectionTopBar(
                title = stringResource(R.string.history_title_new),
                onBack = onBack
            )
        },
        containerColor = Color(0xFF090B10),
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.History) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> Unit
                    DashboardTab.Payments -> onShowPayments()
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
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HablaPagoSearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        label = stringResource(R.string.history_search_label),
                        placeholder = stringResource(R.string.history_search_placeholder)
                    )
                }

                item {
                    HistoryFiltersBar(
                        selectedFilter = selectedFilter,
                        onSelectToday = { selectedFilter = HistoryFilter.Today },
                        onSelectYesterday = { selectedFilter = HistoryFilter.Yesterday },
                        onSelectWeek = { selectedFilter = HistoryFilter.Week },
                        onSelectMonth = { selectedFilter = HistoryFilter.Month }
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
                            color = YapeCyan.copy(alpha = 0.86f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp
                        )
                    }

                    groupedHistory.forEach { group ->
                        item {
                            HistoryDateSection(
                                dateLabel = group.label,
                                records = group.records
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun HistoryFiltersBar(
    selectedFilter: HistoryFilter,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit,
    onSelectWeek: () -> Unit,
    onSelectMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HistoryFilterChip(
            label = "Hoy",
            selected = selectedFilter == HistoryFilter.Today,
            onClick = onSelectToday,
            modifier = Modifier.weight(1f)
        )
        HistoryFilterChip(
            label = "Ayer",
            selected = selectedFilter == HistoryFilter.Yesterday,
            onClick = onSelectYesterday,
            modifier = Modifier.weight(1f)
        )
        HistoryFilterChip(
            label = "Semana",
            selected = selectedFilter == HistoryFilter.Week,
            onClick = onSelectWeek,
            modifier = Modifier.weight(1f)
        )
        HistoryFilterChip(
            label = "Mes",
            selected = selectedFilter == HistoryFilter.Month,
            onClick = onSelectMonth,
            modifier = Modifier.weight(1f)
        )
    }
}



@Composable
private fun HistoryFilterChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val accentColor = if (label == "Hoy" || label == "Mes") YapePurple else YapeCyan
    val containerColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "history-filter-container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = containerColor,
        border = if (selected) BorderStroke(1.dp, accentColor.copy(alpha = 0.24f)) else null
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun HistoryEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = YapePurple.copy(alpha = 0.16f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_history),
                    contentDescription = null,
                    tint = YapePurple,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.history_empty_title),
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.history_empty_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryDateHeader(dateLabel: String) {
    Text(
        text = dateLabel,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
}

@Composable
private fun HistoryDateSection(
    dateLabel: String,
    records: List<PaymentRecord>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HistoryDateHeader(dateLabel = dateLabel)
        Column {
            records.forEachIndexed { index, record ->
                PaymentHistoryRow(record = record)
                if (index != records.lastIndex) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HistoryItemDivider()
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HablaPagoIconTile(
            iconRes = R.drawable.ic_metric_receipt,
            tint = YapePurple,
            containerColor = YapePurple.copy(alpha = 0.18f),
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = record.formattedTimestamp(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)),
                color = Color.White,
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
                    text = stringResource(R.string.history_status_success),
                    color = Color(0xFF1FA866),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
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
            .background(YapeCyan.copy(alpha = 0.12f))
    )
}

private fun PaymentRecord.toLocalDate(): LocalDate =
    Date(timestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

private fun PaymentRecord.formattedTimestamp(): String {
    val time = SimpleDateFormat("hh:mm a", Locale("es", "PE")).format(Date(timestamp))
    val dateText = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "PE")).format(toLocalDate())
    return "$time · $dateText"
}




