package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ArtistPlayCount(
    val artist: String,
    val count: Int
)

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY addedAt DESC")
    fun getLikedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY addedAt DESC")
    fun getDownloadedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isLocal = 1 ORDER BY title ASC")
    fun getLocalSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isLiked = :isLiked WHERE id = :songId")
    suspend fun setLiked(songId: String, isLiked: Boolean)

    @Query("UPDATE songs SET isDownloaded = :isDownloaded WHERE id = :songId")
    suspend fun setDownloaded(songId: String, isDownloaded: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String)

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    // Listening History & Wrapped Analytics
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: ListeningHistoryEntity)

    @Query("SELECT * FROM listening_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 30): Flow<List<ListeningHistoryEntity>>

    @Query("SELECT COALESCE(SUM(durationListenedSeconds) / 60, 0) FROM listening_history")
    suspend fun getTotalListeningMinutes(): Long

    @Query("SELECT artist, COUNT(*) as count FROM listening_history GROUP BY artist ORDER BY count DESC LIMIT :limit")
    suspend fun getTopArtists(limit: Int = 5): List<ArtistPlayCount>

    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    suspend fun getTopSongs(limit: Int = 5): List<SongEntity>
}
