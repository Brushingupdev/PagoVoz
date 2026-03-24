package com.example.pagovoz

import android.content.Context
import android.provider.Settings
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

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

    // Scope único vinculado al proceso — evita coroutines huérfanas
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        val supabaseClient = client ?: return
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val channel = supabaseClient.realtime.channel("license-updates")

        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "licenses"
        }.onEach { change ->
            // Extraer el device_id del registro actualizado
            val recordDeviceId = change.record["device_id"]?.jsonPrimitive?.content
            if (recordDeviceId == androidId) {
                // Si el cambio es para este equipo, forzamos refresco
                checkPremiumStatus(context, force = true)
            }
        }.launchIn(applicationScope)

        applicationScope.launch {
            supabaseClient.realtime.connect()
            channel.subscribe()
        }
    }

    suspend fun checkPremiumStatus(context: Context, force: Boolean = false): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
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
            supabaseClient.from("app_config").select().decodeSingleOrNull<AppConfig>()
        } catch (e: Exception) {
            null
        }
    }

    fun startPremiumCheckInBackground(context: Context) {
        applicationScope.launch {
            checkPremiumStatus(context)
        }
    }

    suspend fun reportDeviceVersion(context: Context) {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val supabaseClient = client ?: return

        try {
            supabaseClient.postgrest.rpc(
                function = "upsert_device_version",
                parameters = mapOf(
                    "p_device_id" to androidId,
                    "p_version_code" to BuildConfig.VERSION_CODE,
                    "p_version_name" to BuildConfig.VERSION_NAME
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
