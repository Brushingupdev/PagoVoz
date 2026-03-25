package com.example.pagovoz

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class PendingAppUpdate(
    val downloadId: Long,
    val destinationFile: File
)

sealed interface AppUpdateDownloadResult {
    data class Success(val apkFile: File) : AppUpdateDownloadResult
    data class Failed(val reason: String) : AppUpdateDownloadResult
}

object AppUpdateInstaller {
    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

    fun canRequestPackageInstalls(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun enqueueDownload(
        context: Context,
        downloadUrl: String,
        versionName: String
    ): PendingAppUpdate? {
        val downloadManager = context.getSystemService(DownloadManager::class.java) ?: return null
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val fileName = "HablaPago-${sanitizeVersionName(versionName)}.apk"
        val destinationFile = File(downloadsDir, fileName)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("Actualizacion de HablaPago")
            setDescription("Descargando version $versionName")
            setMimeType(APK_MIME_TYPE)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        }

        val downloadId = downloadManager.enqueue(request)
        return PendingAppUpdate(downloadId = downloadId, destinationFile = destinationFile)
    }

    suspend fun awaitDownload(
        context: Context,
        pendingUpdate: PendingAppUpdate,
        onProgress: (Int?) -> Unit
    ): AppUpdateDownloadResult = withContext(Dispatchers.IO) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
            ?: return@withContext AppUpdateDownloadResult.Failed("No se pudo iniciar la descarga.")

        val query = DownloadManager.Query().setFilterById(pendingUpdate.downloadId)

        while (true) {
            val cursor = downloadManager.query(query)
            cursor.use {
                if (!it.moveToFirst()) {
                    return@withContext AppUpdateDownloadResult.Failed("La descarga ya no esta disponible.")
                }

                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val totalBytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val downloadedBytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                val progress = if (totalBytes > 0) {
                    ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                } else {
                    null
                }
                onProgress(progress)

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        return@withContext if (pendingUpdate.destinationFile.exists()) {
                            AppUpdateDownloadResult.Success(pendingUpdate.destinationFile)
                        } else {
                            AppUpdateDownloadResult.Failed("Se completo la descarga, pero no se encontro el APK.")
                        }
                    }

                    DownloadManager.STATUS_FAILED -> {
                        val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        return@withContext AppUpdateDownloadResult.Failed(mapFailureReason(reason))
                    }
                }
            }

            delay(500)
        }

        @Suppress("UNREACHABLE_CODE")
        AppUpdateDownloadResult.Failed("No se pudo completar la descarga.")
    }

    fun launchInstaller(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val packageManager = context.packageManager
        if (installIntent.resolveActivity(packageManager) == null) {
            return false
        }

        context.startActivity(installIntent)
        return true
    }

    private fun sanitizeVersionName(versionName: String): String {
        return versionName
            .trim()
            .ifBlank { "update" }
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    private fun mapFailureReason(reason: Int): String {
        return when (reason) {
            DownloadManager.ERROR_CANNOT_RESUME -> "No se pudo reanudar la descarga."
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "No se encontro almacenamiento disponible."
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "El archivo de actualizacion ya existe."
            DownloadManager.ERROR_FILE_ERROR -> "Hubo un problema al guardar el APK."
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "La descarga se interrumpio por un error de red."
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "No hay espacio suficiente para descargar la actualizacion."
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "La URL de descarga tiene demasiadas redirecciones."
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "El servidor devolvio un error al descargar la actualizacion."
            DownloadManager.ERROR_UNKNOWN -> "No se pudo descargar la actualizacion."
            else -> "No se pudo descargar la actualizacion."
        }
    }
}
