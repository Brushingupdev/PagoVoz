package com.example.pagovoz

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppRadii
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import java.util.Locale
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    return NotificationListenerHelper.isNotificationServiceEnabled(context)
}

fun isLikelyRestrictedSettingsBlocked(context: Context, hasNotificationPermission: Boolean): Boolean {
    if (hasNotificationPermission || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    return try {
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
        installer == null || installer != "com.android.vending"
    } catch (_: Exception) {
        false
    }
}

fun manufacturerHint(): String {
    return when (Build.MANUFACTURER.lowercase(Locale.ROOT)) {
        "xiaomi", "redmi", "poco" -> "Luego activa Inicio automatico y Bateria sin restricciones."
        "samsung" -> "Luego activa Uso de bateria sin restricciones para HablaPago."
        "oppo", "realme", "oneplus" -> "Luego activa Inicio automatico y Permitir actividad en segundo plano."
        "huawei", "honor" -> "Luego agrega HablaPago en Apps protegidas o Inicio de aplicaciones."
        "motorola" -> "Luego desactiva optimizacion de bateria para HablaPago."
        else -> "Luego revisa bateria e inicio automatico para que no se detenga el servicio."
    }
}

fun openBatteryReviewSettings(context: Context, alreadyIgnoringOptimizations: Boolean) {
    val packageUri = Uri.parse("package:${context.packageName}")
    val intents = buildList {
        if (!alreadyIgnoringOptimizations) {
            add(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri))
        }
        add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri))
        add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }

    val packageManager = context.packageManager
    val resolvedIntent = intents.firstOrNull { intent ->
        intent.resolveActivity(packageManager) != null
    }

    resolvedIntent?.let(context::startActivity)
}

@Composable
fun HomeScreen(
    onShowHistory: () -> Unit,
    onShowRecentHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowPremium: () -> Unit,
    onShowReports: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onShowProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isBatteryOptimizationOff by remember { mutableStateOf(isBatteryOptimizationDisabled(context)) }
    var isWidgetPinned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isWidgetPinned = GlanceAppWidgetManager(context).getGlanceIds(PagoGlanceWidget::class.java).isNotEmpty()
    }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            sessionRepository = defaultSessionRepository(context),
            licenseRepository = defaultLicenseRepository(context),
            isNotificationEnabled = { isNotificationServiceEnabled(context) }
        )
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
                isBatteryOptimizationOff = isBatteryOptimizationDisabled(context)
                scope.launch {
                    isWidgetPinned = GlanceAppWidgetManager(context).getGlanceIds(PagoGlanceWidget::class.java).isNotEmpty()
                }

            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (uiState.showTrialModal) {
        TrialPromoDialog(
            onConfirm = viewModel::dismissTrialModal,
            onDismiss = viewModel::dismissTrialModal
        )
    }

    if (uiState.showDeleteConfirm) {
        DeleteHistoryDialog(
            onConfirm = viewModel::confirmDeleteHistory,
            onDismiss = viewModel::dismissDeleteConfirm
        )
    }

    Scaffold(
        containerColor = Color(0xFF090B10),
        topBar = {
            HomeTopBar(onProfileClick = onShowProfile)
        },
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Home) { tab ->
                when (tab) {
                    DashboardTab.Home -> Unit
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> if (uiState.isPremium) onShowReports() else onShowPremium()
                    DashboardTab.Premium -> onShowPremium()
                }
            }
        }
    ) { padding ->
        val showRestrictedSettingsHint = isLikelyRestrictedSettingsBlocked(
            context = context,
            hasNotificationPermission = uiState.isPermissionEnabled
        )
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            item {
                HomeBalanceHeroCard(
                    total = uiState.dailyTotal,
                    yesterdayTotal = uiState.yesterdayTotal,
                    count = uiState.dailyCount,
                    isListeningEnabled = uiState.isPermissionEnabled,
                    onClick = onShowPayments
                )
            }

            item {
                HomeSetupAccessSection(
                    notificationEnabled = uiState.isPermissionEnabled,
                    restrictedSettingsReady = !showRestrictedSettingsHint,
                    batteryOptimizationDisabled = isBatteryOptimizationOff,
                    onOpenNotifications = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    },
                    onOpenRestrictedSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    onOpenBatterySettings = {
                        openBatteryReviewSettings(
                            context = context,
                            alreadyIgnoringOptimizations = isBatteryOptimizationOff
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                HomeRecentActivityCard(
                    title = stringResource(R.string.home_recent_activity_title),
                    payments = uiState.recentPayments,
                    onViewAll = onShowRecentHistory,
                    onPaymentClick = onShowRecentHistory
                )
            }

            item {
                HomeWidgetPinCard(
                    isPinned = isWidgetPinned,
                    onClick = { PagoGlanceWidget.requestPin(context) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                HomeActionStrip(
                    onShowPayments = onShowPayments,
                    onShowVoiceSettings = onShowVoiceSettings,
                    onShowReports = {
                        if (uiState.isPremium || uiState.trialDays > 0) onShowReports() else onShowPremium()
                    }
                )
            }

            item {
                HomeSecondaryActions(
                    onClearToday = viewModel::showDeleteConfirm
                )
            }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    LicenseFooter()
                }
            }
        }
    }
}

