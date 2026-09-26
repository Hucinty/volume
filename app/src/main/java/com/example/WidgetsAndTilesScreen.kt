package com.example

import android.content.Context
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun WidgetsAndTilesScreen(
    volumeState: VolumeState,
    onSetVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPalette by remember { mutableStateOf(PastelPalette.LILAC) }
    var isCompactWidgetPreview by remember { mutableStateOf(false) }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    val effectiveVolume = if (volumeState.isMuted) 0f else volumeState.volumePercent

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // Header Card: Winget (Widget) & Quick Settings Tile Overview
        // -------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = selectedPalette.accentDot.copy(alpha = 0.45f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = Color(0xFF2E2836),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Widget & Quick Settings Tile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Direct home screen volume control & notification shade tile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // SECTION 1: HOME SCREEN WIDGET PREVIEW & PIN
        // -------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .shadow(2.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color(0x10000000))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Home Screen Widget (\"winget\")",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Compact vs Standard toggle
                    Surface(
                        onClick = { isCompactWidgetPreview = !isCompactWidgetPreview },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.testTag("toggle_widget_mode")
                    ) {
                        Text(
                            text = if (isCompactWidgetPreview) "4x2 Standard" else "2x1 Compact",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Pastel Color Scheme Bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Theme:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PastelPalette.entries.forEach { palette ->
                        val isSelected = selectedPalette == palette
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 22.dp else 18.dp)
                                .clip(CircleShape)
                                .background(palette.accentDot)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedPalette = palette }
                        )
                    }
                }

                // LIVE WIDGET CANVAS PREVIEW
                if (!isCompactWidgetPreview) {
                    StandardWidgetPreview(
                        effectiveVolume = effectiveVolume,
                        isMuted = volumeState.isMuted,
                        palette = selectedPalette,
                        onVolumeChange = onSetVolume,
                        onToggleMute = onToggleMute
                    )
                } else {
                    CompactWidgetPreview(
                        effectiveVolume = effectiveVolume,
                        isMuted = volumeState.isMuted,
                        palette = selectedPalette,
                        onVolumeChange = onSetVolume,
                        onToggleMute = onToggleMute
                    )
                }

                // Actions: Pin to Home Screen & Help
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val pinned = VolumeAppWidgetProvider.pinWidgetToHomeScreen(context)
                            if (pinned) {
                                Toast.makeText(context, "Pin widget prompt opened! Check your home screen launcher.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Long-press your home screen -> Widgets -> 'Volume Slider' to place it.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_widget_to_home_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pin to Home Screen", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { showInstructionsDialog = !showInstructionsDialog },
                        shape = CircleShape,
                        modifier = Modifier.testTag("widget_help_button")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                if (showInstructionsDialog) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "How to add the Widget manually:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "1. Go to your Android Home Screen.\n2. Touch and hold any empty space.\n3. Tap 'Widgets' from the menu.\n4. Scroll to 'Volume Slider' and drag the widget onto your home screen.\n5. Adjust volume with the + / - buttons or tap presets anytime!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // SECTION 2: SYSTEM QUICK SETTINGS TILE
        // -------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .shadow(2.dp, shape = RoundedCornerShape(28.dp), ambientColor = Color(0x10000000))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Quick Settings Tile",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (volumeState.isMuted) "Inactive" else "Active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (volumeState.isMuted) Color(0xFFF43F5E) else Color(0xFF10B981)
                    )
                }

                Text(
                    text = "Appears in your Android Notification Quick Settings shade. Tap the tile to step through volume levels or toggle mute.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Simulated Quick Settings Shade
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1C1924)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "QUICK SETTINGS SHADE PREVIEW",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Dummy Tile: Wi-Fi
                            ShadeDummyTile(
                                icon = Icons.Default.Wifi,
                                title = "Wi-Fi",
                                subtitle = "Home",
                                isActive = true,
                                modifier = Modifier.weight(1f)
                            )

                            // Our Volume Tile!
                            val isQsActive = !volumeState.isMuted && effectiveVolume > 0f
                            val qsBgColor by animateColorAsState(
                                targetValue = if (isQsActive) selectedPalette.accentDot else Color(0xFF2E2A38),
                                label = "qs_bg"
                            )
                            val qsContentColor by animateColorAsState(
                                targetValue = if (isQsActive) Color(0xFF2E2836) else Color.White.copy(alpha = 0.7f),
                                label = "qs_content"
                            )

                            Surface(
                                onClick = {
                                    VolumeManager.cycleStep(context)
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = qsBgColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .testTag("interactive_qs_tile_preview")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            volumeState.isMuted || effectiveVolume == 0f -> Icons.AutoMirrored.Filled.VolumeOff
                                            effectiveVolume <= 33f -> Icons.AutoMirrored.Filled.VolumeMute
                                            effectiveVolume <= 66f -> Icons.AutoMirrored.Filled.VolumeDown
                                            else -> Icons.AutoMirrored.Filled.VolumeUp
                                        },
                                        contentDescription = null,
                                        tint = qsContentColor,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Column {
                                        Text(
                                            text = "${effectiveVolume.roundToInt()}%",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = qsContentColor
                                        )
                                        Text(
                                            text = if (volumeState.isMuted) "Muted" else "Media Vol",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = qsContentColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Dummy Tile: Bluetooth
                            ShadeDummyTile(
                                icon = Icons.Default.Bluetooth,
                                title = "Bluetooth",
                                subtitle = "On",
                                isActive = true,
                                modifier = Modifier.weight(1f)
                            )

                            // Dummy Tile: DND
                            ShadeDummyTile(
                                icon = Icons.Default.DoNotDisturb,
                                title = "Do Not Disturb",
                                subtitle = "Off",
                                isActive = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Add to Quick Settings Button
                Button(
                    onClick = {
                        VolumeTileService.requestAddTileToQuickSettings(context) { added ->
                            if (added) {
                                Toast.makeText(context, "Volume Tile added to Quick Settings!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Pull down your notification bar -> tap Edit (pencil) -> drag 'Volume' into active tiles.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_qs_tile_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Tile to Android Quick Settings", style = MaterialTheme.typography.labelMedium)
                }

                // ---------------------------------------------------------
                // Quick Settings Tile Behavior Settings (Option 1 & Option 2)
                // ---------------------------------------------------------
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Tile Click Behavior Mode",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        var currentAction by remember { mutableStateOf(VolumeManager.tileClickAction) }

                        TileClickAction.entries.forEach { action ->
                            val isSelected = currentAction == action
                            Surface(
                                onClick = {
                                    VolumeManager.tileClickAction = action
                                    currentAction = action
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                                    )
                                    Column {
                                        Text(
                                            text = action.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = action.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Stream Selection
                        Text(
                            text = "Target Audio Stream",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AudioStreamType.entries.forEach { streamType ->
                                val isSelected = VolumeManager.targetStream == streamType.streamConstant
                                Surface(
                                    onClick = {
                                        VolumeManager.setTargetStreamType(context, streamType.streamConstant)
                                    },
                                    shape = CircleShape,
                                    color = if (isSelected) selectedPalette.accentDot else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = streamType.label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 10.sp
                                            ),
                                            color = if (isSelected) Color(0xFF2E2836) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Direct Controller Actions
                        Text(
                            text = "Direct Controller Actions",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Option 1 Button: Launch System Volume Panel
                        Button(
                            onClick = {
                                VolumeManager.openSystemVolumePanel(context)
                                Toast.makeText(context, "Showing standard Android volume slider panel (FLAG_SHOW_UI)!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedPalette.accentDot,
                                contentColor = Color(0xFF2E2836)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth().testTag("open_system_volume_panel_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open System Volume Panel (Option 1)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        // Option 2 Buttons: Step Raise / Lower
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { VolumeManager.stepLower(context) },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f).testTag("step_volume_down_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Step Down (-1)", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { VolumeManager.stepRaise(context) },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f).testTag("step_volume_up_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Step Up (+1)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // SECTION 3: SYSTEM AUDIO TELEMETRY & FEEDBACK
        // -------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Stream Telemetry",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        onClick = { VolumeManager.playFeedback(context) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("test_sound_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Test Click", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STREAM_MUSIC index: ${volumeState.currentSystemVolume} / ${volumeState.maxSystemVolume}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Mute: ${if (volumeState.isMuted) "YES" else "NO"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (volumeState.isMuted) Color(0xFFF43F5E) else Color(0xFF10B981)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ShadeDummyTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) Color(0xFF383344) else Color(0xFF262230),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color(0xFFDDD6FE) else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Interactive Preview of 4x2 Standard Home Screen Widget
 */
@Composable
fun StandardWidgetPreview(
    effectiveVolume: Float,
    isMuted: Boolean,
    palette: PastelPalette,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFDFBF7),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.accentDot.copy(alpha = 0.7f)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("standard_widget_preview")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Widget Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isMuted || effectiveVolume == 0f -> Icons.AutoMirrored.Filled.VolumeOff
                            effectiveVolume <= 33f -> Icons.AutoMirrored.Filled.VolumeMute
                            effectiveVolume <= 66f -> Icons.AutoMirrored.Filled.VolumeDown
                            else -> Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = null,
                        tint = if (isMuted) Color(0xFFF43F5E) else Color(0xFF2E2836),
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Text(
                            text = "Pastel Volume",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E2836)
                        )
                        Text(
                            text = if (isMuted) "Muted • Tap to unmute" else "Media Sound",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Color(0xFF7A7285)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = palette.accentDot.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${effectiveVolume.roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF2E2836),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Widget Interactive Progress Bar
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEAE5DC))
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
            ) {
                val fillWidth = maxWidth * (effectiveVolume / 100f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(fillWidth)
                        .background(Brush.horizontalGradient(palette.colors))
                )
            }

            // Widget Controls Row (+, -, Mute, Presets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Mute
                Surface(
                    onClick = onToggleMute,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isMuted) Color(0xFFFFE4E6) else Color(0xFFEDE9FE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentDot.copy(alpha = 0.8f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute",
                            tint = if (isMuted) Color(0xFFF43F5E) else Color(0xFF2E2836),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Minus 10%
                Surface(
                    onClick = { onVolumeChange((effectiveVolume - 10f).coerceAtLeast(0f)) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEDE9FE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentDot.copy(alpha = 0.8f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Down",
                            tint = Color(0xFF2E2836),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Plus 10%
                Surface(
                    onClick = { onVolumeChange((effectiveVolume + 10f).coerceAtMost(100f)) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEDE9FE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentDot.copy(alpha = 0.8f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Up",
                            tint = Color(0xFF2E2836),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Presets: 25%, 70%, 100%
                listOf(25, 70, 100).forEach { p ->
                    val isSelected = effectiveVolume.roundToInt() == p
                    Surface(
                        onClick = { onVolumeChange(p.toFloat()) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) palette.accentDot else Color(0xFFF3EFE8),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) palette.accentDot else Color(0xFFE0D7CC)
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .width(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (p == 100) "MAX" else "$p%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color(0xFF2E2836)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Preview of 2x1 Compact Pill Home Screen Widget
 */
@Composable
fun CompactWidgetPreview(
    effectiveVolume: Float,
    isMuted: Boolean,
    palette: PastelPalette,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFDFBF7),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.accentDot.copy(alpha = 0.7f)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compact_widget_preview")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onToggleMute,
                shape = CircleShape,
                color = palette.accentDot.copy(alpha = 0.4f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (isMuted) Color(0xFFF43F5E) else Color(0xFF2E2836),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAE5DC))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            onVolumeChange(fraction * 100f)
                        }
                    }
            ) {
                val fillWidth = maxWidth * (effectiveVolume / 100f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(fillWidth)
                        .background(Brush.horizontalGradient(palette.colors))
                )
            }

            Text(
                text = "${effectiveVolume.roundToInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2E2836)
            )
        }
    }
}
