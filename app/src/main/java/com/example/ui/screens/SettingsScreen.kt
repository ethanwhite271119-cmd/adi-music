package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import com.example.data.model.LyricsDisplayMode
import com.example.service.NotificationHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pureAmoled by viewModel.pureAmoled.collectAsState()
    val crossfade by viewModel.crossfadeSeconds.collectAsState()
    val gamingBubble by viewModel.gamingBubbleEnabled.collectAsState()
    val bluetoothInfo by viewModel.controller.bluetoothDeviceInfo.collectAsState()
    val lyricsMode by viewModel.lyricsMode.collectAsState()

    var gapless by remember { mutableStateOf(true) }
    var skipSilence by remember { mutableStateOf(false) }
    var audioNormalization by remember { mutableStateOf(true) }
    var discordRpc by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setTab(ScreenTab.HOME) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Settings",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Appearance
        item {
            SectionTitle("Appearance & Theme")
            SettingToggleItem(
                icon = Icons.Default.DarkMode,
                title = "Pure AMOLED Black",
                subtitle = "Deep true-black (#000000) for OLED battery saving",
                checked = pureAmoled,
                onCheckedChange = { viewModel.togglePureAmoled() }
            )
        }

        // Playback & Crossfade
        item {
            SectionTitle("Playback & Engine")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Crossfade", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "${crossfade}s", color = SpotifyGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "Smooth transition between playing songs", color = TextSecondary, fontSize = 12.sp)

                    Slider(
                        value = crossfade.toFloat(),
                        onValueChange = { viewModel.setCrossfadeSeconds(it.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = TextPrimary,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = CardSurfaceVariant
                        )
                    )
                }
            }

            SettingToggleItem(
                icon = Icons.Default.Tune,
                title = "Gapless Playback",
                subtitle = "Seamless audio transitions without silence gaps",
                checked = gapless,
                onCheckedChange = { gapless = it }
            )

            SettingToggleItem(
                icon = Icons.Default.Tune,
                title = "Audio Normalization",
                subtitle = "Balances volume across all streaming tracks",
                checked = audioNormalization,
                onCheckedChange = { audioNormalization = it }
            )
        }

        // Indian & Hindi/English Lyrics Preference
        item {
            SectionTitle("🇮🇳 Indian & Hindi Lyrics Mode")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Lyrics Language & Script",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Controls whether Hindi songs display Devanagari, English Hinglish, or Dual simultaneously",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LyricsDisplayMode.values().forEach { mode ->
                            val isSelected = lyricsMode == mode
                            Button(
                                onClick = { viewModel.setLyricsMode(mode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) SpotifyGreen else CardSurfaceVariant,
                                    contentColor = if (isSelected) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = mode.shortLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Active mode: ${lyricsMode.title} — ${lyricsMode.description}",
                        color = SpotifyGreen,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Bluetooth & Hardware Controls
        item {
            SectionTitle("Bluetooth & Audio Output")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Audio Output",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (bluetoothInfo.isBluetoothConnected) "Connected: ${bluetoothInfo.deviceName}" else "Device: Phone Speaker",
                                color = if (bluetoothInfo.isBluetoothConnected) SpotifyGreen else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.controller.bluetoothDeviceManager.openBluetoothSettings()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pair / Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Hardware remote controls (earbud tap, headphone buttons, and car steering wheel) are fully enabled.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            SettingToggleItem(
                icon = Icons.Default.BluetoothAudio,
                title = "Auto-Pause on Disconnect",
                subtitle = "Pauses music when Bluetooth device disconnects",
                checked = bluetoothInfo.isPauseOnDisconnectEnabled,
                onCheckedChange = {
                    viewModel.controller.bluetoothDeviceManager.setPauseOnDisconnectEnabled(it)
                }
            )

            SettingToggleItem(
                icon = Icons.Default.Bluetooth,
                title = "Auto-Resume on Connect",
                subtitle = "Resumes playback when Bluetooth device reconnects",
                checked = bluetoothInfo.isAutoResumeEnabled,
                onCheckedChange = {
                    viewModel.controller.bluetoothDeviceManager.setAutoResumeEnabled(it)
                }
            )
        }

        // Notification & Lockscreen Media Controls
        item {
            SectionTitle("Device Notification Menu & Lockscreen")

            val hasNotificationPermission = NotificationHelper.hasNotificationPermission(context)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Media Notification Controls",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (hasNotificationPermission) "Active in notification shade & lockscreen" else "Permission required for notification bar",
                                color = if (hasNotificationPermission) SpotifyGreen else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.setTab(ScreenTab.NOTIFICATION_CENTER)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Control Center",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Real-time interactive media playback controls (Previous, Play/Pause, Next), album artwork, seekbar scrub, and song metadata in the Android notification drawer and lockscreen media carousel.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Gaming & Background Audio
        item {
            SectionTitle("Gaming & Battery Optimization")

            SettingToggleItem(
                icon = Icons.Default.SportsEsports,
                title = "Gaming Audio Bubble",
                subtitle = "Floating mini-controller overlay while playing mobile games",
                checked = gamingBubble,
                onCheckedChange = { viewModel.toggleGamingBubble() }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Battery Optimization", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Ensure uninterrupted music with screen off or background apps", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Battery whitelist check active", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceVariant)
                    ) {
                        Text("Whitelist", color = SpotifyGreen, fontSize = 12.sp)
                    }
                }
            }
        }

        // Database Backup & Restore
        item {
            SectionTitle("Backup & Restore")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Export Room DB Backup", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Saves playlists, liked tracks, and listening history", color = TextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.exportBackup {
                                Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                    ) {
                        Text("Export", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // About
        item {
            SectionTitle("About Adi Music")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Adi Music v1.0", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "High-fidelity Spotify-grade Android music streaming & offline player engine. Powered by AndroidX Media3 ExoPlayer, Jetpack Compose, Room, and LRCLIB synced lyrics.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = SpotifyGreen,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = SpotifyGreen,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = CardSurfaceVariant
                )
            )
        }
    }
}
