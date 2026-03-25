package com.example.pagovoz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateUiState(
    val showOptional: Boolean = false,
    val showForced: Boolean = false,
    val latestVersionName: String = "",
    val downloadUrl: String = "",
    val isDownloading: Boolean = false,
    val downloadProgress: Int? = null,
    val statusMessage: String? = null
)

class UpdateViewModel(
    private val appContext: Context,
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState = _uiState.asStateFlow()

    private val prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val checkIntervalMs = if (BuildConfig.DEBUG) 5 * 60 * 1000L else 12 * 60 * 60 * 1000L
    private val forcedCacheTtlMs = 24 * 60 * 60 * 1000L

    fun checkForUpdates() {
        val now = System.currentTimeMillis()
        val lastCheck = prefs.getLong(KEY_LAST_UPDATE_CHECK, 0L)
        if ((now - lastCheck) < checkIntervalMs) return

        viewModelScope.launch {
            val remote = updateRepository.fetchAppConfig()
            if (remote == null) {
                applyForcedFromCacheIfRecent(now)
                return@launch
            }

            val needsUpdate = remote.latest_version_code > BuildConfig.VERSION_CODE
            val showForced = needsUpdate && remote.force_update
            val showOptional = needsUpdate && !remote.force_update

            _uiState.update {
                it.copy(
                    showForced = showForced,
                    showOptional = showOptional,
                    latestVersionName = remote.latest_version_name,
                    downloadUrl = remote.download_url,
                    statusMessage = null
                )
            }

            prefs.edit()
                .putLong(KEY_LAST_UPDATE_CHECK, now)
                .putBoolean(KEY_CACHED_FORCE_UPDATE, showForced)
                .putString(KEY_CACHED_DOWNLOAD_URL, remote.download_url)
                .putString(KEY_CACHED_VERSION_NAME, remote.latest_version_name)
                .putLong(KEY_CACHED_FORCE_UPDATE_AT, now)
                .apply()
        }
    }

    private fun applyForcedFromCacheIfRecent(now: Long) {
        val cachedForced = prefs.getBoolean(KEY_CACHED_FORCE_UPDATE, false)
        val cachedAt = prefs.getLong(KEY_CACHED_FORCE_UPDATE_AT, 0L)
        if (!cachedForced || (now - cachedAt) > forcedCacheTtlMs) return

        _uiState.update {
            it.copy(
                showForced = true,
                showOptional = false,
                latestVersionName = prefs.getString(KEY_CACHED_VERSION_NAME, "") ?: "",
                downloadUrl = prefs.getString(KEY_CACHED_DOWNLOAD_URL, "") ?: "",
                statusMessage = null
            )
        }
    }

    fun startUpdate() {
        val currentState = _uiState.value
        if (currentState.isDownloading || currentState.downloadUrl.isBlank()) return

        if (!AppUpdateInstaller.canRequestPackageInstalls(appContext)) {
            _uiState.update {
                it.copy(
                    statusMessage = "Activa 'Instalar apps desconocidas' y vuelve a tocar Actualizar."
                )
            }
            AppUpdateInstaller.openInstallPermissionSettings(appContext)
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    statusMessage = "Preparando descarga..."
                )
            }

            val pendingUpdate = AppUpdateInstaller.enqueueDownload(
                context = appContext,
                downloadUrl = currentState.downloadUrl,
                versionName = currentState.latestVersionName
            )

            if (pendingUpdate == null) {
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = null,
                        statusMessage = "No se pudo iniciar la descarga."
                    )
                }
                return@launch
            }

            when (
                val result = AppUpdateInstaller.awaitDownload(appContext, pendingUpdate) { progress ->
                    _uiState.update {
                        it.copy(
                            downloadProgress = progress,
                            statusMessage = progress?.let { percent ->
                                "Descargando actualizacion... $percent%"
                            } ?: "Descargando actualizacion..."
                        )
                    }
                }
            ) {
                is AppUpdateDownloadResult.Success -> {
                    val installerOpened = AppUpdateInstaller.launchInstaller(appContext, result.apkFile)
                    _uiState.update {
                        it.copy(
                            isDownloading = false,
                            downloadProgress = if (installerOpened) 100 else null,
                            statusMessage = if (installerOpened) {
                                "Instalador listo. Confirma la actualizacion para completar el proceso."
                            } else {
                                "Se descargo el APK, pero no se pudo abrir el instalador."
                            }
                        )
                    }
                }

                is AppUpdateDownloadResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            isDownloading = false,
                            downloadProgress = null,
                            statusMessage = result.reason
                        )
                    }
                }
            }
        }
    }

    fun dismissOptional() {
        if (_uiState.value.isDownloading) return
        _uiState.update {
            it.copy(
                showOptional = false,
                statusMessage = null
            )
        }
    }

    companion object {
        private const val KEY_LAST_UPDATE_CHECK = "last_update_check"
        private const val KEY_CACHED_FORCE_UPDATE = "cached_force_update"
        private const val KEY_CACHED_FORCE_UPDATE_AT = "cached_force_update_at"
        private const val KEY_CACHED_DOWNLOAD_URL = "cached_download_url"
        private const val KEY_CACHED_VERSION_NAME = "cached_version_name"
    }
}
