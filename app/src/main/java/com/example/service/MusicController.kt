package com.example.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.session.MediaSession
import com.example.MainActivity
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MusicController(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AdiMusic/1.0")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(25000)
        .setKeepPostFor302Redirects(true)

    private val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

    private val extractorsFactory = DefaultExtractorsFactory()
        .setConstantBitrateSeekingEnabled(true)

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setLooper(Looper.getMainLooper())
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory))
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    val bluetoothDeviceManager = BluetoothDeviceManager(
        context = context,
        onPauseRequested = {
            if (player.isPlaying) {
                player.pause()
            }
        },
        onResumeRequested = {
            if (!player.isPlaying && _currentSong.value != null) {
                player.play()
            }
        }
    )
    val bluetoothDeviceInfo: StateFlow<BluetoothDeviceInfo> = bluetoothDeviceManager.deviceInfo

    val mediaSession: MediaSession by lazy {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        MediaSession.Builder(context, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(1.0f)
    val playbackPitch: StateFlow<Float> = _playbackPitch.asStateFlow()

    private val _sleepTimerMinutesLeft = MutableStateFlow(0)
    val sleepTimerMinutesLeft: StateFlow<Int> = _sleepTimerMinutesLeft.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow(0L)
    val sleepTimerSecondsLeft: StateFlow<Long> = _sleepTimerSecondsLeft.asStateFlow()

    private val _isSleepTimerEndOfTrack = MutableStateFlow(false)
    val isSleepTimerEndOfTrack: StateFlow<Boolean> = _isSleepTimerEndOfTrack.asStateFlow()

    // Equalizer state
    private val _equalizerBands = MutableStateFlow(listOf(0, 0, 0, 0, 0)) // 5 bands in dB (-10 to +10)
    val equalizerBands: StateFlow<List<Int>> = _equalizerBands.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(0) // 0 to 1000
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private var sleepTimerJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    try {
                        AdiMusicService.startService(context)
                    } catch (_: Exception) {}
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        val playerDur = player.duration
                        if (playerDur > 0L && playerDur != C.TIME_UNSET) {
                            _duration.value = playerDur
                        } else if ((_currentSong.value?.durationMs ?: 0L) > 0L) {
                            _duration.value = _currentSong.value!!.durationMs
                        }
                        attachAudioEffects()
                    }
                    Player.STATE_ENDED -> {
                        if (_isSleepTimerEndOfTrack.value) {
                            cancelSleepTimer()
                            player.pause()
                        } else {
                            skipNext()
                        }
                    }
                    Player.STATE_BUFFERING -> {
                        // Keep position and duration stable
                    }
                    Player.STATE_IDLE -> {
                        // Player idle
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                android.util.Log.e("MusicController", "ExoPlayer error [${error.errorCodeName}]: ${error.message}", error)
                scope.launch {
                    delay(1000)
                    if (_isPlaying.value && player.playbackState == Player.STATE_IDLE) {
                        if (_queue.value.size > 1) {
                            skipNext()
                        } else {
                            player.prepare()
                            player.play()
                        }
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (_isSleepTimerEndOfTrack.value && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    cancelSleepTimer()
                    player.pause()
                }
                val mediaId = mediaItem?.mediaId
                val found = _queue.value.find { it.id == mediaId }
                if (found != null) {
                    _currentSong.value = found
                    _currentPosition.value = 0L
                    if (found.durationMs > 0L) {
                        _duration.value = found.durationMs
                    }
                }
            }
        })

        // Position ticker coroutine with smooth real-time position & duration updates
        scope.launch {
            while (isActive) {
                try {
                    val state = player.playbackState
                    if (state == Player.STATE_READY || state == Player.STATE_BUFFERING || player.isPlaying) {
                        val pos = player.currentPosition.coerceAtLeast(0L)
                        _currentPosition.value = pos

                        val dur = player.duration
                        if (dur > 0L && dur != C.TIME_UNSET) {
                            _duration.value = dur
                        } else if (_duration.value <= 0L && (_currentSong.value?.durationMs ?: 0L) > 0L) {
                            _duration.value = _currentSong.value!!.durationMs
                        }
                    }
                } catch (_: Exception) {}
                delay(200)
            }
        }

        bluetoothDeviceManager.startListening()
    }

    private fun attachAudioEffects() {
        try {
            val audioSessionId = player.audioSessionId
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                if (equalizer == null) {
                    equalizer = Equalizer(0, audioSessionId).apply {
                        enabled = true
                    }
                }
                if (bassBoost == null) {
                    bassBoost = BassBoost(0, audioSessionId).apply {
                        enabled = true
                        setStrength(_bassBoostStrength.value.toShort())
                    }
                }
            }
        } catch (_: Exception) {
            // Virtual/emulated environments may not expose audio session hardware
        }
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList()) {
        val newQueue = if (playlist.isNotEmpty()) playlist else listOf(song)
        _queue.value = newQueue
        _currentSong.value = song
        _currentPosition.value = 0L
        _duration.value = if (song.durationMs > 0L) song.durationMs else 180000L

        val mediaItems = newQueue.map { s ->
            val builder = MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(Uri.parse(s.streamUrl))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(if (s.artworkUri.isNotBlank()) Uri.parse(s.artworkUri) else null)
                        .build()
                )

            val mimeType = when {
                s.streamUrl.endsWith(".mp3", ignoreCase = true) -> MimeTypes.AUDIO_MPEG
                s.streamUrl.contains(".m4a", ignoreCase = true) || s.streamUrl.contains(".mp4", ignoreCase = true) -> MimeTypes.AUDIO_MP4
                s.streamUrl.contains(".aac", ignoreCase = true) -> MimeTypes.AUDIO_AAC
                s.streamUrl.contains("audio-ssl.itunes.apple.com") -> MimeTypes.AUDIO_MP4
                else -> null
            }
            if (mimeType != null) {
                builder.setMimeType(mimeType)
            }
            builder.build()
        }

        player.setMediaItems(mediaItems)
        val targetIndex = newQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.seekTo(targetIndex, 0L)
        player.prepare()
        player.play()

        try {
            AdiMusicService.startService(context)
        } catch (_: Exception) {}
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                player.prepare()
            }
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            val currentIdx = _queue.value.indexOfFirst { it.id == _currentSong.value?.id }
            if (currentIdx != -1 && currentIdx + 1 < _queue.value.size) {
                playSong(_queue.value[currentIdx + 1], _queue.value)
            } else if (_repeatMode.value == Player.REPEAT_MODE_ALL && _queue.value.isNotEmpty()) {
                playSong(_queue.value.first(), _queue.value)
            }
        }
    }

    fun skipPrevious() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            val currentIdx = _queue.value.indexOfFirst { it.id == _currentSong.value?.id }
            if (currentIdx > 0) {
                playSong(_queue.value[currentIdx - 1], _queue.value)
            } else {
                player.seekTo(0)
            }
        }
    }

    fun toggleShuffle() {
        val next = !_isShuffle.value
        _isShuffle.value = next
        player.shuffleModeEnabled = next
    }

    fun cycleRepeatMode() {
        val next = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = next
        player.repeatMode = next
    }

    fun setSpeedAndPitch(speed: Float, pitch: Float) {
        _playbackSpeed.value = speed
        _playbackPitch.value = pitch
        player.playbackParameters = PlaybackParameters(speed, pitch)
    }

    fun setEqualizerBand(bandIndex: Int, gainDb: Int) {
        val updated = _equalizerBands.value.toMutableList()
        if (bandIndex in updated.indices) {
            updated[bandIndex] = gainDb
            _equalizerBands.value = updated
            try {
                equalizer?.let { eq ->
                    if (bandIndex < eq.numberOfBands) {
                        val mB = (gainDb * 100).toShort()
                        eq.setBandLevel(bandIndex.toShort(), mB)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setBassBoost(strength: Int) {
        _bassBoostStrength.value = strength
        try {
            bassBoost?.setStrength(strength.toShort())
        } catch (_: Exception) {}
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return

        _isSleepTimerEndOfTrack.value = false
        _sleepTimerMinutesLeft.value = minutes
        _sleepTimerSecondsLeft.value = minutes * 60L

        sleepTimerJob = scope.launch {
            var remainingSec = minutes * 60L
            while (remainingSec > 0) {
                delay(1000L)
                remainingSec--
                _sleepTimerSecondsLeft.value = remainingSec
                _sleepTimerMinutesLeft.value = ((remainingSec + 59) / 60).toInt()

                // Gentle volume fade-out during final 10 seconds
                if (remainingSec in 1..10) {
                    val fadeVolume = (remainingSec.toFloat() / 10f).coerceIn(0.05f, 1.0f)
                    player.volume = fadeVolume
                }
            }
            player.pause()
            player.volume = 1.0f
            _isPlaying.value = false
            _sleepTimerMinutesLeft.value = 0
            _sleepTimerSecondsLeft.value = 0L
            _isSleepTimerEndOfTrack.value = false
        }
    }

    fun startSleepTimerEndOfTrack() {
        cancelSleepTimer()
        _isSleepTimerEndOfTrack.value = true
        _sleepTimerMinutesLeft.value = -1 // Special indicator for 'End of track'
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        player.volume = 1.0f
        _sleepTimerMinutesLeft.value = 0
        _sleepTimerSecondsLeft.value = 0L
        _isSleepTimerEndOfTrack.value = false
    }

    fun release() {
        try {
            bluetoothDeviceManager.stopListening()
            mediaSession.release()
            equalizer?.release()
            bassBoost?.release()
            player.release()
        } catch (_: Exception) {}
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        private var lastHeadsetHookTime = 0L
        private var headsetHookClickCount = 0
        private val hookHandler = Handler(Looper.getMainLooper())
        private val hookRunnable = Runnable {
            when (headsetHookClickCount) {
                1 -> togglePlayPause()
                2 -> skipNext()
                3 -> skipPrevious()
                else -> togglePlayPause()
            }
            headsetHookClickCount = 0
        }

        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }

            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        player.play()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                        player.pause()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        togglePlayPause()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        skipNext()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        skipPrevious()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_STOP -> {
                        player.pause()
                        return true
                    }
                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        val now = SystemClock.uptimeMillis()
                        if (now - lastHeadsetHookTime < 400L) {
                            headsetHookClickCount++
                        } else {
                            headsetHookClickCount = 1
                        }
                        lastHeadsetHookTime = now
                        hookHandler.removeCallbacks(hookRunnable)
                        hookHandler.postDelayed(hookRunnable, 350L)
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                        seekTo((player.currentPosition + 10000L).coerceAtMost(player.duration))
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_REWIND -> {
                        seekTo((player.currentPosition - 10000L).coerceAtLeast(0L))
                        return true
                    }
                }
            }
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }
    }
}
