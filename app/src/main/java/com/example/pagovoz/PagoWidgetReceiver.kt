package com.example.pagovoz

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Entry point del widget usando la API de Glance.
 */
class PagoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PagoGlanceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        PagoGlanceWidget.updateAll(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        PagoGlanceWidget.updateAll(context)
    }
}
