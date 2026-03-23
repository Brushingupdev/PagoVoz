# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for stack traces in crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Supabase SDK — mantener clases de serialización
# ============================================================
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ============================================================
# Ktor — cliente HTTP usado internamente por Supabase
# ============================================================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ============================================================
# kotlinx.serialization — necesario para @Serializable data classes
# ============================================================
-keepattributes *Annotation*
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$serializer {
    static **$serializer INSTANCE;
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================
# Data classes del proyecto (License, AppConfig, PaymentRecord, etc.)
# ============================================================
-keep class com.example.pagovoz.License { *; }
-keep class com.example.pagovoz.AppConfig { *; }
-keep class com.example.pagovoz.ActivateLicenseParams { *; }
-keep class com.example.pagovoz.PremiumStatusParams { *; }
-keep class com.example.pagovoz.PremiumStatusResponse { *; }
-keep class com.example.pagovoz.PaymentRecord { *; }

# ============================================================
# Jetpack Compose — necesario con minificación
# ============================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**