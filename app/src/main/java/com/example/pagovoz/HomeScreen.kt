package com.example.pagovoz

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import java.util.Locale

fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
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
        "samsung" -> "Luego activa Uso de bateria sin restricciones para PagoVoz."
        "oppo", "realme", "oneplus" -> "Luego activa Inicio automatico y Permitir actividad en segundo plano."
        "huawei", "honor" -> "Luego agrega PagoVoz en Apps protegidas o Inicio de aplicaciones."
        "motorola" -> "Luego desactiva optimizacion de bateria para PagoVoz."
        else -> "Luego revisa bateria e inicio automatico para que no se detenga el servicio."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onShowHistory: () -> Unit,
    onShowPremium: () -> Unit,
    onShowReports: () -> Unit
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
        AlertDialog(
            onDismissRequest = viewModel::dismissTrialModal,
            icon = {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    shadowElevation = 6.dp,
                    border = BorderStroke(2.dp, YapeCyan)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mi_logo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            title = {
                Text(
                    stringResource(R.string.trial_modal_title),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 22.sp,
                    color = YapePurple
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.trial_modal_body_1),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.trial_modal_body_2),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = YapePurple,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::dismissTrialModal,
                    colors = ButtonDefaults.buttonColors(containerColor = YapePurple),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.trial_modal_confirm), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_body)) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmDeleteHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete_dialog_confirm), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) {
                    Text(stringResource(R.string.delete_dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.dashboard_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = YapePurple)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val showRestrictedSettingsHint = isLikelyRestrictedSettingsBlocked(
                context = context,
                hasNotificationPermission = uiState.isPermissionEnabled
            )

            SummaryCard(total = uiState.dailyTotal, count = uiState.dailyCount)

            StatusCard(
                isEnabled = uiState.isPermissionEnabled,
                onConfigClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )

            if (showRestrictedSettingsHint) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, Color(0xFF43A047))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.restricted_settings_title),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = stringResource(R.string.restricted_settings_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = manufacturerHint(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                }
                            ) {
                                Text(
                                    stringResource(R.string.restricted_settings_open_app_info),
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                }
                            ) {
                                Text(
                                    stringResource(R.string.restricted_settings_open_notification_access),
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            if (!isBatteryOptimizationOff && !batteryWarningDismissed) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.battery_warning_title),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    stringResource(R.string.battery_warning_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            ) {
                                Text(
                                    stringResource(R.string.battery_warning_open_settings),
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TextButton(
                                onClick = {
                                    SessionManager.setBatteryWarningDismissed(context, true)
                                    batteryWarningDismissed = true
                                }
                            ) {
                                Text(
                                    stringResource(R.string.battery_warning_dismiss),
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.control_tools_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ToolButton(
                title = stringResource(R.string.tool_history_title),
                subtitle = stringResource(R.string.tool_history_subtitle),
                icon = Icons.Default.DateRange,
                onClick = onShowHistory
            )

            ToolButton(
                title = stringResource(R.string.tool_reports_title),
                subtitle = stringResource(R.string.tool_reports_subtitle),
                icon = Icons.Default.Info,
                onClick = {
                    if (uiState.isPremium) onShowReports() else onShowPremium()
                }
            )

            ToolButton(
                title = stringResource(R.string.tool_clear_title),
                subtitle = stringResource(R.string.tool_clear_subtitle),
                icon = Icons.Default.Settings,
                onClick = viewModel::showDeleteConfirm
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.trialDays > 0) {
                Card(
                    onClick = onShowPremium,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2FAFF)),
                    border = BorderStroke(1.dp, YapeCyan.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = YapePurple.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = YapePurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.premium_trial_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = YapePurple
                            )
                            Text(
                                text = stringResource(R.string.premium_days_left, uiState.trialDays),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF22303A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = YapeCyan.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = stringResource(R.string.premium_badge_label),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = YapePurple
                            )
                        }
                    }
                }
            }

            LicenseFooter(onPremiumClick = onShowPremium)
        }
    }
}
