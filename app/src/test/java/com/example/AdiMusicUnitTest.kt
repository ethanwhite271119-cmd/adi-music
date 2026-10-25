package com.example

import com.example.data.model.LyricLine
import com.example.data.model.Playlist
import com.example.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdiMusicUnitTest {

    @Test
    fun testSongModelCreation() {
        val song = Song(
            id = "dz_528330441",
            title = "Believer",
            artist = "Imagine Dragons",
            album = "Evolve",
            artworkUri = "https://cdn-images.dzcdn.net/images/cover/247b228179aea3b083eef43522b78b45/500x500.jpg",
            streamUrl = "https://cdnt-preview.dzcdn.net/api/1/1/c/b/e/0/cbe9e3bf5b7fb4c15dc76f53c2221559.mp3",
            durationMs = 202000L,
            genre = "Rock"
        )

        assertEquals("dz_528330441", song.id)
        assertEquals("Believer", song.title)
        assertEquals("Imagine Dragons", song.artist)
        assertFalse(song.isLiked)
        assertFalse(song.isDownloaded)
    }

    @Test
    fun testPlaylistModel() {
        val playlist = Playlist(
            id = "pl_1",
            name = "Today's Top Hits",
            songCount = 50,
            isPinned = true
        )

        assertEquals("pl_1", playlist.id)
        assertTrue(playlist.isPinned)
        assertEquals(50, playlist.songCount)
    }

    @Test
    fun testLyricLineOrdering() {
        val lyrics = listOf(
            LyricLine(12000L, "Second line"),
            LyricLine(5000L, "First line"),
            LyricLine(20000L, "Third line")
        ).sortedBy { it.timestampMs }

        assertEquals(5000L, lyrics[0].timestampMs)
        assertEquals("First line", lyrics[0].text)
        assertEquals(12000L, lyrics[1].timestampMs)
        assertEquals(20000L, lyrics[2].timestampMs)
    }

    @Test
    fun testLocalSongModelAndConversion() {
        val localSong = com.example.data.model.LocalSong(
            id = 101L,
            title = "Acoustic Sunset",
            artist = "Local Artist",
            album = "My Recordings",
            durationMs = 185000L,
            dataPath = "/storage/emulated/0/Music/sunset.flac",
            albumArtUri = "content://media/external/audio/albumart/101"
        )

        assertEquals(101L, localSong.id)
        assertEquals("Acoustic Sunset", localSong.title)

        val playable = localSong.toSong()
        assertEquals("local_101", playable.id)
        assertTrue(playable.isLocal)
        assertTrue(playable.isDownloaded)
        assertEquals("/storage/emulated/0/Music/sunset.flac", playable.streamUrl)
    }

    @Test
    fun testPlaylistSongCrossRefCreation() {
        val crossRef = com.example.data.local.PlaylistSongCrossRef(
            playlistId = "pl_custom_1",
            songId = "dz_528330441",
            position = 0
        )

        assertEquals("pl_custom_1", crossRef.playlistId)
        assertEquals("dz_528330441", crossRef.songId)
        assertEquals(0, crossRef.position)
    }
}
