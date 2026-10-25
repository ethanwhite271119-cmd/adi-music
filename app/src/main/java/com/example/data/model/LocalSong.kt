package com.example.data.model

data class LocalSong(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dataPath: String,
    val albumArtUri: String
) {
    fun toSong(): Song {
        return Song(
            id = "local_$id",
            title = title,
            artist = artist,
            album = album,
            artworkUri = albumArtUri,
            streamUrl = dataPath,
            durationMs = durationMs,
            isLocal = true,
            isDownloaded = true,
            isLiked = false,
            genre = "Local Audio"
        )
    }
}
