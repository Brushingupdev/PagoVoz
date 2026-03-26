package com.example.pagovoz

import android.content.Context
import android.provider.Settings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

@Serializable
data class License(
    val id: String? = null,
    val code: String,
    val active: Boolean,
    val used: Boolean = false,
    val device_id: String? = null,
    val user_id: String? = null,
    val is_premium: Boolean = false,
    val premium_until: String? = null,
    val gives_trial: Boolean = false
)

@Serializable
data class AppConfig(
    val latest_version_code: Int,
    val latest_version_name: String,
    val download_url: String,
    val force_update: Boolean
)

@Serializable
data class ActivateLicenseParams(
    val p_code: String,
    val p_device_id: String
)

@Serializable
data class PremiumStatusParams(
    val p_device_id: String
)

@Serializable
data class PremiumStatusResponse(
    val is_premium: Boolean = false,
    val premium_until: String? = null
)

object SupabaseManager {

    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_KEY = BuildConfig.SUPABASE_KEY
    private const val DEBUG_CHECK_INTERVAL_MS = 30_000L
    private const val RELEASE_CHECK_INTERVAL_MS = 5 * 60 * 1000L
    private const val APP_PREFS_NAME = "app_prefs"
    private const val VERSION_REPORT_INTERVAL_MS = 12 * 60 * 60 * 1000L
    private const val KEY_LAST_VERSION_REPORT_AT = "last_device_version_report_at"
    private const val KEY_LAST_REPORTED_VERSION_CODE = "last_reported_version_code"

    // Scope único vinculado al proceso — evita coroutines huérfanas
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val deviceVersionReportMutex = Mutex()

    private val client by lazy {
        runCatching {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Postgrest)
                install(Realtime)
            }
        }.getOrNull()
    }

    fun listenForPremiumChanges(context: Context) {
        applicationScope.launch {
            checkPremiumStatus(context, force = true)
        }
    }

    suspend fun checkPremiumStatus(context: Context, force: Boolean = false): Boolean {
        val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_premium_check", 0L)
        val minInterval = if (BuildConfig.DEBUG) DEBUG_CHECK_INTERVAL_MS else RELEASE_CHECK_INTERVAL_MS

        if (!force && (System.currentTimeMillis() - lastCheck) < minInterval) {
            return SessionManager.isPremium(context)
        }

        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val supabaseClient = client ?: return SessionManager.isPremium(context)

        return try {
            val response = supabaseClient.postgrest.rpc(
                function = "get_premium_status",
                parameters = PremiumStatusParams(p_device_id = androidId)
            )
            val premiumStatus = response.decodeSingleOrNull<PremiumStatusResponse>()
            val isPremiumNow = premiumStatus?.is_premium ?: false

            SessionManager.setPremium(context, isPremiumNow, premiumStatus?.premium_until)
            prefs.edit().putLong("last_premium_check", System.currentTimeMillis()).apply()
            isPremiumNow
        } catch (e: Exception) {
            e.printStackTrace()
            SessionManager.isPremium(context)
        }
    }

    suspend fun validarCodigo(context: Context, inputCode: String): Boolean {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val supabaseClient = client ?: return false

        return try {
            val result = supabaseClient.postgrest.rpc(
                function = "activate_license",
                parameters = ActivateLicenseParams(
                    p_code = inputCode,
                    p_device_id = androidId
                )
            ).decodeAs<Boolean>()

            if (!result) return false

            checkPremiumStatus(context, force = true)
            SessionManager.setActive(context, true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun checkAppUpdate(): AppConfig? {
        val supabaseClient = client ?: return null
        return try {
            supabaseClient.from("app_config")
                .select()
                .decodeList<AppConfig>()
                .maxWithOrNull(
                    compareBy<AppConfig> { it.latest_version_code }
                        .thenByDescending { it.force_update }
                        .thenBy { it.latest_version_name }
                )
        } catch (e: Exception) {
            null
        }
    }

    fun startPremiumCheckInBackground(context: Context) {
        applicationScope.launch {
            checkPremiumStatus(context)
        }
    }

    fun reportDeviceVersionInBackground(context: Context, force: Boolean = false) {
        applicationScope.launch {
            reportDeviceVersion(context, force)
        }
    }

    suspend fun reportDeviceVersion(context: Context, force: Boolean = false) {
        deviceVersionReportMutex.withLock {
            val prefs = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val lastReportedAt = prefs.getLong(KEY_LAST_VERSION_REPORT_AT, 0L)
            val lastReportedVersionCode = prefs.getInt(KEY_LAST_REPORTED_VERSION_CODE, -1)
            val shouldReport = force ||
                lastReportedVersionCode != BuildConfig.VERSION_CODE ||
                now - lastReportedAt >= VERSION_REPORT_INTERVAL_MS

            if (!shouldReport) return@withLock

            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (androidId.isNullOrBlank()) return@withLock

            val supabaseClient = client ?: return@withLock

            try {
                supabaseClient.postgrest.rpc(
                    function = "upsert_device_version",
                    parameters = mapOf(
                        "p_device_id" to androidId,
                        "p_version_code" to BuildConfig.VERSION_CODE,
                        "p_version_name" to BuildConfig.VERSION_NAME
                    )
                )
                prefs.edit()
                    .putLong(KEY_LAST_VERSION_REPORT_AT, now)
                    .putInt(KEY_LAST_REPORTED_VERSION_CODE, BuildConfig.VERSION_CODE)
                    .apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
