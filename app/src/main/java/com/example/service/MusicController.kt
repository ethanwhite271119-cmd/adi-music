package com.example.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
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

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setLooper(Looper.getMainLooper())
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    val mediaSession: MediaSession by lazy {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        MediaSession.Builder(context, player)
            .setSessionActivity(sessionActivityPendingIntent)
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
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0L)
                    attachAudioEffects()
                } else if (playbackState == Player.STATE_ENDED) {
                    skipNext()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId
                val found = _queue.value.find { it.id == mediaId }
                if (found != null) {
                    _currentSong.value = found
                }
            }
        })

        // Position ticker coroutine
        scope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                    _duration.value = player.duration.coerceAtLeast(0L)
                }
                delay(300)
            }
        }
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

        val mediaItems = newQueue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.id)
                .setUri(Uri.parse(s.streamUrl))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(Uri.parse(s.artworkUri))
                        .build()
                )
                .build()
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
        sleepTimerJob?.cancel()
        _sleepTimerMinutesLeft.value = minutes
        if (minutes <= 0) return

        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60000L)
                remaining--
                _sleepTimerMinutesLeft.value = remaining
            }
            player.pause()
            _isPlaying.value = false
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerMinutesLeft.value = 0
    }

    fun release() {
        try {
            mediaSession.release()
            equalizer?.release()
            bassBoost?.release()
            player.release()
        } catch (_: Exception) {}
    }
}
