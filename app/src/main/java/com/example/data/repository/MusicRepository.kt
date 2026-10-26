package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ArtistPlayCount
import com.example.data.local.HistorySongRecord
import com.example.data.local.ListeningHistoryEntity
import com.example.data.local.LocalAudioScanner
import com.example.data.local.PlaylistEntity
import com.example.data.local.SongEntity
import com.example.data.model.LyricLine
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.remote.LyricsClient
import com.example.data.remote.MusicApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MusicRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val localScanner = LocalAudioScanner(context)
    private val lyricsClient = LyricsClient()
    private val musicApiClient = MusicApiClient()

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val indianTracks = musicApiClient.getCuratedIndianTracks()
        songDao.insertSongs(indianTracks.map { it.toEntity() })

        val existing = songDao.getAllSongs().firstOrNull()
        if (existing.isNullOrEmpty() || existing.size <= indianTracks.size) {
            val liveTracks = musicApiClient.getTopCharts()
            if (liveTracks.isNotEmpty()) {
                songDao.insertSongs(liveTracks.map { it.toEntity() })
            }
            val livePlaylists = musicApiClient.getTrendingPlaylists()
            livePlaylists.forEach { pl ->
                songDao.insertPlaylist(
                    PlaylistEntity(
                        id = pl.id,
                        name = pl.name,
                        description = pl.description,
                        artworkUri = pl.artworkUri,
                        isPinned = pl.isPinned
                    )
                )
            }
        }
    }

    suspend fun fetchLiveTopCharts(): List<Song> = withContext(Dispatchers.IO) {
        val indianTracks = musicApiClient.getCuratedIndianTracks()
        try {
            val liveTracks = musicApiClient.getTopCharts()
            val combined = (indianTracks + liveTracks).distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
            if (combined.isNotEmpty()) {
                songDao.insertSongs(combined.map { it.toEntity() })
                return@withContext combined
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val cached = songDao.getAllSongs().firstOrNull()?.map { it.toModel() }
            if (!cached.isNullOrEmpty()) {
                return@withContext (indianTracks + cached).distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
            }
        } catch (_: Exception) {}
        indianTracks
    }

    suspend fun fetchCategoryTracks(category: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val tracks = musicApiClient.getCategoryTracks(category)
            if (tracks.isNotEmpty()) {
                songDao.insertSongs(tracks.map { it.toEntity() })
            }
            tracks
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchLiveMusic(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val results = musicApiClient.searchSongs(query)
            if (results.isNotEmpty()) {
                songDao.insertSongs(results.map { it.toEntity() })
            }
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getTrendingPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        try {
            musicApiClient.getTrendingPlaylists()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getLikedSongs(): Flow<List<Song>> {
        return songDao.getLikedSongs().map { list -> list.map { it.toModel() } }
    }

    fun getDownloadedSongs(): Flow<List<Song>> {
        return songDao.getDownloadedSongs().map { list -> list.map { it.toModel() } }
    }

    fun getLocalSongs(): Flow<List<Song>> {
        return songDao.getLocalSongs().map { list -> list.map { it.toModel() } }
    }

    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { list ->
            list.map {
                Playlist(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    artworkUri = it.artworkUri,
                    songCount = 0,
                    isPinned = it.isPinned
                )
            }
        }
    }

    suspend fun createPlaylist(name: String, description: String = "", artworkUri: String = ""): String = withContext(Dispatchers.IO) {
        val id = "pl_user_${System.currentTimeMillis()}"
        playlistDao.insertPlaylist(
            PlaylistEntity(
                id = id,
                name = name,
                description = description,
                artworkUri = artworkUri,
                isPinned = false
            )
        )
        id
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.appendSongToPlaylist(playlistId, songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsInPlaylist(playlistId: String): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId).map { list -> list.map { it.toModel() } }
    }

    fun getPlaylistWithSongs(playlistId: String): Flow<com.example.data.local.PlaylistWithSongs?> {
        return playlistDao.getPlaylistWithSongs(playlistId)
    }

    fun isSongInPlaylist(playlistId: String, songId: String): Flow<Boolean> {
        return playlistDao.isSongInPlaylist(playlistId, songId)
    }

    suspend fun scanAndSyncLocalAudio(): List<Song> = withContext(Dispatchers.IO) {
        val localSongs = localScanner.scanLocalAudio()
        if (localSongs.isNotEmpty()) {
            songDao.insertSongs(localSongs.map { it.toEntity() })
        }
        localSongs
    }

    suspend fun toggleLike(song: Song) = withContext(Dispatchers.IO) {
        val newStatus = !song.isLiked
        songDao.setLiked(song.id, newStatus)
    }

    suspend fun toggleDownload(song: Song) = withContext(Dispatchers.IO) {
        val newStatus = !song.isDownloaded
        songDao.setDownloaded(song.id, newStatus)
    }

    suspend fun recordListening(song: Song, durationSec: Long = 60) = withContext(Dispatchers.IO) {
        songDao.insertSong(song.toEntity())
        songDao.incrementPlayCount(song.id)
        songDao.insertHistory(
            ListeningHistoryEntity(
                songId = song.id,
                title = song.title,
                artist = song.artist,
                artworkUri = song.artworkUri,
                playedAt = System.currentTimeMillis(),
                durationListenedSeconds = durationSec
            )
        )
    }

    fun getPreviouslyPlayedRecords(limit: Int = 60): Flow<List<HistorySongRecord>> {
        return songDao.getPreviouslyPlayedRecords(limit)
    }

    fun getRecentlyPlayedDistinct(limit: Int = 15): Flow<List<Song>> {
        return songDao.getDistinctRecentlyPlayed(limit).map { list -> list.map { it.toModel() } }
    }

    suspend fun deleteHistoryEntry(historyId: Long) = withContext(Dispatchers.IO) {
        songDao.deleteHistoryEntry(historyId)
    }

    suspend fun clearListeningHistory() = withContext(Dispatchers.IO) {
        songDao.clearListeningHistory()
    }

    suspend fun getSyncedLyrics(song: Song): List<LyricLine> {
        return lyricsClient.getSyncedLyrics(song.title, song.artist)
    }

    suspend fun getListeningStats(): WrappedStats = withContext(Dispatchers.IO) {
        val totalMinutes = songDao.getTotalListeningMinutes()
        val topArtists = songDao.getTopArtists(5)
        val topSongs = songDao.getTopSongs(5).map { it.toModel() }
        WrappedStats(
            totalMinutes = if (totalMinutes == 0L) 120L else totalMinutes,
            topArtists = topArtists,
            topSongs = topSongs,
            dayStreak = 7
        )
    }

    suspend fun exportDatabaseBackup(): String = withContext(Dispatchers.IO) {
        val songs = songDao.getAllSongs().firstOrNull() ?: emptyList()
        val playlists = songDao.getAllPlaylists().firstOrNull() ?: emptyList()

        val json = JSONObject()
        val songsArray = JSONArray()
        songs.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("artist", s.artist)
            obj.put("album", s.album)
            obj.put("artworkUri", s.artworkUri)
            obj.put("isLiked", s.isLiked)
            obj.put("isDownloaded", s.isDownloaded)
            songsArray.put(obj)
        }
        json.put("songs", songsArray)

        val plArray = JSONArray()
        playlists.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("description", p.description)
            plArray.put(obj)
        }
        json.put("playlists", plArray)
        json.put("version", 1)
        json.put("exportedAt", System.currentTimeMillis())

        json.toString(2)
    }

    suspend fun importDatabaseBackup(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(jsonString)
            val songsArray = json.optJSONArray("songs") ?: return@withContext false
            for (i in 0 until songsArray.length()) {
                val obj = songsArray.getJSONObject(i)
                val id = obj.getString("id")
                val isLiked = obj.optBoolean("isLiked", false)
                val isDownloaded = obj.optBoolean("isDownloaded", false)
                songDao.setLiked(id, isLiked)
                songDao.setDownloaded(id, isDownloaded)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun Song.toEntity(): SongEntity {
        return SongEntity(
            id = id,
            title = title,
            artist = artist,
            album = album,
            artworkUri = artworkUri,
            streamUrl = streamUrl,
            durationMs = durationMs,
            isLocal = isLocal,
            isDownloaded = isDownloaded,
            isLiked = isLiked,
            genre = genre,
            playCount = playCount
        )
    }

    private fun SongEntity.toModel(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            artworkUri = artworkUri,
            streamUrl = streamUrl,
            durationMs = durationMs,
            isLocal = isLocal,
            isDownloaded = isDownloaded,
            isLiked = isLiked,
            genre = genre,
            playCount = playCount
        )
    }
}

data class WrappedStats(
    val totalMinutes: Long,
    val topArtists: List<ArtistPlayCount>,
    val topSongs: List<Song>,
    val dayStreak: Int
)
