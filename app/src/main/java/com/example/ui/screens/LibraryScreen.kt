package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.TrackItem
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filter by viewModel.libraryFilter.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()

    val likedSongs by viewModel.likedSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val localSongs by viewModel.localSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val currentSong by viewModel.controller.currentSong.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val filters = listOf("All", "Playlists", "Liked", "Downloaded", "Local Files")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                viewModel.scanLocalFiles()
                delay(600)
                isRefreshing = false
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = it,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = CardSurface,
                color = SpotifyGreen
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Library Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(onClick = { viewModel.scanLocalFiles() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Local Files",
                        tint = TextPrimary
                    )
                }

                IconButton(onClick = { viewModel.toggleGridView() }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Toggle View",
                        tint = TextPrimary
                    )
                }
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { f ->
                val selected = f == filter
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setLibraryFilter(f) },
                    label = { Text(f, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = CardSurfaceVariant,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Pinned Cards: Liked Songs & Offline Downloads
            if (filter == "All" || filter == "Liked") {
                item {
                    PinnedLibraryCard(
                        title = "Liked Songs",
                        subtitle = "${likedSongs.size} tracks saved",
                        gradient = Brush.linearGradient(listOf(Color(0xFF450AF5), Color(0xFFC4EFD9))),
                        icon = Icons.Default.Favorite,
                        onClick = {
                            if (likedSongs.isNotEmpty()) {
                                viewModel.playSong(likedSongs.first(), likedSongs)
                            }
                        }
                    )
                }
            }

            if (filter == "All" || filter == "Downloaded") {
                item {
                    PinnedLibraryCard(
                        title = "Offline Downloads",
                        subtitle = "${downloadedSongs.size} songs cached for offline",
                        gradient = Brush.linearGradient(listOf(Color(0xFF0F9D58), Color(0xFF004D40))),
                        icon = Icons.Default.Download,
                        onClick = {
                            if (downloadedSongs.isNotEmpty()) {
                                viewModel.playSong(downloadedSongs.first(), downloadedSongs)
                            }
                        }
                    )
                }
            }

            if (filter == "All" || filter == "Local Files") {
                item {
                    PinnedLibraryCard(
                        title = "Local Device Audio",
                        subtitle = "${localSongs.size} files (.mp3, .flac, .m4a)",
                        gradient = Brush.linearGradient(listOf(Color(0xFFFF6D00), Color(0xFFDD2C00))),
                        icon = Icons.Default.SdStorage,
                        onClick = {
                            viewModel.setTab(ScreenTab.LOCAL_DEVICE)
                        }
                    )
                }
            }

            // Custom Playlists Section
            if (filter == "All" || filter == "Playlists") {
                item {
                    Text(
                        text = "Playlists",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(playlists) { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = pl.artworkUri,
                            contentDescription = pl.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = pl.name,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${pl.description} • ${pl.songCount} songs",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Display Tracks for selected sub-filter
            val displayedSongs = when (filter) {
                "Liked" -> likedSongs
                "Downloaded" -> downloadedSongs
                "Local Files" -> localSongs
                else -> emptyList()
            }

            if (displayedSongs.isNotEmpty()) {
                item {
                    Text(
                        text = "$filter Tracks (${displayedSongs.size})",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                items(displayedSongs) { song ->
                    TrackItem(
                        song = song,
                        isPlaying = song.id == currentSong?.id,
                        onClick = { viewModel.playSong(song, displayedSongs) },
                        onLikeToggle = {
                            viewModel.playSong(song, displayedSongs)
                            viewModel.toggleLikeCurrentSong()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedLibraryCard(
    title: String,
    subtitle: String,
    gradient: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardSurface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
