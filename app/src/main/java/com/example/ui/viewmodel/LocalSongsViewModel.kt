package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AdiMusicApplication
import com.example.data.model.LocalSong
import com.example.data.model.Song
import com.example.data.repository.LocalMusicRepository
import com.example.service.MusicController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LocalSongsUiState {
    data object Loading : LocalSongsUiState
    data object PermissionRequired : LocalSongsUiState
    data object Empty : LocalSongsUiState
    data class Success(
        val allSongs: List<LocalSong>,
        val filteredSongs: List<LocalSong>,
        val query: String = ""
    ) : LocalSongsUiState
}

class LocalSongsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AdiMusicApplication
    private val repository = LocalMusicRepository(application)
    val musicController: MusicController = app.musicController

    private val _uiState = MutableStateFlow<LocalSongsUiState>(LocalSongsUiState.Loading)
    val uiState: StateFlow<LocalSongsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadLocalSongs()
    }

    fun loadLocalSongs() {
        viewModelScope.launch {
            _uiState.value = LocalSongsUiState.Loading
            try {
                val songs = repository.getLocalSongs()
                if (songs.isEmpty()) {
                    _uiState.value = LocalSongsUiState.Empty
                } else {
                    _uiState.value = LocalSongsUiState.Success(
                        allSongs = songs,
                        filteredSongs = songs,
                        query = _searchQuery.value
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = LocalSongsUiState.Empty
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.value = LocalSongsUiState.PermissionRequired
    }

    fun filterSongs(query: String) {
        _searchQuery.value = query
        val currentState = _uiState.value
        if (currentState is LocalSongsUiState.Success) {
            val filtered = if (query.isBlank()) {
                currentState.allSongs
            } else {
                currentState.allSongs.filter { song ->
                    song.title.contains(query, ignoreCase = true) ||
                            song.artist.contains(query, ignoreCase = true) ||
                            song.album.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = currentState.copy(
                filteredSongs = filtered,
                query = query
            )
        }
    }

    fun playLocalSong(localSong: LocalSong, songList: List<LocalSong>) {
        val playableSong = localSong.toSong()
        val playlist = songList.map { it.toSong() }
        musicController.playSong(playableSong, playlist)
    }
}
