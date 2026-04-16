package com.example.pagovoz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class PagoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val total = SessionManager.getDailyTotal(context).toDouble()
        val count = SessionManager.getDailyCount(context)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    total = total,
                    count = count
                )
            }
        }
    }

    companion object {
        fun updateAll(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(PagoGlanceWidget::class.java)
                glanceIds.forEach { id ->
                    PagoGlanceWidget().update(context, id)
                }
            }
        }

        fun requestPin(context: Context) {
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val myProvider = android.content.ComponentName(context, PagoWidgetReceiver::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            }
        }
    }
}

@Composable
private fun WidgetContent(total: Double, count: Int) {
    // Definición de colores usando Hex para máxima compatibilidad en widgets
    val bgColor = ColorProvider(Color(0xFF0D1117))
    val brandGreen = Color(0xFF1DB870)
    val brandGreenProvider = ColorProvider(brandGreen)
    val brandGreenTransparent = ColorProvider(Color(0x1F1DB870)) // ~12% Alpha Green
    val white90 = ColorProvider(Color(0xE6FFFFFF))               // 90% Alpha White
    val white50 = ColorProvider(Color(0x80FFFFFF))               // 50% Alpha White
    val white04 = ColorProvider(Color(0x0AFFFFFF))               // 4% Alpha White

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HablaPago",
                    style = TextStyle(
                        color = white90,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                
                Row(
                    modifier = GlanceModifier
                        .background(brandGreenTransparent)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .width(6.dp)
                            .height(6.dp)
                            .background(brandGreenProvider)
                    ) {}
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = "VIVO",
                        style = TextStyle(
                            color = brandGreenProvider,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.defaultWeight())

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "TOTAL RECAUDADO",
                        style = TextStyle(
                            color = white50,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "S/ ${String.format(Locale.US, "%.2f", total)}",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = count.toString(),
                        style = TextStyle(
                            color = brandGreenProvider,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (count == 1) "COBRO" else "COBROS",
                        style = TextStyle(
                            color = brandGreenProvider,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(white04)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "◈",
                    style = TextStyle(color = brandGreenProvider, fontSize = 10.sp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "$count ${if (count == 1) "pago captado" else "pagos captados"}",
                    style = TextStyle(
                        color = white50,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
