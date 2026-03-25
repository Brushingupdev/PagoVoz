package com.example.pagovoz

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: AppNavigationViewModel = viewModel(
        factory = AppNavigationViewModelFactory(
            sessionRepository = defaultSessionRepository(context),
            licenseRepository = defaultLicenseRepository(context)
        )
    )
    val updateViewModel: UpdateViewModel = viewModel(
        factory = UpdateViewModelFactory(
            appContext = context,
            updateRepository = defaultUpdateRepository()
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.start()
        updateViewModel.checkForUpdates()
        SupabaseManager.reportDeviceVersionInBackground(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updateViewModel.checkForUpdates()
                SupabaseManager.reportDeviceVersionInBackground(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = uiState.currentScreen != "home") {
        viewModel.openHome()
    }

    if (updateUiState.showForced) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Actualizacion obligatoria") },
            text = {
                Text(
                    text = buildUpdateDialogMessage(
                        versionName = updateUiState.latestVersionName,
                        isForced = true,
                        statusMessage = updateUiState.statusMessage
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = updateViewModel::startUpdate,
                    enabled = !updateUiState.isDownloading
                ) {
                    Text(updateActionLabel(updateUiState))
                }
            }
        )
    } else if (updateUiState.showOptional) {
        AlertDialog(
            onDismissRequest = {
                if (!updateUiState.isDownloading) {
                    updateViewModel.dismissOptional()
                }
            },
            title = { Text(text = "Actualizacion disponible") },
            text = {
                Text(
                    text = buildUpdateDialogMessage(
                        versionName = updateUiState.latestVersionName,
                        isForced = false,
                        statusMessage = updateUiState.statusMessage
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = updateViewModel::startUpdate,
                    enabled = !updateUiState.isDownloading
                ) {
                    Text(updateActionLabel(updateUiState))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = updateViewModel::dismissOptional,
                    enabled = !updateUiState.isDownloading
                ) {
                    Text("Luego")
                }
            }
        )
    }

    when (uiState.currentScreen) {
        "premium" -> {
            if (uiState.isPremium) {
                PremiumStatusScreen(
                    onBack = viewModel::openHome,
                    onShowReports = viewModel::openReports,
                    onShowHistory = viewModel::openHistory,
                    onShowPayments = viewModel::openPayments,
                    onShowVoiceSettings = viewModel::openVoiceSettings,
                    onShowProfile = viewModel::openProfile
                )
            } else {
                PremiumInfoScreen(onBack = viewModel::openHome)
            }
        }

        "voice_settings" -> VoiceSettingsScreen(
            onBack = viewModel::openHome,
            onShowHistory = viewModel::openHistory,
            onShowPayments = viewModel::openPayments,
            onShowReports = viewModel::openReports,
            onShowProfile = viewModel::openProfile
        )

        "payments" -> PaymentsScreen(
            onBack = viewModel::openHome,
            onShowHistory = viewModel::openHistory,
            onShowReports = viewModel::openReports,
            onShowPremium = viewModel::openPremium,
            onShowProfile = viewModel::openProfile
        )

        "history" -> HistoryScreen(
            onBack = viewModel::openHome,
            onShowPayments = viewModel::openPayments,
            onShowReports = viewModel::openReports,
            onShowVoiceSettings = viewModel::openVoiceSettings,
            onShowPremium = viewModel::openPremium,
            onShowProfile = viewModel::openProfile,
            openedFromRecentActivity = uiState.historyOpenedFromRecent
        )

        "reports" -> ReportGeneratorScreen(
            onBack = viewModel::openHome,
            onShowHistory = viewModel::openHistory,
            onShowPayments = viewModel::openPayments,
            onShowVoiceSettings = viewModel::openVoiceSettings,
            onShowPremium = viewModel::openPremium,
            onShowProfile = viewModel::openProfile
        )

        "profile" -> ProfileScreen(
            onBack = viewModel::openHome,
            onShowHistory = viewModel::openHistory,
            onShowPayments = viewModel::openPayments,
            onShowReports = viewModel::openReports,
            onShowPremium = viewModel::openPremium,
            onShowVoiceSettings = viewModel::openVoiceSettings
        )

        else -> HomeScreen(
            onShowHistory = viewModel::openHistory,
            onShowRecentHistory = viewModel::openHistoryFromRecent,
            onShowPayments = viewModel::openPayments,
            onShowPremium = viewModel::openPremium,
            onShowReports = viewModel::openReports,
            onShowVoiceSettings = viewModel::openVoiceSettings,
            onShowProfile = viewModel::openProfile
        )
    }
}

private fun buildUpdateDialogMessage(
    versionName: String,
    isForced: Boolean,
    statusMessage: String?
): String {
    val baseMessage = if (isForced) {
        "Hay una version nueva ($versionName). Debes actualizar para continuar."
    } else {
        "Hay una version nueva ($versionName). Quieres actualizar ahora?"
    }

    return statusMessage?.takeIf { it.isNotBlank() }?.let { "$baseMessage\n\n$it" } ?: baseMessage
}

private fun updateActionLabel(uiState: UpdateUiState): String {
    if (!uiState.isDownloading) return "Actualizar"
    return uiState.downloadProgress?.let { "Descargando $it%" } ?: "Descargando..."
}
