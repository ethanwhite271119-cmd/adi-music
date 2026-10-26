package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AdiMusicApplication
import com.example.data.local.HistorySongRecord
import com.example.data.model.LyricLine
import com.example.data.model.LyricsDisplayMode
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.data.repository.WrappedStats
import com.example.service.MusicController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.service.NotificationControlConfig
import com.example.service.CompactPreset

enum class ScreenTab {
    HOME,
    SEARCH,
    LIBRARY,
    LOCAL_DEVICE,
    WRAPPED,
    SETTINGS,
    HISTORY,
    NOTIFICATION_CENTER
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AdiMusicApplication
    val repository: MusicRepository = app.musicRepository
    val controller: MusicController = app.musicController

    // Navigation & Player UI State
    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    private val _isLyricsVisible = MutableStateFlow(false)
    val isLyricsVisible: StateFlow<Boolean> = _isLyricsVisible.asStateFlow()

    private val _isEqualizerVisible = MutableStateFlow(false)
    val isEqualizerVisible: StateFlow<Boolean> = _isEqualizerVisible.asStateFlow()

    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    // Home Screen Category Filter & Real Streams
    private val _homeCategory = MutableStateFlow("All")
    val homeCategory: StateFlow<String> = _homeCategory.asStateFlow()

    private val _isHomeLoading = MutableStateFlow(true)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    private val _streamCatalog = MutableStateFlow<List<Song>>(emptyList())
    val streamCatalog: StateFlow<List<Song>> = _streamCatalog.asStateFlow()

    private val _trendingPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val trendingPlaylists: StateFlow<List<Playlist>> = _trendingPlaylists.asStateFlow()

    // Search Screen State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchCategory = MutableStateFlow("All")
    val searchCategory: StateFlow<String> = _searchCategory.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _searchHistory = MutableStateFlow(listOf("Arijit Singh", "The Weeknd", "Travis Scott", "Phonk Drift", "Taylor Swift"))
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Library Tab Filter
    private val _libraryFilter = MutableStateFlow("All")
    val libraryFilter: StateFlow<String> = _libraryFilter.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    // Settings
    private val _pureAmoled = MutableStateFlow(false)
    val pureAmoled: StateFlow<Boolean> = _pureAmoled.asStateFlow()

    private val _crossfadeSeconds = MutableStateFlow(3)
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    private val _gamingBubbleEnabled = MutableStateFlow(false)
    val gamingBubbleEnabled: StateFlow<Boolean> = _gamingBubbleEnabled.asStateFlow()

    // Dynamic Lyrics State
    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    // Indian & Bilingual Hindi/English Lyrics Mode
    private val _lyricsMode = MutableStateFlow(LyricsDisplayMode.BILINGUAL)
    val lyricsMode: StateFlow<LyricsDisplayMode> = _lyricsMode.asStateFlow()

    fun setLyricsMode(mode: LyricsDisplayMode) {
        _lyricsMode.value = mode
    }

    // Notification Control Center Configuration
    private val _notificationConfig = MutableStateFlow(NotificationControlConfig())
    val notificationConfig: StateFlow<NotificationControlConfig> = _notificationConfig.asStateFlow()

    fun updateNotificationConfig(transform: (NotificationControlConfig) -> NotificationControlConfig) {
        _notificationConfig.value = transform(_notificationConfig.value)
    }

    // Wrapped Stats
    private val _wrappedStats = MutableStateFlow<WrappedStats?>(null)
    val wrappedStats: StateFlow<WrappedStats?> = _wrappedStats.asStateFlow()

    val likedSongs: StateFlow<List<Song>> = repository.getLikedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<Song>> = repository.getDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localSongs: StateFlow<List<Song>> = repository.getLocalSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val previouslyPlayed: StateFlow<List<HistorySongRecord>> = repository.getPreviouslyPlayedRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedDistinct: StateFlow<List<Song>> = repository.getRecentlyPlayedDistinct()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearListeningHistory() {
        viewModelScope.launch {
            repository.clearListeningHistory()
        }
    }

    fun deleteHistoryEntry(historyId: Long) {
        viewModelScope.launch {
            repository.deleteHistoryEntry(historyId)
        }
    }

    private var searchJob: Job? = null

    init {
        loadHomeData()
        loadWrappedStats()

        // Sync lyrics whenever current song changes
        viewModelScope.launch {
            controller.currentSong.collect { song ->
                if (song != null) {
                    loadLyricsForSong(song)
                    repository.recordListening(song)
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isHomeLoading.value = true
            val topTracks = repository.fetchLiveTopCharts()
            _streamCatalog.value = topTracks
            _trendingPlaylists.value = repository.getTrendingPlaylists()
            _isHomeLoading.value = false
        }
    }

    fun setHomeCategory(cat: String) {
        _homeCategory.value = cat
        viewModelScope.launch {
            _isHomeLoading.value = true
            val tracks = if (cat == "All") {
                repository.fetchLiveTopCharts()
            } else {
                repository.fetchCategoryTracks(cat)
            }
            if (tracks.isNotEmpty()) {
                _streamCatalog.value = tracks
            }
            _isHomeLoading.value = false
        }
    }

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setFullPlayerVisible(visible: Boolean) {
        _isFullPlayerVisible.value = visible
    }

    fun setLyricsVisible(visible: Boolean) {
        _isLyricsVisible.value = visible
    }

    fun setEqualizerVisible(visible: Boolean) {
        _isEqualizerVisible.value = visible
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _isQueueSheetVisible.value = visible
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearchLoading.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(350) // Debounce network calls
            _isSearchLoading.value = true
            val results = repository.searchLiveMusic(query)
            _searchResults.value = results
            _isSearchLoading.value = false
        }
    }

    fun setSearchCategory(cat: String) {
        _searchCategory.value = cat
        if (_searchQuery.value.isNotBlank()) {
            setSearchQuery("${_searchQuery.value} $cat")
        }
    }

    fun addSearchQuery(query: String) {
        _searchQuery.value = query
        val list = _searchHistory.value.toMutableList()
        list.remove(query)
        list.add(0, query)
        _searchHistory.value = list.take(10)
        setSearchQuery(query)
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun setLibraryFilter(filter: String) {
        _libraryFilter.value = filter
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun togglePureAmoled() {
        _pureAmoled.value = !_pureAmoled.value
    }

    fun setCrossfadeSeconds(sec: Int) {
        _crossfadeSeconds.value = sec
    }

    fun toggleGamingBubble() {
        _gamingBubbleEnabled.value = !_gamingBubbleEnabled.value
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        controller.playSong(song, playlist.ifEmpty { _streamCatalog.value })
    }

    fun toggleLikeCurrentSong() {
        val current = controller.currentSong.value ?: return
        viewModelScope.launch {
            repository.toggleLike(current)
        }
    }

    fun toggleDownloadCurrentSong() {
        val current = controller.currentSong.value ?: return
        viewModelScope.launch {
            repository.toggleDownload(current)
        }
    }

    fun scanLocalFiles() {
        viewModelScope.launch {
            repository.scanAndSyncLocalAudio()
        }
    }

    fun loadWrappedStats() {
        viewModelScope.launch {
            _wrappedStats.value = repository.getListeningStats()
        }
    }

    private fun loadLyricsForSong(song: Song) {
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            _lyrics.value = repository.getSyncedLyrics(song)
            _isLoadingLyrics.value = false
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDatabaseBackup()
            onResult(json)
        }
    }

    fun importBackup(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importDatabaseBackup(json)
            if (success) {
                loadHomeData()
                loadWrappedStats()
            }
            onResult(success)
        }
    }
}
