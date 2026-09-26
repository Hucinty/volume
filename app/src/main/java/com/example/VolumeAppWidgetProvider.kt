package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import kotlin.math.roundToInt

class VolumeAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_VOL_UP = "com.example.ACTION_VOL_UP"
        const val ACTION_VOL_DOWN = "com.example.ACTION_VOL_DOWN"
        const val ACTION_VOL_MUTE_TOGGLE = "com.example.ACTION_VOL_MUTE_TOGGLE"
        const val ACTION_VOL_SET_PRESET = "com.example.ACTION_VOL_SET_PRESET"
        const val EXTRA_PRESET_PERCENT = "extra_preset_percent"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val thisWidget = ComponentName(context, VolumeAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                val state = VolumeManager.syncFromSystem(context)
                for (widgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId, state)
                }
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            state: VolumeState
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_volume_slider)

            val percent = state.volumePercent.roundToInt()
            val isMuted = state.isMuted || percent == 0

            // 1. Text & Progress
            views.setTextViewText(R.id.widget_percent_text, "$percent%")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)

            if (isMuted) {
                views.setTextViewText(R.id.widget_status, "Muted • Tap to unmute")
                views.setImageViewResource(R.id.widget_speaker_icon, R.drawable.ic_widget_mute)
                views.setImageViewResource(R.id.widget_btn_mute, R.drawable.ic_widget_mute)
            } else {
                views.setTextViewText(R.id.widget_status, "Media Sound • ${state.currentSystemVolume}/${state.maxSystemVolume}")
                views.setImageViewResource(R.id.widget_speaker_icon, R.drawable.ic_widget_unmute)
                views.setImageViewResource(R.id.widget_btn_mute, R.drawable.ic_widget_unmute)
            }

            val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            // 2. Open MainActivity on background container click
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val launchPendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flag)
            views.setOnClickPendingIntent(R.id.widget_root, launchPendingIntent)

            // 3. Mute toggle button
            val muteIntent = Intent(context, VolumeAppWidgetProvider::class.java).apply {
                action = ACTION_VOL_MUTE_TOGGLE
            }
            val mutePendingIntent = PendingIntent.getBroadcast(context, 101, muteIntent, flag)
            views.setOnClickPendingIntent(R.id.widget_btn_mute, mutePendingIntent)

            // 4. Volume Down (-10%)
            val downIntent = Intent(context, VolumeAppWidgetProvider::class.java).apply {
                action = ACTION_VOL_DOWN
            }
            val downPendingIntent = PendingIntent.getBroadcast(context, 102, downIntent, flag)
            views.setOnClickPendingIntent(R.id.widget_btn_down, downPendingIntent)

            // 5. Volume Up (+10%)
            val upIntent = Intent(context, VolumeAppWidgetProvider::class.java).apply {
                action = ACTION_VOL_UP
            }
            val upPendingIntent = PendingIntent.getBroadcast(context, 103, upIntent, flag)
            views.setOnClickPendingIntent(R.id.widget_btn_up, upPendingIntent)

            // 6. Preset 25%
            val preset25Intent = Intent(context, VolumeAppWidgetProvider::class.java).apply {
                action = ACTION_VOL_SET_PRESET
                putExtra(EXTRA_PRESET_PERCENT, 25f)
            }
            val preset25Pending = PendingIntent.getBroadcast(context, 201, preset25Intent, flag)
            views.setOnClickPendingIntent(R.id.widget_preset_25, preset25Pending)

            // 7. Preset 70%
            val preset70Intent = Intent(context, VolumeAppWidgetProvider::class.java).apply {
                action = ACTION_VOL_SET_PRESET
                putExtra(EXTRA_PRESET_PERCENT, 70f)
            }
            val preset70Pending = PendingIntent.getBroadcast(context, 202, preset70Intent, flag)
            views.setOnClickPendingIntent(R.id.widget_preset_70, preset70Pending)

            // 8. Preset 100% (MAX)
            val presetMaxIntent = Intent(context, VolumeAppWidgetProvider::class.java).apply {
                action = ACTION_VOL_SET_PRESET
                putExtra(EXTRA_PRESET_PERCENT, 100f)
            }
            val presetMaxPending = PendingIntent.getBroadcast(context, 203, presetMaxIntent, flag)
            views.setOnClickPendingIntent(R.id.widget_preset_max, presetMaxPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun pinWidgetToHomeScreen(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val myProvider = ComponentName(context, VolumeAppWidgetProvider::class.java)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val pinnedSuccessIntent = Intent(context, VolumeAppWidgetProvider::class.java)
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        0,
                        pinnedSuccessIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    return appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                }
            }
            return false
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val state = VolumeManager.syncFromSystem(context)
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId, state)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_VOL_UP -> {
                VolumeManager.adjustVolumeBy(context, 10f)
            }
            ACTION_VOL_DOWN -> {
                VolumeManager.adjustVolumeBy(context, -10f)
            }
            ACTION_VOL_MUTE_TOGGLE -> {
                VolumeManager.toggleMute(context)
            }
            ACTION_VOL_SET_PRESET -> {
                val preset = intent.getFloatExtra(EXTRA_PRESET_PERCENT, 50f)
                VolumeManager.setVolumePercent(context, preset)
            }
        }
    }
}
