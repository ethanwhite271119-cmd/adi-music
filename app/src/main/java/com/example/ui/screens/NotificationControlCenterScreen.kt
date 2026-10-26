package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.service.CompactPreset
import com.example.service.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun NotificationControlCenterScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSong by viewModel.controller.currentSong.collectAsState()
    val isPlaying by viewModel.controller.isPlaying.collectAsState()
    val currentPosition by viewModel.controller.currentPosition.collectAsState()
    val duration by viewModel.controller.duration.collectAsState()
    val config by viewModel.notificationConfig.collectAsState()

    val hasPermission = NotificationHelper.hasNotificationPermission(context)
    val areNotificationsEnabled = NotificationHelper.areNotificationsEnabledInSystem(context)

    BackHandler { onBackClick() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Control Center",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Android System Shade & Lockscreen Media",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hasPermission && areNotificationsEnabled) SpotifyGreen.copy(alpha = 0.2f) else Color(0xFFFFB74D).copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (hasPermission && areNotificationsEnabled) SpotifyGreen else Color(0xFFFFB74D))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasPermission && areNotificationsEnabled) "Live Connected" else "Needs Setup",
                            color = if (hasPermission && areNotificationsEnabled) SpotifyGreen else Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Android Media Notification Preview Widget
        item {
            SectionTitle("Live Interactive Notification Preview")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF282828),
                                    Color(0xFF1E1E1E),
                                    Color(0xFF121212)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        // System Shade Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(SpotifyGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Adi Music",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = " • Quick Settings",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPlaying) "Playing" else "Paused",
                                    color = if (isPlaying) SpotifyGreen else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Artwork + Title + Artist Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val artworkUrl = currentSong?.artworkUri ?: ""
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (artworkUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(artworkUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Notification Artwork",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentSong?.title ?: "No Song Selected",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentSong?.artist ?: "Select a song from Home or Search",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentSong?.album ?: "Adi Music Engine",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            if (config.showLikeButton && currentSong != null) {
                                IconButton(onClick = { viewModel.toggleLikeCurrentSong() }) {
                                    Icon(
                                        imageVector = if (currentSong?.isLiked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (currentSong?.isLiked == true) SpotifyGreen else TextSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Live Interactive Seekbar
                        if (config.showSeekbar) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val effectiveDur = if (duration > 0) duration else (currentSong?.durationMs?.takeIf { it > 0 } ?: 0L)
                            val progress = if (effectiveDur > 0) (currentPosition.toFloat() / effectiveDur.toFloat()).coerceIn(0f, 1f) else 0f

                            Slider(
                                value = progress,
                                onValueChange = { frac ->
                                    if (effectiveDur > 0) {
                                        viewModel.controller.seekTo((frac * effectiveDur).toLong())
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = SpotifyGreen,
                                    activeTrackColor = SpotifyGreen,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = formatMs(currentPosition), color = TextMuted, fontSize = 11.sp)
                                Text(
                                    text = if (effectiveDur > 0L) formatMs(effectiveDur) else "LIVE",
                                    color = if (effectiveDur <= 0L) SpotifyGreen else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (effectiveDur <= 0L) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive Media Notification Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (config.compactPreset) {
                                CompactPreset.STANDARD -> {
                                    IconButton(onClick = { viewModel.controller.skipPrevious() }) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous Track",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    IconButton(onClick = {
                                        viewModel.controller.seekTo((currentPosition - 10000).coerceAtLeast(0L))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.FastRewind,
                                            contentDescription = "Rewind 10s",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(SpotifyGreen)
                                            .clickable { viewModel.controller.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    IconButton(onClick = {
                                        viewModel.controller.seekTo((currentPosition + 30000).coerceAtMost(duration))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Forward 30s",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.controller.skipNext() }) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next Track",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                CompactPreset.FAVORITE_FIRST -> {
                                    IconButton(onClick = { viewModel.toggleLikeCurrentSong() }) {
                                        Icon(
                                            imageVector = if (currentSong?.isLiked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (currentSong?.isLiked == true) SpotifyGreen else TextSecondary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.controller.skipPrevious() }) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous Track",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(SpotifyGreen)
                                            .clickable { viewModel.controller.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.controller.skipNext() }) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next Track",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                CompactPreset.SEEK_FOCUSED -> {
                                    IconButton(onClick = {
                                        viewModel.controller.seekTo((currentPosition - 10000).coerceAtLeast(0L))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.FastRewind,
                                            contentDescription = "Rewind 10s",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(SpotifyGreen)
                                            .clickable { viewModel.controller.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }

                                    IconButton(onClick = {
                                        viewModel.controller.seekTo((currentPosition + 30000).coerceAtMost(duration))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Forward 30s",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "⚡ Interactive live simulation — buttons directly control active playback and match your device notification drawer.",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // Quick Notification Actions
        item {
            SectionTitle("Quick Control Actions")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Refresh Notification Button
                Button(
                    onClick = {
                        NotificationHelper.triggerNotificationRefresh(context)
                        Toast.makeText(context, "System notification refreshed in shade", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SpotifyGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Push Notification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Send Test Notification
                Button(
                    onClick = {
                        NotificationHelper.sendTestNotification(
                            context = context,
                            title = currentSong?.title ?: "Adi Music Playback",
                            artist = currentSong?.artist ?: "Notification Control Center"
                        )
                        Toast.makeText(context, "Test notification posted to shade", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardSurfaceVariant,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SpotifyGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Notification", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Android System Settings Shortcut Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { NotificationHelper.openNotificationSettings(context) },
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CardSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Device App Notification Settings",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Configure lockscreen banners, sound, and quick settings carousel",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                }
            }
        }

        // Notification Customization Toggles
        item {
            SectionTitle("Notification Display & Feature Preferences")

            SettingToggleItem(
                title = "Lockscreen Media Artwork",
                subtitle = "Display full album cover backdrop on device lockscreen",
                checked = config.showArtworkOnLockscreen,
                onCheckedChange = { newValue ->
                    viewModel.updateNotificationConfig { it.copy(showArtworkOnLockscreen = newValue) }
                }
            )

            SettingToggleItem(
                title = "Live Progress Seekbar",
                subtitle = "Enables scrubbing song progress from notification drawer",
                checked = config.showSeekbar,
                onCheckedChange = { newValue ->
                    viewModel.updateNotificationConfig { it.copy(showSeekbar = newValue) }
                }
            )

            SettingToggleItem(
                title = "Quick Favorite Button",
                subtitle = "Show Heart icon in notification menu to like tracks instantly",
                checked = config.showLikeButton,
                onCheckedChange = { newValue ->
                    viewModel.updateNotificationConfig { it.copy(showLikeButton = newValue) }
                }
            )

            SettingToggleItem(
                title = "Persistent Background Notification",
                subtitle = "Keep media player notification in drawer even when playback is paused",
                checked = config.persistentNotification,
                onCheckedChange = { newValue ->
                    viewModel.updateNotificationConfig { it.copy(persistentNotification = newValue) }
                }
            )
        }

        // Compact Drawer Preset Selection
        item {
            SectionTitle("Compact Mode Action Layout")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactPreset.values().forEach { preset ->
                    val isSelected = config.compactPreset == preset
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) SpotifyGreen else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                viewModel.updateNotificationConfig { it.copy(compactPreset = preset) }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CardSurfaceVariant else CardSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.label,
                                    color = if (isSelected) SpotifyGreen else TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = preset.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardSurface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = preset.actionsSummary,
                                    color = if (isSelected) SpotifyGreen else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // System Diagnostic & Troubleshooting Guide
        item {
            SectionTitle("System Diagnostic & Device Tips")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasPermission) SpotifyGreen else Color(0xFFFFB74D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasPermission) "Notification Permission Granted" else "Notification Permission Needed",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Samsung OneUI: Ensure 'Pin media player' is checked under Quick Panel layout.\n" +
                                "• Xiaomi MIUI/HyperOS: Allow Autostart and enable 'Lock screen notifications'.\n" +
                                "• OnePlus OxygenOS: Turn off 'Aggressive Battery Optimization' to ensure music does not stop when screen turns off.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
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

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
