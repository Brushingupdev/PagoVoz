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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.pagovoz.ui.theme.YapePurple
import java.util.Locale

fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(pkgName)
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
    var isBatteryOptimizationOff by remember { mutableStateOf(isBatteryOptimizationDisabled(context)) }
    var batteryWarningDismissed by remember { mutableStateOf(SessionManager.isBatteryWarningDismissed(context)) }
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
                batteryWarningDismissed = SessionManager.isBatteryWarningDismissed(context)
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeTopBar()
        },
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Home) { tab ->
                when (tab) {
                    DashboardTab.Home -> Unit
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> if (uiState.isPremium) onShowReports() else onShowPremium()
                    DashboardTab.Profile -> onShowProfile()
                }
            }
        }
    ) { padding ->
        val showRestrictedSettingsHint = isLikelyRestrictedSettingsBlocked(
            context = context,
            hasNotificationPermission = uiState.isPermissionEnabled
        )
        val showBatteryWarning = !isBatteryOptimizationOff && !batteryWarningDismissed
        val hasProAccess = uiState.isPremium || uiState.trialDays > 0

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeBalanceHeroCard(
                total = uiState.dailyTotal,
                count = uiState.dailyCount,
                isListeningEnabled = uiState.isPermissionEnabled
            )

            HomeListenerStatusCard(
                isListeningEnabled = uiState.isPermissionEnabled,
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )

            if (showRestrictedSettingsHint) {
                WarningCard(
                    title = stringResource(R.string.restricted_settings_title),
                    body = stringResource(R.string.restricted_settings_body),
                    actionLabel = stringResource(R.string.restricted_settings_open_app_info),
                    accentColor = Color(0xFFE1802F),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
            } else if (showBatteryWarning) {
                WarningCard(
                    title = stringResource(R.string.battery_optimization_title),
                    body = stringResource(R.string.battery_warning_body),
                    actionLabel = stringResource(R.string.battery_warning_open_settings),
                    accentColor = Color(0xFFE1802F),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    }
                )
            }

            DashboardSectionHeader(
                title = stringResource(R.string.home_quick_actions_title)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeQuickAction(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.home_quick_action_collect),
                        iconRes = R.drawable.ic_nav_payments,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onShowPayments
                    )
                    HomeQuickAction(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.home_quick_action_history),
                        iconRes = R.drawable.ic_nav_history,
                        iconTint = Color(0xFF2A79C8),
                        iconBackground = Color(0xFFEAF4FF),
                        onClick = onShowHistory
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeQuickAction(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.home_quick_action_reports),
                        iconRes = R.drawable.ic_nav_reports,
                        iconTint = AppColors.BrandPrimaryStrong,
                        iconBackground = AppColors.SurfaceBrand,
                        onClick = {
                            if (uiState.isPremium) onShowReports() else onShowPremium()
                        }
                    )
                    HomeQuickAction(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.home_quick_action_voice),
                        iconRes = R.drawable.ic_voice_pro,
                        iconTint = AppColors.BrandAccent,
                        iconBackground = Color(0xFFE8FCF7),
                        onClick = {
                            if (uiState.isPremium) onShowVoiceSettings() else onShowPremium()
                        }
                    )
                }
            }

            HomeRecentActivityCard(
                payments = uiState.recentPayments,
                onViewAll = onShowRecentHistory
            )

            DashboardSectionHeader(
                title = stringResource(R.string.home_features_title)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadii.xl),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = AppElevation.sm
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    HomeFeatureCard(
                        title = stringResource(R.string.home_feature_listening_title),
                        body = stringResource(R.string.home_feature_listening_body),
                        badgeText = if (uiState.isPermissionEnabled) {
                            stringResource(R.string.home_feature_badge_ready)
                        } else {
                            stringResource(R.string.home_feature_badge_pending)
                        },
                        iconRes = R.drawable.ic_nav_payments,
                        iconTint = MaterialTheme.colorScheme.primary,
                        iconBackground = MaterialTheme.colorScheme.primaryContainer,
                        onClick = {
                            if (uiState.isPermissionEnabled) {
                                onShowPayments()
                            } else {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                    HomeSectionDivider()

                    HomeFeatureCard(
                        title = stringResource(R.string.home_feature_history_title),
                        body = stringResource(R.string.home_feature_history_body),
                        badgeText = stringResource(R.string.home_feature_badge_ready),
                        iconRes = R.drawable.ic_nav_history,
                        iconTint = Color(0xFF2A79C8),
                        iconBackground = Color(0xFFEAF4FF),
                        onClick = onShowHistory
                    )
                    HomeSectionDivider()

                    HomeFeatureCard(
                        title = stringResource(R.string.home_feature_reports_title),
                        body = stringResource(R.string.home_feature_reports_body),
                        badgeText = if (hasProAccess) {
                            stringResource(R.string.home_feature_badge_ready)
                        } else {
                            stringResource(R.string.home_feature_badge_pro)
                        },
                        iconRes = R.drawable.ic_nav_reports,
                        iconTint = AppColors.BrandPrimaryStrong,
                        iconBackground = AppColors.SurfaceBrand,
                        onClick = {
                            if (hasProAccess) onShowReports() else onShowPremium()
                        }
                    )
                    HomeSectionDivider()

                    HomeFeatureCard(
                        title = stringResource(R.string.home_feature_voice_title),
                        body = stringResource(R.string.home_feature_voice_body),
                        badgeText = if (hasProAccess) {
                            stringResource(R.string.home_feature_badge_ready)
                        } else {
                            stringResource(R.string.home_feature_badge_pro)
                        },
                        iconRes = R.drawable.ic_voice_pro,
                        iconTint = AppColors.BrandAccent,
                        iconBackground = Color(0xFFE8FCF7),
                        onClick = {
                            if (hasProAccess) onShowVoiceSettings() else onShowPremium()
                        }
                    )
                    HomeSectionDivider()

                    HomeFeatureCard(
                        title = stringResource(R.string.home_feature_profile_title),
                        body = stringResource(R.string.home_feature_profile_body),
                        badgeText = stringResource(R.string.home_feature_badge_account),
                        iconRes = R.drawable.ic_nav_profile,
                        iconTint = Color(0xFF576273),
                        iconBackground = Color(0xFFF2F4F8),
                        onClick = onShowProfile
                    )
                }
            }

            HomeSecondaryActions(
                onClearToday = viewModel::showDeleteConfirm
            )

            if (!isBatteryOptimizationOff && batteryWarningDismissed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            SessionManager.setBatteryWarningDismissed(context, false)
                            batteryWarningDismissed = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.battery_warning_show_again),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LicenseFooter()
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F3FA)),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.xl)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFF8F1FC),
                                        Color(0xFFFFFFFF)
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0x14A855F7))
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x1400D6C2))
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
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier
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
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = stringResource(R.string.trial_modal_badge),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.9.sp,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = stringResource(R.string.trial_modal_title_new),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                lineHeight = 25.sp
                            )

                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TrialBenefitItem(
                        title = stringResource(R.string.trial_modal_benefit_title_1),
                        subtitle = stringResource(R.string.trial_modal_benefit_subtitle_1)
                    )
                    TrialBenefitItem(
                        title = stringResource(R.string.trial_modal_benefit_title_2),
                        subtitle = stringResource(R.string.trial_modal_benefit_subtitle_2)
                    )
                    TrialBenefitItem(
                        title = stringResource(R.string.trial_modal_benefit_title_3),
                        subtitle = stringResource(R.string.trial_modal_benefit_subtitle_3)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = AppElevation.md)
                    ) {
                        Text(
                            text = stringResource(R.string.trial_modal_confirm_new),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.trial_modal_legal),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
    subtitle: String
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
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp
            )
        }
    }
}


