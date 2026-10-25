
# 🎵 Adi Music — Android Music Player

A modern Android music player concept featuring a dark AMOLED-style interface, vibrant green accents, music discovery, playback controls, and audio customization.

## 📱 App UI Screenshots

Here is a preview of the Adi Music app interface, organized in a compact 2-column grid.

<table>
  <tr>
    <td align="center" width="50%">
      <b>🏠 Home Screen</b><br><br>
      <img src="https://i.postimg.cc/YCgvRSYX/Screenshot-2026-10-25-14-34-13-78-752d8aae83b40248d21845b7579a970b.jpg" width="220" alt="Adi Music Home Screen">
    </td>
    <td align="center" width="50%">
      <b>🔍 Search Screen</b><br><br>
      <img src="https://i.postimg.cc/pL8mCdzq/Screenshot-2026-10-25-14-07-26-20-752d8aae83b40248d21845b7579a970b.jpg" width="220" alt="Adi Music Search Screen">
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <b>🎵 Your Library</b><br><br>
      <img src="https://i.postimg.cc/9QTDbfZ1/Screenshot-2026-10-25-14-07-17-90-752d8aae83b40248d21845b7579a970b.jpg" width="220" alt="Adi Music Library Screen">
    </td>
    <td align="center" width="50%">
      <b>🎧 Now Playing</b><br><br>
      <img src="https://i.postimg.cc/nLJCPtGT/Screenshot-2026-10-25-14-35-13-39-752d8aae83b40248d21845b7579a970b.jpg" width="220" alt="Adi Music Player Screen">
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <b>🎚️ Audio Equalizer & FX</b><br><br>
      <img src="https://i.postimg.cc/d0YLxKj6/Screenshot-2026-10-25-14-35-30-75-752d8aae83b40248d21845b7579a970b.jpg" width="220" alt="Adi Music Equalizer Screen">
    </td>
    <td align="center" width="50%">
      <b>📊 Adi Wrapped & Analytics</b><br><br>
      <img src="https://i.postimg.cc/bvhsBj9W/Screenshot-2026-10-25-14-35-41-97-752d8aae83b40248d21845b7579a970b.jpg" width="220" alt="Adi Music Analytics Screen">
    </td>
  </tr>
</table>

## ✨ Features

- 🏠 **Home Screen** — Music categories, recommendations, quick picks, and trending charts.
- 🔍 **Search Screen** — Interface for finding songs, artists, and albums.
- 🎶 **Your Library** — Sections for liked songs, playlists, downloads, and local audio.
- 🎧 **Music Player** — Playback interface with play/pause, next/previous, shuffle, and repeat controls.
- 🎚️ **Audio Equalizer & FX** — Five-band graphic equalizer interface and audio effect controls.
- 📊 **Adi Wrapped & Analytics** — Listening statistics, top artists, and music trends.
- 🌑 **Dark Theme** — Black background with green accents.
- 📱 **Bottom Navigation** — Home, Search, and Your Library.

## 🛠️ Technology Stack

- **Kotlin** — Android application programming language.
- **Jetpack Compose** — Native Android UI toolkit.
- **Material 3** — UI components and styling.
- **AndroidX Media3 / ExoPlayer** — Planned or integrated audio playback engine.
- **Room Database** — Planned local storage for playlists and listening history.

## 📂 Suggested Repository Structure

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
2. Open the project in Android Studio.
3. Wait for Gradle sync to complete.
4. Select an Android emulator or connect a compatible device.
5. Press **Run** to launch the application.

> Requirements depend on the Android Gradle Plugin and SDK versions configured in the project.

## 🚧 Development Status

The screenshots show the intended app interface. The availability of each feature depends on its implementation in the Android project.

Potential areas for further development:

- Real-time song and artist search using a suitable music catalogue API.
- Actual playback using AndroidX Media3 / ExoPlayer.
- Persistent playlists and liked songs using Room.
- Local device audio browsing.
- Working equalizer and audio effects.
- Lyrics synchronization.
- Background playback and media notifications.
- Listening analytics and statistics.
- Offline playback where supported and legally permitted by the music source.

## 📸 Screenshot Information

The screenshots above use externally hosted image URLs. If a screenshot does not load, check whether its image-hosting link is still accessible.

To keep screenshots available long-term, you can also upload the six images into the repository's `screenshots/` directory and update the image links to use the repository files.

## 🤝 Contributing

Suggestions, bug reports, and improvements are welcome. Test your changes before submitting a pull request.

## 📄 License

See [LICENSE](LICENSE) for the applicable project license. Add a license file to the repository if one does not already exist.

---

**Adi Music — Your music, your vibe. 🎧**
