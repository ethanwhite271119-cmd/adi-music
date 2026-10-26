package com.example.data.model

enum class LyricsDisplayMode(
    val title: String,
    val shortLabel: String,
    val description: String
) {
    BILINGUAL(
        title = "Dual / दोनों",
        shortLabel = "Dual",
        description = "Hindi Devanagari & English Romanized simultaneously"
    ),
    HINDI(
        title = "हिन्दी (Hindi)",
        shortLabel = "हिन्दी",
        description = "Original Devanagari Hindi script"
    ),
    ENGLISH_ROMAN(
        title = "English / Hinglish",
        shortLabel = "English",
        description = "Phonetic Roman text for singing along"
    ),
    TRANSLATION(
        title = "Translation",
        shortLabel = "Meaning",
        description = "Poetic English meaning of the lyrics"
    )
}
