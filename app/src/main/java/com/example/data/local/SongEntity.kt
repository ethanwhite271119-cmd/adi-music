package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUri: String,
    val streamUrl: String,
    val durationMs: Long,
    val isLocal: Boolean,
    val isDownloaded: Boolean,
    val isLiked: Boolean,
    val genre: String,
    val playCount: Int,
    val addedAt: Long = System.currentTimeMillis()
)
