package com.example

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

// Pastel Palette Definitions
enum class PastelPalette(val displayName: String, val colors: List<Color>, val accentDot: Color) {
  LILAC("Lilac", listOf(Color(0xFFC4B5FD), Color(0xFFDDD6FE), Color(0xFFEDE9FE)), Color(0xFFC4B5FD)),
  PEACH("Peach", listOf(Color(0xFFFECDD3), Color(0xFFFDE2E4), Color(0xFFFBCFE8)), Color(0xFFFECDD3)),
  MINT("Mint", listOf(Color(0xFFA7F3D0), Color(0xFFBBF7D0), Color(0xFFD1FAE5)), Color(0xFFA7F3D0)),
  SKY("Sky", listOf(Color(0xFFBAE6FD), Color(0xFFC7D2FE), Color(0xFFE0F2FE)), Color(0xFFBAE6FD)),
  BUTTER("Butter", listOf(Color(0xFFFEF08A), Color(0xFFFED7AA), Color(0xFFFEF9C3)), Color(0xFFFEF08A))
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        VolumeSliderApp()
      }
    }
  }
}

enum class AppTab(val title: String, val icon: @Composable () -> Unit) {
  WEB_TILE("Web Tile", { Icon(Icons.Default.Web, contentDescription = null, modifier = Modifier.size(15.dp)) }),
  NATIVE_TILE("Native Tile", { Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(15.dp)) }),
  WIDGETS_AND_TILES("Widgets & QS", { Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(15.dp)) }),
  HTML_CODE("HTML Code", { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp)) })
}

