package com.example

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string resources`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Volume Slider", appName)

    val qsLabel = context.getString(R.string.qs_tile_volume_label)
    assertEquals("Volume", qsLabel)

    val widgetTitle = context.getString(R.string.widget_title)
    assertEquals("Pastel Volume Widget", widgetTitle)
  }

  @Test
  fun `volume manager updates state and bounds correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val state = VolumeManager.syncFromSystem(context)
    assertNotNull(state)

    // Set 50%
    val state50 = VolumeManager.setVolumePercent(context, 50f, feedback = false)
    assertEquals(50f, state50.volumePercent, 0.1f)
    assertFalse(state50.isMuted)

    // Set Mute 0%
    val state0 = VolumeManager.setVolumePercent(context, 0f, feedback = false)
    assertEquals(0f, state0.volumePercent, 0.1f)
    assertTrue(state0.isMuted)

    // Toggle unmute restores non-zero
    val restored = VolumeManager.toggleMute(context)
    assertTrue(restored.volumePercent > 0f)
    assertFalse(restored.isMuted)
  }

  @Test
  fun `quick settings tile options test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Test Option 1 (Open System Volume Panel) does not throw
    VolumeManager.openSystemVolumePanel(context)

    // Test Option 2 (Step Raise and Lower)
    val afterRaise = VolumeManager.stepRaise(context)
    assertNotNull(afterRaise)

    val afterLower = VolumeManager.stepLower(context)
    assertNotNull(afterLower)

    // Test stream change
    VolumeManager.setTargetStreamType(context, AudioManager.STREAM_RING)
    assertEquals(AudioManager.STREAM_RING, VolumeManager.targetStream)
  }
}
