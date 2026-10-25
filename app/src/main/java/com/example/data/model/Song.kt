package com.example.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val artworkUri: String = "",
    val streamUrl: String = "",
    val durationMs: Long = 0L,
    val isLocal: Boolean = false,
    val isDownloaded: Boolean = false,
    val isLiked: Boolean = false,
    val genre: String = "Pop",
    val playCount: Int = 0
)