@Composable
fun VolumeSliderApp() {
  var selectedTab by remember { mutableStateOf(AppTab.NATIVE_TILE) }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Collect reactive volume state synced with Widget, TileService, and System Audio
  val volumeState by VolumeManager.volumeState.collectAsStateWithLifecycle()

  // Sync on startup and resume
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        VolumeManager.syncFromSystem(context)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  fun setAppVolume(newVal: Float) {
    VolumeManager.setVolumePercent(context, newVal)
  }

  fun toggleAppMute() {
    VolumeManager.toggleMute(context)
  }

  val isGlobalMuted = volumeState.isMuted
  val effectiveVolume = if (isGlobalMuted) 0f else volumeState.volumePercent

  val htmlCode = remember {
    try {
      context.assets.open("index.html").use { input ->
        BufferedReader(InputStreamReader(input)).readText()
      }
    } catch (e: Exception) {
      "<!-- Error loading HTML file from assets: ${e.localizedMessage} -->"
    }
  }

  BackHandler(enabled = selectedTab != AppTab.NATIVE_TILE) {
    selectedTab = AppTab.NATIVE_TILE
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
          .statusBarsPadding()
          .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
          )
          .padding(bottom = 8.dp)
      ) {
        // ==============================================
        // 1. REALISTIC STATUS BAR WITH INLINE VOLUME TILE
        // ==============================================
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Time & Stream Tag
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "09:41",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Box(
              modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Color(0xFFC4B5FD))
            )
          }

          // Center: INTERACTIVE STATUS BAR VOLUME TILE PILL
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .testTag("status_bar_volume_tile")
              .pointerInput(Unit) {
                detectTapGestures {
                  toggleAppMute()
                }
              }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = when {
                  isGlobalMuted || effectiveVolume == 0f -> Icons.AutoMirrored.Filled.VolumeOff
                  effectiveVolume <= 33f -> Icons.AutoMirrored.Filled.VolumeMute
                  effectiveVolume <= 66f -> Icons.AutoMirrored.Filled.VolumeDown
                  else -> Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = "Status Bar Volume Tile",
                tint = if (isGlobalMuted) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
              )

              // Mini slider track in status bar
              BoxWithConstraints(
                modifier = Modifier
                  .width(60.dp)
                  .height(6.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                  .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                      val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                      setAppVolume(fraction * 100f)
                    }
                  }
              ) {
                val fillWidth = maxWidth * (effectiveVolume / 100f)
                Box(
                  modifier = Modifier
                    .fillMaxHeight()
                    .width(fillWidth)
                    .background(
                      Brush.horizontalGradient(
                        colors = listOf(Color(0xFFC4B5FD), Color(0xFFDDD6FE), Color(0xFFBAE6FD))
                      )
                    )
                )
              }

              Text(
                text = "${effectiveVolume.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isGlobalMuted) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Battery & Network
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "98%",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
              shape = RoundedCornerShape(2.dp),
              color = Color(0xFF10B981),
              modifier = Modifier
                .width(10.dp)
                .height(6.dp)
            ) {}
          }
        }

        // ==============================================
        // 2. NAV BAR TILES IN STATUS BAR HEADER
        // ==============================================
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 4 Nav Bar Tiles
          Row(
            modifier = Modifier
              .weight(1f, fill = false)
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AppTab.entries.forEach { tab ->
              val isSelected = selectedTab == tab
              val tileBg by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                label = "nav_tile_bg"
              )
              val tileContentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "nav_tile_content"
              )

              Surface(
                onClick = { selectedTab = tab },
                shape = CircleShape,
                color = tileBg,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent
                ),
                modifier = Modifier.testTag("nav_tile_${tab.name.lowercase()}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  tab.icon()
                  Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = tileContentColor
                  )
                }
              }
            }
          }

          // Reload / Action Tile on Far Right
          if (selectedTab == AppTab.WEB_TILE) {
            Surface(
              onClick = { webViewRef?.reload() },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surfaceContainer,
              modifier = Modifier.testTag("reload_webview_button")
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Reload Web View",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        AppTab.WEB_TILE -> {
          WebSliderView(
            onWebViewReady = { webViewRef = it }
          )
        }
        AppTab.NATIVE_TILE -> {
          NativeQuickSettingsSliderView(
            initialVolume = effectiveVolume,
            initialMuted = isGlobalMuted,
            onGlobalVolumeChange = { vol -> setAppVolume(vol) },
            onGlobalMuteToggle = { toggleAppMute() }
          )
        }
        AppTab.WIDGETS_AND_TILES -> {
          WidgetsAndTilesScreen(
            volumeState = volumeState,
            onSetVolume = { vol -> setAppVolume(vol) },
            onToggleMute = { toggleAppMute() }
          )
        }
        AppTab.HTML_CODE -> {
          CodeViewerScreen(
            htmlContent = htmlCode,
            onCopyCode = { copyToClipboard(context, htmlCode) }
          )
        }
      }
    }
  }
}

/**
 * Web Slider Container
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebSliderView(
  onWebViewReady: (WebView) -> Unit,
  modifier: Modifier = Modifier
) {
  AndroidView(
    factory = { ctx ->
      WebView(ctx).apply {
        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          loadWithOverviewMode = true
          useWideViewPort = true
          builtInZoomControls = false
          displayZoomControls = false
          mediaPlaybackRequiresUserGesture = false
          cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {}
        loadUrl("file:///android_asset/index.html")
        onWebViewReady(this)
      }
    },
    update = { webView ->
      onWebViewReady(webView)
    },
    modifier = modifier
      .fillMaxSize()
      .testTag("web_slider_view")
  )
}

/**
 * Minimal Pastel Native Jetpack Compose Quick Settings Volume Tile
 */
