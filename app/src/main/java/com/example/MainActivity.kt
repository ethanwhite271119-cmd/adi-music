package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.MiniPlayer
import com.example.ui.components.QueueSheet
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LocalSongsScreen
import com.example.ui.screens.LyricsScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsWrappedScreen
import com.example.ui.viewmodel.LocalSongsViewModel
import com.example.ui.theme.AdiMusicTheme
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val localSongsViewModel: LocalSongsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val pureAmoled by viewModel.pureAmoled.collectAsState()

            AdiMusicTheme(pureAmoled = pureAmoled) {
                AdiMusicApp(
                    viewModel = viewModel,
                    localSongsViewModel = localSongsViewModel
                )
            }
        }
    }
}

@Composable
fun AdiMusicApp(
    viewModel: MainViewModel,
    localSongsViewModel: LocalSongsViewModel
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentSong by viewModel.controller.currentSong.collectAsState()
    val isPlaying by viewModel.controller.isPlaying.collectAsState()
    val currentPos by viewModel.controller.currentPosition.collectAsState()
    val duration by viewModel.controller.duration.collectAsState()
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsState()
    val isLyricsVisible by viewModel.isLyricsVisible.collectAsState()
    val isEqualizerVisible by viewModel.isEqualizerVisible.collectAsState()
    val isQueueSheetVisible by viewModel.isQueueSheetVisible.collectAsState()
    val queue by viewModel.controller.queue.collectAsState()

    // Permissions check
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        ) {
            viewModel.scanLocalFiles()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val permissionsToRequest = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Handle Back Press gracefully across sheets and tabs
    BackHandler(enabled = isLyricsVisible || isEqualizerVisible || isFullPlayerVisible || currentTab != ScreenTab.HOME) {
        when {
            isLyricsVisible -> viewModel.setLyricsVisible(false)
            isEqualizerVisible -> viewModel.setEqualizerVisible(false)
            isFullPlayerVisible -> viewModel.setFullPlayerVisible(false)
            currentTab != ScreenTab.HOME -> viewModel.setTab(ScreenTab.HOME)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Floating Mini-Player docked above bottom bar
                    if (currentSong != null && !isFullPlayerVisible) {
                        MiniPlayer(
                            song = currentSong,
                            isPlaying = isPlaying,
                            currentPosition = currentPos,
                            duration = duration,
                            onPlayPauseClick = { viewModel.controller.togglePlayPause() },
                            onSkipNext = { viewModel.controller.skipNext() },
                            onSkipPrevious = { viewModel.controller.skipPrevious() },
                            onLikeToggle = { viewModel.toggleLikeCurrentSong() },
                            onClick = { viewModel.setFullPlayerVisible(true) }
                        )
                    }

                    // Spotify Style Bottom Navigation
                    NavigationBar(
                        containerColor = CardSurface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.HOME,
                            onClick = { viewModel.setTab(ScreenTab.HOME) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyGreen,
                                selectedTextColor = SpotifyGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Color.Transparent
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.SEARCH,
                            onClick = { viewModel.setTab(ScreenTab.SEARCH) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == ScreenTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text("Search", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyGreen,
                                selectedTextColor = SpotifyGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Color.Transparent
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == ScreenTab.LIBRARY,
                            onClick = { viewModel.setTab(ScreenTab.LIBRARY) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == ScreenTab.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                                    contentDescription = "Your Library"
                                )
                            },
                            label = { Text("Your Library", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SpotifyGreen,
                                selectedTextColor = SpotifyGreen,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    ScreenTab.HOME -> HomeScreen(viewModel = viewModel)
                    ScreenTab.SEARCH -> SearchScreen(viewModel = viewModel)
                    ScreenTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                    ScreenTab.LOCAL_DEVICE -> LocalSongsScreen(
                        viewModel = localSongsViewModel,
                        onBackClick = { viewModel.setTab(ScreenTab.LIBRARY) }
                    )
                    ScreenTab.WRAPPED -> StatsWrappedScreen(viewModel = viewModel)
                    ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Full Now Playing Expandable Overlay
        AnimatedVisibility(
            visible = isFullPlayerVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                viewModel = viewModel,
                onDismiss = { viewModel.setFullPlayerVisible(false) }
            )
        }

        // Time-Synced Lyrics Screen Overlay
        AnimatedVisibility(
            visible = isLyricsVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            LyricsScreen(
                viewModel = viewModel,
                onClose = { viewModel.setLyricsVisible(false) }
            )
        }

        // Equalizer & FX Screen Overlay
        AnimatedVisibility(
            visible = isEqualizerVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            EqualizerScreen(
                viewModel = viewModel,
                onClose = { viewModel.setEqualizerVisible(false) }
            )
        }

        // Queue Sheet
        if (isQueueSheetVisible) {
            QueueSheet(
                queue = queue,
                currentSong = currentSong,
                onSongClick = { song ->
                    viewModel.playSong(song, queue)
                    viewModel.setQueueSheetVisible(false)
                },
                onDismiss = { viewModel.setQueueSheetVisible(false) }
            )
        }
    }
}
