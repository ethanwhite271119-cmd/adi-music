package com.example.data.model

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val artworkUri: String = "",
    val songCount: Int = 0,
    val isPinned: Boolean = false
)
