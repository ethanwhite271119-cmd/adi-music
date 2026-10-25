package com.example.data.remote

import com.example.data.model.Playlist
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MusicApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun getTopCharts(): List<Song> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Song>()
        try {
            val request = Request.Builder()
                .url("https://api.deezer.com/chart/0/tracks?limit=30")
                .header("User-Agent", "AdiMusic/1.0 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val data = json.optJSONArray("data")
                    if (data != null) {
                        for (i in 0 until data.length()) {
                            val item = data.getJSONObject(i)
                            val song = parseDeezerTrack(item, "Global Top 50")
                            if (song != null && song.streamUrl.isNotEmpty()) {
                                tracks.add(song)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (tracks.isEmpty()) {
            return@withContext searchSongs("Top Hits 2026")
        }
        tracks
    }

    suspend fun getCategoryTracks(category: String): List<Song> = withContext(Dispatchers.IO) {
        val query = when (category.lowercase()) {
            "music" -> "Today Top Hits"
            "relax" -> "Lofi Sleep Chill"
            "workout" -> "Workout Motivation Beast"
            "gaming/phonk" -> "Phonk Drift Tokyo"
            else -> "Global Viral Hits"
        }
        searchSongs(query)
    }

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val results = mutableListOf<Song>()
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")

        // 1. Try Deezer Search API
        try {
            val url = "https://api.deezer.com/search?q=$encoded&limit=25"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AdiMusic/1.0 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val data = json.optJSONArray("data")
                        if (data != null) {
                            for (i in 0 until data.length()) {
                                val item = data.getJSONObject(i)
                                val song = parseDeezerTrack(item, query)
                                if (song != null && song.streamUrl.isNotEmpty()) {
                                    results.add(song)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. If Deezer yielded few results or failed, supplement with iTunes API
        if (results.size < 5) {
            try {
                val itunesUrl = "https://itunes.apple.com/search?term=$encoded&entity=song&limit=20"
                val itunesRequest = Request.Builder()
                    .url(itunesUrl)
                    .header("User-Agent", "AdiMusic/1.0 (Android)")
                    .build()

                client.newCall(itunesRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val itunesResults = json.optJSONArray("results")
                            if (itunesResults != null) {
                                for (i in 0 until itunesResults.length()) {
                                    val item = itunesResults.getJSONObject(i)
                                    val trackName = item.optString("trackName", "")
                                    val artistName = item.optString("artistName", "")
                                    val previewUrl = item.optString("previewUrl", "")
                                    val collectionName = item.optString("collectionName", "")
                                    val rawArtwork = item.optString("artworkUrl100", "")
                                    val highResArtwork = rawArtwork.replace("100x100bb", "600x600bb")
                                    val durationMs = item.optLong("trackTimeMillis", 180000L)
                                    val trackId = item.optLong("trackId", System.currentTimeMillis()).toString()

                                    if (trackName.isNotEmpty() && previewUrl.isNotEmpty()) {
                                        results.add(
                                            Song(
                                                id = "itunes_$trackId",
                                                title = trackName,
                                                artist = artistName,
                                                album = collectionName,
                                                artworkUri = highResArtwork,
                                                streamUrl = previewUrl,
                                                durationMs = durationMs,
                                                genre = query
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        results.distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
    }

    suspend fun getTrendingPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        val playlists = mutableListOf<Playlist>()
        try {
            val url = "https://api.deezer.com/chart/0/playlists?limit=8"
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val data = json.optJSONArray("data")
                    if (data != null) {
                        for (i in 0 until data.length()) {
                            val pl = data.getJSONObject(i)
                            val id = pl.optLong("id").toString()
                            val title = pl.optString("title", "Top Playlist")
                            val picture = pl.optString("picture_big", pl.optString("picture_medium", ""))
                            val nbTracks = pl.optInt("nb_tracks", 50)

                            playlists.add(
                                Playlist(
                                    id = id,
                                    name = title,
                                    description = "Official Real-Time Chart",
                                    artworkUri = picture,
                                    songCount = nbTracks,
                                    isPinned = i < 3
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (playlists.isEmpty()) {
            listOf(
                Playlist("pl_real_1", "Today's Top Hits", "Global Live Music Chart", "https://cdn-images.dzcdn.net/images/cover/b868399da682f34dcd7d98af1c0de80b/500x500-000000-80-0-0.jpg", 50, true),
                Playlist("pl_real_2", "Viral Hits 2026", "Trending on TikTok & Charts", "https://cdn-images.dzcdn.net/images/cover/247b228179aea3b083eef43522b78b45/500x500-000000-80-0-0.jpg", 40, true),
                Playlist("pl_real_3", "Phonk & Bass Night", "Aggressive Drift Anthems", "https://cdn-images.dzcdn.net/images/cover/32715eb33ab5e20b6c5be10da46a852c/500x500-000000-80-0-0.jpg", 35, true)
            )
        } else {
            playlists
        }
    }

    private fun parseDeezerTrack(item: JSONObject, defaultGenre: String): Song? {
        val id = item.optLong("id", 0L).toString()
        val title = item.optString("title", "").trim()
        val streamUrl = item.optString("preview", "").trim()
        val durationSec = item.optLong("duration", 180L)

        if (title.isEmpty() || streamUrl.isEmpty()) return null

        val artistObj = item.optJSONObject("artist")
        val artistName = artistObj?.optString("name", "Unknown Artist") ?: "Unknown Artist"

        val albumObj = item.optJSONObject("album")
        val albumTitle = albumObj?.optString("title", "") ?: ""
        val artwork = albumObj?.optString("cover_big", "")
            ?: albumObj?.optString("cover_medium", "")
            ?: artistObj?.optString("picture_big", "")
            ?: ""

        return Song(
            id = "dz_$id",
            title = title,
            artist = artistName,
            album = albumTitle,
            artworkUri = artwork,
            streamUrl = streamUrl,
            durationMs = durationSec * 1000L,
            genre = defaultGenre
        )
    }
}