@Composable
private fun DeleteHistoryDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB81A1830).copy(alpha = 0.56f))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.xl)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.a11y_delete_history),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = stringResource(R.string.delete_dialog_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.delete_dialog_body),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.delete_dialog_cancel),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD52D2D))
                        ) {
                            Text(
                                text = stringResource(R.string.delete_dialog_confirm),
                                color = MaterialTheme.colorScheme.surface,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrialPromoDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val yapeAccent = YapePurple
    val plinAccent = Color(0xFF34B7D7)
    val modalBorder = Color(0xFFD9D5E5)
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "trial_overlay_alpha"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 320),
        label = "trial_content_alpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = tween(durationMillis = 360),
        label = "trial_content_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB81A1830).copy(alpha = 0.72f * overlayAlpha))
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentAlpha
                        scaleX = contentScale
                        scaleY = contentScale
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, modalBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.xl)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF4EEFA),
                                    Color(0xFFF0F7FB)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(yapeAccent.copy(alpha = 0.12f))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(plinAccent.copy(alpha = 0.14f))
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.a11y_close_dialog),
                                    tint = Color(0xFF8F98A4),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.72f))
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clickable(onClick = onDismiss)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TrialBrandBadge(
                                    logoRes = R.drawable.yape,
                                    contentDescription = "Yape",
                                    size = 44.dp
                                )
                                TrialBrandBadge(
                                    logoRes = R.drawable.plin,
                                    contentDescription = "Plin",
                                    size = 42.dp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(yapeAccent, plinAccent)
                                            ),
                                            shape = RoundedCornerShape(999.dp)
                                        )
                                ) {
                                    Text(
                                        text = stringResource(R.string.trial_modal_badge),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.9.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = stringResource(R.string.trial_modal_title_new),
                                color = Color(0xFF201B2A),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                lineHeight = 25.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TrialBenefitItem(
                        title = stringResource(R.string.trial_modal_benefit_title_1),
                        subtitle = stringResource(R.string.trial_modal_benefit_subtitle_1),
                        accent = Color(0xFF30D46C),
                        container = Color(0xFF30D46C).copy(alpha = 0.16f)
                    )
                    TrialBenefitItem(
                        title = stringResource(R.string.trial_modal_benefit_title_2),
                        subtitle = stringResource(R.string.trial_modal_benefit_subtitle_2),
                        accent = Color(0xFF30D46C),
                        container = Color(0xFF30D46C).copy(alpha = 0.16f)
                    )
                    TrialBenefitItem(
                        title = stringResource(R.string.trial_modal_benefit_title_3),
                        subtitle = stringResource(R.string.trial_modal_benefit_subtitle_3),
                        accent = Color(0xFF30D46C),
                        container = Color(0xFF30D46C).copy(alpha = 0.16f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = AppElevation.md)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(yapeAccent, plinAccent)
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.trial_modal_confirm_new),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.trial_modal_legal),
                        color = Color(0xFF7A7485),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrialBrandBadge(
    logoRes: Int,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp
) {
    Image(
        painter = painterResource(logoRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
    )
}

@Composable
private fun TrialBenefitItem(
    title: String,
    subtitle: String,
    accent: Color,
    container: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.padding(top = 1.dp),
            shape = CircleShape,
            color = container
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = title,
                color = Color(0xFF201B2A),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = subtitle,
                color = Color(0xFF6E6A78),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp
            )
        }
    }
}


