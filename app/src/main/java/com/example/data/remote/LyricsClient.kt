package com.example.data.remote

import com.example.data.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class LyricsClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun getSyncedLyrics(trackName: String, artistName: String): List<LyricLine> = withContext(Dispatchers.IO) {
        try {
            val encodedTrack = URLEncoder.encode(trackName, "UTF-8")
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            val url = "https://lrclib.net/api/get?track_name=$encodedTrack&artist_name=$encodedArtist"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AdiMusicApp/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val syncedLyrics = json.optString("syncedLyrics", "")
                    if (syncedLyrics.isNotEmpty()) {
                        return@withContext parseLrc(syncedLyrics)
                    }
                }
            }
        } catch (_: Exception) {
        }

        // Return fallback synced lines for offline / fallback demo
        generateFallbackLyrics(trackName, artistName)
    }

    private fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

        lrcContent.lines().forEach { line ->
            val matcher = pattern.matcher(line.trim())
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msString = matcher.group(3) ?: "0"
                val ms = if (msString.length == 2) msString.toLong() * 10 else msString.toLong()
                val totalMs = (min * 60 + sec) * 1000 + ms
                val text = matcher.group(4)?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, text))
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    private fun generateFallbackLyrics(trackName: String, artistName: String): List<LyricLine> {
        return listOf(
            LyricLine(0L, "♪ $trackName - $artistName ♪"),
            LyricLine(5000L, "Lost in the rhythm and the neon lights"),
            LyricLine(12000L, "Feel the obsidian pulse through the night"),
            LyricLine(20000L, "Can you hear the sound inside your soul?"),
            LyricLine(28000L, "Adi Music takes complete control"),
            LyricLine(36000L, "Waveforms rising high up to the sky"),
            LyricLine(45000L, "Echoes in the dark, we never say goodbye"),
            LyricLine(58000L, "Yeah, turn the bass up, let the speakers glow"),
            LyricLine(70000L, "This is the sound that makes the river flow"),
            LyricLine(85000L, "Feel the obsidian pulse through the night"),
            LyricLine(100000L, "Lost in the rhythm and the neon lights")
        )
    }
}
