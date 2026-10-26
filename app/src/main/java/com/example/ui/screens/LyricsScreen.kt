package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LyricLine
import com.example.data.model.LyricsDisplayMode
import com.example.ui.MainViewModel
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LyricsScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lyrics by viewModel.lyrics.collectAsState()
    val lyricsMode by viewModel.lyricsMode.collectAsState()
    val isLoading by viewModel.isLoadingLyrics.collectAsState()
    val currentPosition by viewModel.controller.currentPosition.collectAsState()
    val duration by viewModel.controller.duration.collectAsState()
    val isPlaying by viewModel.controller.isPlaying.collectAsState()
    val currentSong by viewModel.controller.currentSong.collectAsState()

    var showShareCard by remember { mutableStateOf(false) }
    var selectedLyricForShare by remember { mutableStateOf<LyricLine?>(null) }
    var lastUserInteractionTime by remember { mutableLongStateOf(0L) }
    var isUserScrolling by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val isDragged by listState.interactionSource.collectIsDraggedAsState()
    val coroutineScope = rememberCoroutineScope()

    // Pause auto-scroll on user drag
    LaunchedEffect(isDragged) {
        if (isDragged) {
            isUserScrolling = true
            lastUserInteractionTime = System.currentTimeMillis()
        }
    }

    // Auto-resume sync after 3.5 seconds
    LaunchedEffect(lastUserInteractionTime) {
        if (lastUserInteractionTime > 0L) {
            delay(3500)
            isUserScrolling = false
        }
    }

    // Determine current active lyric line based on music playback timestamp
    val activeIndex by remember(lyrics, currentPosition) {
        derivedStateOf {
            if (lyrics.isEmpty()) -1
            else {
                val idx = lyrics.indexOfLast { it.timestampMs <= currentPosition }
                if (idx >= 0) idx else 0
            }
        }
    }

    // Centered auto-scroll keeping active line in middle
    LaunchedEffect(activeIndex, isUserScrolling) {
        if (!isUserScrolling && lyrics.isNotEmpty() && activeIndex in lyrics.indices) {
            try {
                listState.animateScrollToItem(
                    index = activeIndex,
                    scrollOffset = -220
                )
            } catch (_: Exception) {}
        }
    }

    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF231633),
                        ObsidianBlack,
                        Color.Black
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Synced Lyrics",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SpotifyGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🇮🇳 HINDI • ENG",
                                color = SpotifyGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${currentSong?.title ?: ""} • ${currentSong?.artist ?: ""}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Lyrics",
                        tint = TextPrimary
                    )
                }
            }

            // Real-time Song Progress Bar
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SpotifyGreen,
                trackColor = CardSurfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // Indian Language & Mode Switcher Bar
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LyricsDisplayMode.values().forEach { mode ->
                    val isSelected = lyricsMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SpotifyGreen else CardSurface)
                            .clickable { viewModel.setLyricsMode(mode) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mode == LyricsDisplayMode.BILINGUAL) {
                                Text("🇮🇳 ", fontSize = 11.sp)
                            }
                            Text(
                                text = mode.title,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SpotifyGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Synchronizing Hindi & English lyrics...", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else if (lyrics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No synced lyrics found for this song", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    itemsIndexed(lyrics, key = { index, line -> "$index-${line.timestampMs}" }) { index, line ->
                        val isActive = index == activeIndex
                        val isPast = index < activeIndex

                        val primaryText = line.getPrimaryText(lyricsMode)
                        val secondaryText = line.getSecondaryText(lyricsMode)

                        val primaryColor by animateColorAsState(
                            targetValue = when {
                                isActive -> Color.White
                                isPast -> Color(0xFFD1D5DB)
                                else -> Color(0xFF6B7280)
                            },
                            animationSpec = tween(300),
                            label = "primaryTextColor"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    // Interactive Karaoke: Tap any line to seek instantly!
                                    viewModel.controller.seekTo(line.timestampMs)
                                    isUserScrolling = false
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active vertical green indicator pill
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(if (secondaryText != null) 44.dp else 28.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(SpotifyGreen)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                Column {
                                    Text(
                                        text = primaryText,
                                        fontSize = if (isActive) 24.sp else 19.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = primaryColor,
                                        lineHeight = if (isActive) 32.sp else 26.sp
                                    )

                                    if (secondaryText != null) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = secondaryText,
                                            fontSize = if (isActive) 15.sp else 13.sp,
                                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isActive) SpotifyGreen else Color(0xFF9CA3AF),
                                            lineHeight = if (isActive) 21.sp else 18.sp
                                        )
                                    }
                                }
                            }

                            if (isActive) {
                                IconButton(
                                    onClick = {
                                        selectedLyricForShare = line
                                        showShareCard = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Lyric Card",
                                        tint = SpotifyGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Mini Controls Bar (allows pausing/skipping without closing lyrics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.controller.skipPrevious() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen)
                        .clickable { viewModel.controller.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = { viewModel.controller.skipNext() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Floating "Sync with Song" Pill when user scrolls manually
        AnimatedVisibility(
            visible = isUserScrolling && lyrics.isNotEmpty() && activeIndex in lyrics.indices,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        ) {
            Button(
                onClick = {
                    isUserScrolling = false
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = activeIndex,
                            scrollOffset = -220
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sync with Music",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Spotify-style Lyric Card Generator Modal with Hindi & English Support
        if (showShareCard && selectedLyricForShare != null) {
            val line = selectedLyricForShare!!
            val sharePrimary = line.getPrimaryText(lyricsMode)
            val shareSecondary = line.getSecondaryText(lyricsMode)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { showShareCard = false }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1DB954), Color(0xFF191414))
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ADI MUSIC 🇮🇳 HINDI & ENGLISH LYRICS",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "\"$sharePrimary\"",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )

                    if (shareSecondary != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = shareSecondary,
                            color = Color(0xFFE5E7EB),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = currentSong?.title ?: "",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentSong?.artist ?: "",
                        color = Color(0xFFE5E7EB),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
