package com.example

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.util.concurrent.Executor
import kotlin.math.roundToInt

class VolumeTileService : TileService() {

    companion object {
        fun requestTileUpdate(context: Context) {
            try {
                requestListeningState(
                    context,
                    ComponentName(context, VolumeTileService::class.java)
                )
            } catch (_: Exception) {}
        }

        fun requestAddTileToQuickSettings(
            context: Context,
            onResult: (Boolean) -> Unit
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val statusBarManager = context.getSystemService(StatusBarManager::class.java)
                if (statusBarManager != null) {
                    val tileComponent = ComponentName(context, VolumeTileService::class.java)
                    val icon = Icon.createWithResource(context, R.drawable.ic_qs_volume)
                    val executor = Executor { it.run() }
                    try {
                        statusBarManager.requestAddTileService(
                            tileComponent,
                            context.getString(R.string.qs_tile_volume_label),
                            icon,
                            executor
                        ) { resultCode ->
                            onResult(resultCode == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED)
                        }
                        return
                    } catch (_: Exception) {}
                }
            }
            onResult(false)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val am = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val currentStream = VolumeManager.targetStream

        when (VolumeManager.tileClickAction) {
            TileClickAction.SHOW_SYSTEM_PANEL -> {
                // Option 1 (Recommended): Open the System Volume Panel directly
                // FLAG_SHOW_UI forces the system volume slider overlay to pop up immediately
                am.adjustStreamVolume(
                    currentStream,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI or AudioManager.FLAG_PLAY_SOUND
                )

                // Automatically collapse the notification panel to show the volume slider clearly
                try {
                    @Suppress("DEPRECATION")
                    val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    sendBroadcast(closeIntent)
                } catch (_: Exception) {}

                VolumeManager.playFeedback(applicationContext)
            }
            TileClickAction.CYCLE_PRESETS -> {
                val newState = VolumeManager.cycleStep(applicationContext)
                try {
                    am.adjustStreamVolume(
                        currentStream,
                        AudioManager.ADJUST_SAME,
                        AudioManager.FLAG_SHOW_UI
                    )
                } catch (_: Exception) {}
                applyStateToTile(newState)
            }
            TileClickAction.TOGGLE_MUTE -> {
                val newState = VolumeManager.toggleMute(applicationContext)
                try {
                    am.adjustStreamVolume(
                        currentStream,
                        AudioManager.ADJUST_SAME,
                        AudioManager.FLAG_SHOW_UI
                    )
                } catch (_: Exception) {}
                applyStateToTile(newState)
            }
        }

        updateTileState()
    }

    private fun updateTileState() {
        val state = VolumeManager.syncFromSystem(applicationContext)
        applyStateToTile(state)
    }

    private fun applyStateToTile(state: VolumeState) {
        val tile = qsTile ?: return
        val percent = state.volumePercent.roundToInt()
        val isMuted = state.isMuted || percent == 0

        tile.state = if (isMuted) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.label = "$percent%"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isMuted) "Muted" else when (VolumeManager.targetStream) {
                AudioManager.STREAM_RING -> "Ring"
                AudioManager.STREAM_NOTIFICATION -> "Notify"
                AudioManager.STREAM_ALARM -> "Alarm"
                else -> "Media"
            }
        }
        val iconRes = if (isMuted) R.drawable.ic_qs_volume_mute else R.drawable.ic_qs_volume
        tile.icon = Icon.createWithResource(this, iconRes)
        tile.updateTile()
    }
}