@Composable
fun NativeQuickSettingsSliderView(
  initialVolume: Float,
  initialMuted: Boolean,
  onGlobalVolumeChange: (Float) -> Unit,
  onGlobalMuteToggle: () -> Unit
) {
  var isVerticalLayout by remember { mutableStateOf(false) }
  var selectedPalette by remember { mutableStateOf(PastelPalette.LILAC) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {

    // Pastel Palette Selector Row
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      PastelPalette.entries.forEach { palette ->
        val isSelected = selectedPalette == palette
        Box(
          modifier = Modifier
            .size(if (isSelected) 26.dp else 22.dp)
            .clip(CircleShape)
            .background(palette.accentDot)
            .border(
              width = if (isSelected) 2.5.dp else 0.dp,
              color = MaterialTheme.colorScheme.primary,
              shape = CircleShape
            )
            .clickable { selectedPalette = palette }
        )
      }
    }

    // Top Card (Stream & Layout Switcher)
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      shape = RoundedCornerShape(28.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Media Stream Tile",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
          )
          Text(
            text = if (initialMuted) "Muted • Synced with Status Bar" else "Harmonic • Status Bar Tile Active",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          onClick = { isVerticalLayout = !isVerticalLayout },
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier.testTag("toggle_native_layout_button")
        ) {
          Text(
            text = if (isVerticalLayout) "Capsule ↕" else "Pill ↔",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }
    }

    // MAIN MINIMAL PASTEL CARD
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      shape = RoundedCornerShape(32.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .shadow(4.dp, shape = RoundedCornerShape(32.dp), ambientColor = Color(0x1A000000))
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(32.dp))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {

        // Percentage & Descriptor Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = when {
              initialMuted || initialVolume == 0f -> "Muted"
              initialVolume <= 25f -> "Gentle"
              initialVolume <= 50f -> "Subtle"
              initialVolume <= 75f -> "Comfortable"
              else -> "Vibrant"
            },
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Text(
              text = "${initialVolume.roundToInt()}",
              style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp
              ),
              color = if (initialMuted) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "%",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // TILE SLIDER DISPLAY
        if (!isVerticalLayout) {
          HorizontalPastelTileSlider(
            volume = initialVolume,
            isMuted = initialMuted,
            gradientColors = selectedPalette.colors,
            onVolumeChange = onGlobalVolumeChange,
            onMuteToggle = onGlobalMuteToggle
          )
        } else {
          VerticalPastelTileSlider(
            volume = initialVolume,
            isMuted = initialMuted,
            gradientColors = selectedPalette.colors,
            onVolumeChange = onGlobalVolumeChange,
            onMuteToggle = onGlobalMuteToggle
          )
        }

        // Presets: 0%, 25%, 50%, 75%, 100%
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf(0, 25, 50, 75, 100).forEach { presetVal ->
            val isSelected = initialVolume.roundToInt() == presetVal
            val btnColor by animateColorAsState(
              targetValue = if (isSelected) selectedPalette.accentDot else MaterialTheme.colorScheme.surfaceContainer,
              label = "preset_color"
            )
            val textColor by animateColorAsState(
              targetValue = if (isSelected) Color(0xFF2E2836) else MaterialTheme.colorScheme.onSurfaceVariant,
              label = "preset_text"
            )

            Surface(
              onClick = { onGlobalVolumeChange(presetVal.toFloat()) },
              shape = CircleShape,
              color = btnColor,
              modifier = Modifier
                .weight(1f)
                .padding(horizontal = 3.dp)
                .height(36.dp)
                .testTag("preset_btn_$presetVal")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "$presetVal%",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = textColor
                )
              }
            }
          }
        }

      }
    }

    // Status Bar Tile Hint
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFFC4B5FD))
        )
        Text(
          text = "Status Bar Volume Tile is active at top. You can drag or tap the status bar pill directly to adjust or mute system volume!",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

  }
}

/**
 * Horizontal Pastel Pill Slider
 */
