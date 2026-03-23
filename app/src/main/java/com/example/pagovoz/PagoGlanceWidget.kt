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
    }
}

@Composable
private fun WidgetContent(total: Double, count: Int) {
    val bgColor = ColorProvider(Color(0xFF0E2B1F))          // verde oscuro premium
    val brandGreen = ColorProvider(Color(0xFF1DB870))        // verde marca
    val whiteHigh = ColorProvider(Color(0xFFFFFFFF))
    val whiteMid = ColorProvider(Color(0xB3FFFFFF))          // 70% opacity

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna izquierda: etiqueta + total
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = "TOTAL HOY",
                    style = TextStyle(
                        color = whiteMid,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "S/ ${String.format(Locale.US, "%.2f", total)}",
                    style = TextStyle(
                        color = brandGreen,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            // Columna derecha: número de pagos
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = count.toString(),
                    style = TextStyle(
                        color = whiteHigh,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (count == 1) "pago" else "pagos",
                    style = TextStyle(
                        color = whiteMid,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
