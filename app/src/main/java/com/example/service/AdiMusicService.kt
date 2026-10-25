package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.AdiMusicApplication

@OptIn(UnstableApi::class)
class AdiMusicService : MediaSessionService() {

    private var fallbackSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        setMediaNotificationProvider(AdiMusicNotificationProvider(this))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        val app = application as? AdiMusicApplication
        val mainSession = app?.musicController?.mediaSession
        if (mainSession != null) {
            return mainSession
        }
        // Resilient fallback session to guarantee non-null return if accessed in isolation
        if (fallbackSession == null) {
            val fallbackPlayer = ExoPlayer.Builder(this)
                .setLooper(Looper.getMainLooper())
                .build()
            fallbackSession = MediaSession.Builder(this, fallbackPlayer).build()
        }
        return fallbackSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val app = application as? AdiMusicApplication
        val player = app?.musicController?.mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        fallbackSession?.run {
            player.release()
            release()
            fallbackSession = null
        }
        super.onDestroy()
    }

    companion object {
        const val CUSTOM_COMMAND_LIKE = "com.adimusic.app.ACTION_LIKE"

        fun startService(context: Context) {
            try {
                val intent = Intent(context, AdiMusicService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, AdiMusicService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