@Composable
fun HorizontalPastelTileSlider(
  volume: Float,
  isMuted: Boolean,
  gradientColors: List<Color>,
  onVolumeChange: (Float) -> Unit,
  onMuteToggle: () -> Unit
) {
  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth()
      .height(72.dp)
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
      .pointerInput(Unit) {
        detectDragGestures { change, _ ->
          val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
          onVolumeChange(fraction * 100f)
        }
      }
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          val fraction = (offset.x / size.width).coerceIn(0f, 1f)
          onVolumeChange(fraction * 100f)
        }
      }
      .testTag("horizontal_native_slider")
  ) {
    val totalWidth = maxWidth
    val fillWidth = totalWidth * (volume / 100f)

    Box(
      modifier = Modifier
        .fillMaxHeight()
        .width(fillWidth)
        .background(Brush.horizontalGradient(colors = gradientColors))
    )

    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Surface(
        onClick = onMuteToggle,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.85f),
        modifier = Modifier
          .size(44.dp)
          .testTag("native_mute_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = when {
              isMuted || volume == 0f -> Icons.AutoMirrored.Filled.VolumeOff
              volume <= 33f -> Icons.AutoMirrored.Filled.VolumeMute
              volume <= 66f -> Icons.AutoMirrored.Filled.VolumeDown
              else -> Icons.AutoMirrored.Filled.VolumeUp
            },
            contentDescription = "Mute or unmute volume",
            tint = if (isMuted) Color(0xFFF43F5E) else Color(0xFF2E2836),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Text(
        text = if (isMuted) "Muted" else "${volume.roundToInt()}%",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFF2E2836)
      )

      Box(
        modifier = Modifier
          .width(4.dp)
          .height(24.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.9f))
      )
    }
  }
}

/**
 * Vertical Pastel Capsule Slider
 */
@Composable
fun VerticalPastelTileSlider(
  volume: Float,
  isMuted: Boolean,
  gradientColors: List<Color>,
  onVolumeChange: (Float) -> Unit,
  onMuteToggle: () -> Unit
) {
  BoxWithConstraints(
    modifier = Modifier
      .width(100.dp)
      .height(210.dp)
      .clip(RoundedCornerShape(44.dp))
      .background(MaterialTheme.colorScheme.surfaceContainer)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(44.dp))
      .pointerInput(Unit) {
        detectDragGestures { change, _ ->
          val fraction = ((size.height - change.position.y) / size.height).coerceIn(0f, 1f)
          onVolumeChange(fraction * 100f)
        }
      }
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          val fraction = ((size.height - offset.y) / size.height).coerceIn(0f, 1f)
          onVolumeChange(fraction * 100f)
        }
      }
      .testTag("vertical_native_slider")
  ) {
    val totalHeight = maxHeight
    val fillHeight = totalHeight * (volume / 100f)

    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(fillHeight)
        .background(Brush.verticalGradient(colors = gradientColors.reversed()))
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = if (isMuted) "Mute" else "${volume.roundToInt()}%",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = Color(0xFF2E2836)
      )

      Box(
        modifier = Modifier
          .width(24.dp)
          .height(4.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.9f))
      )

      Surface(
        onClick = onMuteToggle,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.85f),
        modifier = Modifier
          .size(44.dp)
          .testTag("native_vertical_mute_button")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = when {
              isMuted || volume == 0f -> Icons.AutoMirrored.Filled.VolumeOff
              volume <= 33f -> Icons.AutoMirrored.Filled.VolumeMute
              volume <= 66f -> Icons.AutoMirrored.Filled.VolumeDown
              else -> Icons.AutoMirrored.Filled.VolumeUp
            },
            contentDescription = "Mute or unmute volume",
            tint = if (isMuted) Color(0xFFF43F5E) else Color(0xFF2E2836),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

/**
 * Single-File HTML Code Viewer
 */
@Composable
fun CodeViewerScreen(
  htmlContent: String,
  onCopyCode: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Single-File HTML Code",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "Includes Status Bar Tile & Quick Settings Shade",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Button(
        onClick = onCopyCode,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = CircleShape,
        modifier = Modifier.testTag("copy_html_button")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy HTML", style = MaterialTheme.typography.labelSmall)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = htmlContent,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          lineHeight = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}

fun copyToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  val clip = ClipData.newPlainText("Volume Slider HTML", text)
  clipboard?.setPrimaryClip(clip)
  Toast.makeText(context, "Single-file HTML copied to clipboard!", Toast.LENGTH_SHORT).show()
}
