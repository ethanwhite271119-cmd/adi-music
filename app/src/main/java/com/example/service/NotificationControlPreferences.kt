package com.example.service

data class NotificationControlConfig(
    val showArtworkOnLockscreen: Boolean = true,
    val showSeekbar: Boolean = true,
    val showLikeButton: Boolean = true,
    val persistentNotification: Boolean = true,
    val compactPreset: CompactPreset = CompactPreset.STANDARD
)

enum class CompactPreset(
    val label: String,
    val description: String,
    val actionsSummary: String
) {
    STANDARD(
        label = "Standard Playback",
        description = "Previous Track • Play/Pause • Next Track",
        actionsSummary = "⏮  ⏯  ⏭"
    ),
    FAVORITE_FIRST(
        label = "Quick Like",
        description = "Like / Favorite • Play/Pause • Next Track",
        actionsSummary = "❤  ⏯  ⏭"
    ),
    SEEK_FOCUSED(
        label = "Scrub & Jump",
        description = "Rewind 10s • Play/Pause • Forward 30s",
        actionsSummary = "⏪  ⏯  ⏩"
    )
}
