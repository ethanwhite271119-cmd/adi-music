package com.example.data.model

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val hindiText: String? = null,
    val romanText: String? = null,
    val englishTranslation: String? = null
) {
    /**
     * Checks if this lyric line has native Indian / Hindi Devanagari text or character content.
     */
    fun hasHindiContent(): Boolean {
        if (!hindiText.isNullOrBlank()) return true
        return text.any { it in '\u0900'..'\u097F' }
    }

    /**
     * Retrieves primary display text based on user's chosen display mode.
     */
    fun getPrimaryText(mode: LyricsDisplayMode): String {
        return when (mode) {
            LyricsDisplayMode.HINDI -> hindiText?.ifBlank { null } ?: text
            LyricsDisplayMode.ENGLISH_ROMAN -> romanText?.ifBlank { null } ?: text
            LyricsDisplayMode.TRANSLATION -> englishTranslation?.ifBlank { null } ?: romanText ?: text
            LyricsDisplayMode.BILINGUAL -> hindiText?.ifBlank { null } ?: text
        }
    }

    /**
     * Retrieves secondary subtitle text (e.g. Roman transliteration under Hindi) in Bilingual mode.
     */
    fun getSecondaryText(mode: LyricsDisplayMode): String? {
        if (mode != LyricsDisplayMode.BILINGUAL) return null
        val sub = romanText ?: englishTranslation
        val prim = getPrimaryText(mode)
        return if (sub != null && sub.trim() != prim.trim()) sub else null
    }
}
