package com.example

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class VolumeState(
    val volumePercent: Float = 50f,
    val isMuted: Boolean = false,
    val maxSystemVolume: Int = 15,
    val currentSystemVolume: Int = 8,
    val activeStream: Int = AudioManager.STREAM_MUSIC
)

enum class TileClickAction(val title: String, val description: String) {
    SHOW_SYSTEM_PANEL(
        "Open System Volume Panel",
        "Shows native Android on-screen volume sliders with FLAG_SHOW_UI (Recommended)"
    ),
    CYCLE_PRESETS(
        "Cycle Step Volume",
        "Cycles Mute -> 30% -> 70% -> 100% on each tap"
    ),
    TOGGLE_MUTE(
        "Mute / Unmute Toggle",
        "Instantly silences and restores stream volume"
    )
}

enum class AudioStreamType(val label: String, val streamConstant: Int) {
    MEDIA("Media", AudioManager.STREAM_MUSIC),
    RING("Ring", AudioManager.STREAM_RING),
    NOTIFICATION("Notifications", AudioManager.STREAM_NOTIFICATION),
    ALARM("Alarm", AudioManager.STREAM_ALARM)
}

object VolumeManager {
    private val _volumeState = MutableStateFlow(VolumeState())
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    var tileClickAction: TileClickAction = TileClickAction.SHOW_SYSTEM_PANEL
    var targetStream: Int = AudioManager.STREAM_MUSIC

    private var toneGenerator: ToneGenerator? = null
    private var lastNonZeroPercent: Float = 50f

    @Synchronized
    private fun getToneGen(): ToneGenerator? {
        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 35)
            } catch (_: Exception) {}
        }
        return toneGenerator
    }

    fun playFeedback(context: Context) {
        // Haptic feedback
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                v?.vibrate(10L)
            }
        } catch (_: Exception) {}

        // Audio feedback click
        try {
            getToneGen()?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
        } catch (_: Exception) {}
    }

    fun syncFromSystem(context: Context): VolumeState {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return _volumeState.value
        val maxVol = am.getStreamMaxVolume(targetStream).coerceAtLeast(1)
        val curVol = am.getStreamVolume(targetStream)
        val percent = ((curVol.toFloat() / maxVol.toFloat()) * 100f).coerceIn(0f, 100f)
        val isMuted = curVol == 0

        if (percent > 0f) {
            lastNonZeroPercent = percent
        }

        val state = VolumeState(
            volumePercent = percent,
            isMuted = isMuted,
            maxSystemVolume = maxVol,
            currentSystemVolume = curVol,
            activeStream = targetStream
        )
        _volumeState.value = state
        return state
    }

    fun setTargetStreamType(context: Context, streamConstant: Int) {
        targetStream = streamConstant
        syncFromSystem(context)
        VolumeAppWidgetProvider.updateAllWidgets(context)
        VolumeTileService.requestTileUpdate(context)
    }

    /**
     * Option 1: Open the System Volume Panel (Recommended)
     * Triggers the native Android on-screen volume sliders with FLAG_SHOW_UI
     */
    fun openSystemVolumePanel(context: Context) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            am.adjustStreamVolume(
                targetStream,
                AudioManager.ADJUST_SAME,
                AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
            )
            // Attempt to collapse notification shade
            try {
                @Suppress("DEPRECATION")
                val closeIntent = android.content.Intent(android.content.Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context.sendBroadcast(closeIntent)
            } catch (_: Exception) {}
        } catch (_: Exception) {}

        playFeedback(context)
        syncFromSystem(context)
    }

    /**
     * Option 2: Step Increment (Up)
     */
    fun stepRaise(context: Context): VolumeState {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return _volumeState.value
        try {
            am.adjustStreamVolume(
                targetStream,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        } catch (_: Exception) {}
        playFeedback(context)
        val state = syncFromSystem(context)
        VolumeAppWidgetProvider.updateAllWidgets(context)
        VolumeTileService.requestTileUpdate(context)
        return state
    }

    /**
     * Option 2: Step Decrement (Down)
     */
    fun stepLower(context: Context): VolumeState {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return _volumeState.value
        try {
            am.adjustStreamVolume(
                targetStream,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
        } catch (_: Exception) {}
        playFeedback(context)
        val state = syncFromSystem(context)
        VolumeAppWidgetProvider.updateAllWidgets(context)
        VolumeTileService.requestTileUpdate(context)
        return state
    }

    fun setVolumePercent(context: Context, percent: Float, feedback: Boolean = true): VolumeState {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return _volumeState.value
        val maxVol = am.getStreamMaxVolume(targetStream).coerceAtLeast(1)
        val clampedPercent = percent.coerceIn(0f, 100f)
        val targetSystemVol = ((clampedPercent / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)

        try {
            am.setStreamVolume(targetStream, targetSystemVol, AudioManager.FLAG_SHOW_UI)
        } catch (_: Exception) {}

        if (clampedPercent > 0f) {
            lastNonZeroPercent = clampedPercent
        }

        val state = VolumeState(
            volumePercent = clampedPercent,
            isMuted = (clampedPercent == 0f || targetSystemVol == 0),
            maxSystemVolume = maxVol,
            currentSystemVolume = targetSystemVol,
            activeStream = targetStream
        )
        _volumeState.value = state

        if (feedback) {
            playFeedback(context)
        }

        // Notify Widget & Quick Settings Tile
        VolumeAppWidgetProvider.updateAllWidgets(context)
        VolumeTileService.requestTileUpdate(context)

        return state
    }

    fun adjustVolumeBy(context: Context, deltaPercent: Float): VolumeState {
        val current = _volumeState.value.volumePercent
        val target = (current + deltaPercent).coerceIn(0f, 100f)
        return setVolumePercent(context, target, feedback = true)
    }

    fun toggleMute(context: Context): VolumeState {
        val current = _volumeState.value
        return if (current.isMuted || current.volumePercent == 0f) {
            val restore = if (lastNonZeroPercent > 5f) lastNonZeroPercent else 50f
            setVolumePercent(context, restore, feedback = true)
        } else {
            lastNonZeroPercent = current.volumePercent
            setVolumePercent(context, 0f, feedback = true)
        }
    }

    fun cycleStep(context: Context): VolumeState {
        val cur = _volumeState.value.volumePercent
        val target = when {
            _volumeState.value.isMuted || cur == 0f -> 30f
            cur < 40f -> 70f
            cur < 85f -> 100f
            else -> 0f
        }
        return setVolumePercent(context, target, feedback = true)
    }
}
