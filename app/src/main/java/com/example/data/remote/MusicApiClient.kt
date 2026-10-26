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
            "indian", "hindi", "bollywood" -> "Bollywood Hits Arijit"
            "music" -> "Today Top Hits"
            "relax" -> "Lofi Sleep Chill"
            "workout" -> "Workout Motivation Beast"
            "gaming/phonk" -> "Phonk Drift Tokyo"
            else -> "Global Viral Hits"
        }
        if (category.lowercase() in listOf("indian", "hindi", "bollywood")) {
            getIndianHits()
        } else {
            searchSongs(query)
        }
    }

    suspend fun getIndianHits(): List<Song> = withContext(Dispatchers.IO) {
        val curated = getCuratedIndianTracks()
        try {
            val live = searchSongs("Bollywood Romantic Hits")
            if (live.isNotEmpty()) {
                val combined = (curated + live).distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
                return@withContext combined
            }
        } catch (_: Exception) {}
        curated
    }

    fun getCuratedIndianTracks(): List<Song> {
        return listOf(
            Song(
                id = "in_kesariya",
                title = "Kesariya (केसरिया)",
                artist = "Arijit Singh, Pritam",
                album = "Brahmastra",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music112/v4/9f/13/ca/9f13ca3b-e533-03e0-f19a-f0aaa774581d/196589311191.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/38/4c/5c/384c5c8f-3ff8-e457-b2f7-3158ce108649/mzaf_12389299033886433185.plus.aac.p.m4a",
                durationMs = 268000L,
                genre = "Bollywood / Hindi"
            ),
            Song(
                id = "in_tumhiho",
                title = "Tum Hi Ho (तुम ही हो)",
                artist = "Arijit Singh, Mithoon",
                album = "Aashiqui 2",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/08/94/3e/08943ee6-e820-22fc-26e6-6e82ee80f08e/8902894353459_cover.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/3a/8c/9b/3a8c9b0b-2def-750a-f615-1555bf941edf/mzaf_17229496441442805917.plus.aac.p.m4a",
                durationMs = 262000L,
                genre = "Hindi Romantic"
            ),
            Song(
                id = "in_apnabanale",
                title = "Apna Bana Le (अपना बना ले)",
                artist = "Arijit Singh, Sachin-Jigar",
                album = "Bhediya",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music122/v4/6c/13/ee/6c13eeef-9c94-d2ec-246e-1da5d6101c51/196589578143.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/eb/27/61/eb2761c7-d606-0912-dff0-2dc6b69974bd/mzaf_2023722930851223219.plus.aac.p.m4a",
                durationMs = 261000L,
                genre = "Bollywood Romantic"
            ),
            Song(
                id = "in_husn",
                title = "Husn (हुस्न)",
                artist = "Anuv Jain",
                album = "Husn Single",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/04/b3/ef/04b3ef86-f78a-c60f-e685-6495db368528/859779374092_cover.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/e4/55/54/e455541c-5130-bc7d-5249-7f7c92e50d3d/mzaf_5017664543802375133.plus.aac.p.m4a",
                durationMs = 217000L,
                genre = "Indian Indie"
            ),
            Song(
                id = "in_channamereya",
                title = "Channa Mereya (चन्ना मेरेया)",
                artist = "Arijit Singh, Pritam",
                album = "Ae Dil Hai Mushkil",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music118/v4/ca/87/a2/ca87a229-3b95-ef6c-7e61-a5bf775ee808/886446187834.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/d0/a6/3c/d0a63c52-d2a1-2e1b-a221-f011a1500b8a/mzaf_5133687548743640142.plus.aac.p.m4a",
                durationMs = 289000L,
                genre = "Sufi & Soul"
            ),
            Song(
                id = "in_raataan",
                title = "Raataan Lambiyan (रातां लंबियां)",
                artist = "Jubin Nautiyal, Asees Kaur",
                album = "Shershaah",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/01/24/76/01247657-fc7a-d04b-325d-4f1ef59ff4e6/8902894360341_cover.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/ff/7a/7a/ff7a7a63-9947-cefa-e269-ad9602fb1ca3/mzaf_14039909875904828823.plus.aac.p.m4a",
                durationMs = 230000L,
                genre = "Punjabi Bollywood"
            ),
            Song(
                id = "in_pasoori",
                title = "Pasoori (पसूरी)",
                artist = "Ali Sethi, Shae Gill",
                album = "Coke Studio Season 14",
                artworkUri = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/f1/a0/0a/f1a00a12-82dc-eb4b-149b-38148b8cfeb5/196626639530.jpg/600x600bb.jpg",
                streamUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/62/33/1e/62331ea8-d1df-027d-fe75-ac16a519323d/mzaf_14381883946572745360.plus.aac.p.m4a",
                durationMs = 224000L,
                genre = "Desi Global"
            )
        )
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
