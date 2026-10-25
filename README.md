
# 🎵 Adi Music — Android Music Player

A modern Android music player concept featuring a dark AMOLED-style interface, vibrant green accents, music discovery, playback controls, audio customization, and listening analytics.

## 📱 App UI Screenshots

<table>
  <tr>
    <td align="center" width="25%">
      <b>🏠 Home</b><br><br>
      <img src="https://i.postimg.cc/YCgvRSYX/Screenshot-2026-10-25-14-34-13-78-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Adi Music Home Screen">
    </td>
    <td align="center" width="25%">
      <b>🔍 Search</b><br><br>
      <img src="https://i.postimg.cc/pL8mCdzq/Screenshot-2026-10-25-14-07-26-20-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Adi Music Search Screen">
    </td>
    <td align="center" width="25%">
      <b>🎵 Library</b><br><br>
      <img src="https://i.postimg.cc/9QTDbfZ1/Screenshot-2026-10-25-14-07-17-90-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Adi Music Library Screen">
    </td>
    <td align="center" width="25%">
      <b>🎧 Player</b><br><br>
      <img src="https://i.postimg.cc/nLJCPtGT/Screenshot-2026-10-25-14-35-13-39-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Adi Music Player Screen">
    </td>
  </tr>
  <tr>
    <td align="center" width="25%">
      <b>🎚️ Equalizer</b><br><br>
      <img src="https://i.postimg.cc/d0YLxKj6/Screenshot-2026-10-25-14-35-30-75-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Audio Equalizer and FX">
    </td>
    <td align="center" width="25%">
      <b>📊 Analytics</b><br><br>
      <img src="https://i.postimg.cc/bvhsBj9W/Screenshot-2026-10-25-14-35-41-97-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Adi Wrapped and Analytics">
    </td>
    <td align="center" width="25%">
      <b>🎶 Music Home</b><br><br>
      <img src="https://i.postimg.cc/YCgvRSYX/Screenshot-2026-10-25-14-34-13-78-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Music Discovery Interface">
    </td>
    <td align="center" width="25%">
      <b>🎼 Discover</b><br><br>
      <img src="https://i.postimg.cc/pL8mCdzq/Screenshot-2026-10-25-14-07-26-20-752d8aae83b40248d21845b7579a970b.jpg" width="100%" alt="Music Search and Discovery">
    </td>
  </tr>
</table>

## ✨ Features

- 🏠 **Home Screen** — Music categories, recommendations, quick picks, and trending charts.
- 🔍 **Music Search** — Search interface for songs, artists, and albums.
- 🎵 **Your Library** — Liked songs, playlists, downloads, and local audio.
- 🎧 **Now Playing** — Music playback interface with play/pause, next, previous, shuffle, and repeat controls.
- 🎚️ **Audio Equalizer & FX** — Five-band graphic equalizer interface and audio customization.
- 📊 **Adi Wrapped & Analytics** — Listening statistics, top artists, and music trends.
- 🌑 **Dark AMOLED Theme** — Dark background with vibrant green accents.
- 📱 **Bottom Navigation** — Home, Search, and Your Library.
- 🔋 **Battery Optimization Settings** — UI for background playback-related settings.
- 💾 **Backup & Restore** — Interface for exporting music library data.

## 🛠️ Technology Stack

- **Kotlin** — Android development language.
- **Jetpack Compose** — Native Android UI toolkit.
- **Material 3** — Modern UI components.
- **AndroidX Media3 / ExoPlayer** — Audio playback engine.
- **Room Database** — Local database for playlists and listening history.

> Technology integrations depend on the implementation available in the project source code.

## 📂 Repository Structure

```text
AdiMusicAndroid/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/adimusic/
│           └── MainActivity.kt
├── screenshots/
│   ├── home.png
│   ├── search.png
│   ├── library.png
│   ├── player.png
│   ├── equalizer.png
│   └── analytics.png
├── README.md
├── REALME.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
└── LICENSE
```

## 🚀 Getting Started

1. Clone or download the repository.
2. Open the Android project in Android Studio.
3. Wait for Gradle synchronization to finish.
4. Select an Android emulator or connect a compatible Android device.
5. Press **Run** to build and launch the application.

## 🚧 Development Status

The screenshots represent the intended app interface. Actual functionality depends on the implemented Android source code.

Potential development tasks include:

- Real-time music catalogue search using a suitable API.
- Audio playback using AndroidX Media3 / ExoPlayer.
- Persistent playlists and liked songs using Room.
- Local audio file browsing.
- Functional equalizer and audio effects.
- Synchronized lyrics.
- Background playback and media notifications.
- Listening analytics and statistics.
- Offline playback where supported by the music provider.

## 📸 Screenshot Notes

The screenshot grid uses externally hosted image links.

- Six unique screenshots are included.
- The last two cells reuse the Home and Search screenshots to complete the four-column layout.
- Images must remain accessible on the external hosting service.
- For long-term reliability, upload the screenshots into the repository's `screenshots/` folder and use repository-relative image paths.

## 🤝 Contributing

Contributions, suggestions, and bug reports are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Make and test your changes.
4. Submit a pull request.

## 📄 License

See [LICENSE](LICENSE) for the project's licensing terms. Add a license file if the repository does not already contain one.

---

**Adi Music — Your music, your vibe. 🎧**
