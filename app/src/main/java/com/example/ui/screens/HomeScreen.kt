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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.AlbumCard
import com.example.ui.components.QuickAccessCard
import com.example.ui.components.TrackItem
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.streamCatalog.collectAsState()
    val playlists by viewModel.trendingPlaylists.collectAsState()
    val homeCategory by viewModel.homeCategory.collectAsState()
    val isLoading by viewModel.isHomeLoading.collectAsState()
    val currentSong by viewModel.controller.currentSong.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayedDistinct.collectAsState()

    val greeting = rememberGreeting()
    val categories = listOf("All", "🇮🇳 Indian / Hindi", "Music", "Relax", "Workout", "Gaming/Phonk")
    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadHomeData() },
        state = pullToRefreshState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isLoading,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = CardSurface,
                color = SpotifyGreen
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
        // Top Greeting Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = greeting,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = { viewModel.setTab(ScreenTab.HISTORY) }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Previously Listened",
                            tint = TextPrimary
                        )
                    }

                    IconButton(onClick = { viewModel.setTab(ScreenTab.WRAPPED) }) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Adi Wrapped",
                            tint = TextPrimary
                        )
                    }

                    IconButton(onClick = { viewModel.setTab(ScreenTab.NOTIFICATION_CENTER) }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification Control Center",
                            tint = TextPrimary
                        )
                    }

                    IconButton(onClick = { viewModel.setTab(ScreenTab.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat == homeCategory
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setHomeCategory(cat) },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
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
        }

        // Previously Listened Shelf
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Previously Listened",
                    actionText = "See All",
                    onActionClick = { viewModel.setTab(ScreenTab.HISTORY) }
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recentlyPlayed, key = { it.id }) { song ->
                        AlbumCard(
                            song = song,
                            onClick = { viewModel.playSong(song, recentlyPlayed) }
                        )
                    }
                }
            }
        }

        if (isLoading && songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SpotifyGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading real-time charts & streams...",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            // Real Spotify 2x3 Quick-Access Grid
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickPicks = songs.take(6)
                    for (i in 0 until quickPicks.size step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val first = quickPicks[i]
                            QuickAccessCard(
                                title = first.title,
                                artworkUri = first.artworkUri,
                                onClick = { viewModel.playSong(first, songs) },
                                modifier = Modifier.weight(1f)
                            )

                            if (i + 1 < quickPicks.size) {
                                val second = quickPicks[i + 1]
                                QuickAccessCard(
                                title = second.title,
                                artworkUri = second.artworkUri,
                                onClick = { viewModel.playSong(second, songs) },
                                modifier = Modifier.weight(1f)
                            )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Real Horizontal Carousel: Quick Picks for You
            if (songs.isNotEmpty()) {
                item {
                    SectionHeader(title = "Quick Picks for You")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(songs) { song ->
                            AlbumCard(
                                song = song,
                                onClick = { viewModel.playSong(song, songs) }
                            )
                        }
                    }
                }
            }

            // Dedicated Indian & Bollywood Hits Carousel with Synced Hindi & English Lyrics
            val indianSongs = songs.filter {
                com.example.data.remote.IndianLyricsHelper.isIndianMusic(it.title, it.artist)
            }
            if (indianSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "🇮🇳 Trending Bollywood & Indian Hits",
                        actionText = "Dual Lyrics",
                        onActionClick = {
                            viewModel.playSong(indianSongs.first(), indianSongs)
                            viewModel.setLyricsVisible(true)
                        }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(indianSongs, key = { it.id }) { song ->
                            AlbumCard(
                                song = song,
                                onClick = { viewModel.playSong(song, indianSongs) }
                            )
                        }
                    }
                }
            }

            // Real Horizontal Carousel: Trending Charts & Top 50
            if (songs.size > 5) {
                item {
                    SectionHeader(title = "Trending Charts & Top 50")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(songs.drop(3)) { song ->
                            AlbumCard(
                                song = song,
                                onClick = { viewModel.playSong(song, songs) }
                            )
                        }
                    }
                }
            }

            // Real Horizontal Carousel: Recommended Artists (from live API data)
            val artists = songs.map { it.artist }.distinct()
            if (artists.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recommended Artists")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(artists) { artist ->
                            val matchingSong = songs.find { it.artist == artist }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(96.dp)
                                    .clickable {
                                        if (matchingSong != null) viewModel.playSong(matchingSong, songs)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                        .background(CardSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = artist.take(1).uppercase(),
                                        color = SpotifyGreen,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = artist,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Real Vertical List: Heavy Rotation Tracks
            if (songs.isNotEmpty()) {
                item {
                    SectionHeader(title = "Heavy Rotation")
                }

                items(songs) { song ->
                    TrackItem(
                        song = song,
                        isPlaying = song.id == currentSong?.id,
                        onClick = { viewModel.playSong(song, songs) },
                        onLikeToggle = {
                            viewModel.playSong(song, songs)
                            viewModel.toggleLikeCurrentSong()
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = SpotifyGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
