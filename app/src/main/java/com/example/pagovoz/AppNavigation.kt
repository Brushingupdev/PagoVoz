package com.example.pagovoz

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                updateViewModel.checkForUpdates()
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
            title = { Text(text = "Actualización obligatoria") },
            text = { Text(text = "Hay una versión nueva (${updateUiState.latestVersionName}). Debes actualizar para continuar.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, updateUiState.downloadUrl.toUri())
                        context.startActivity(intent)
                    }
                ) {
                    Text("Actualizar")
                }
            }
        )
    } else if (updateUiState.showOptional) {
        AlertDialog(
            onDismissRequest = { updateViewModel.dismissOptional() },
            title = { Text(text = "Actualización disponible") },
            text = { Text(text = "Hay una versión nueva (${updateUiState.latestVersionName}). ¿Deseas actualizar ahora?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, updateUiState.downloadUrl.toUri())
                        context.startActivity(intent)
                        updateViewModel.dismissOptional()
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = updateViewModel::dismissOptional) {
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
