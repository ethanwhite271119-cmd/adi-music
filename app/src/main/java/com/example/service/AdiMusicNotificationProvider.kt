package com.example.service

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.example.R
import com.google.common.collect.ImmutableList

@OptIn(UnstableApi::class)
class AdiMusicNotificationProvider(private val context: Context) : MediaNotification.Provider {

    private val defaultProvider = DefaultMediaNotificationProvider.Builder(context)
        .setChannelId(NotificationHelper.CHANNEL_ID)
        .setNotificationId(NotificationHelper.NOTIFICATION_ID)
        .build().apply {
            setSmallIcon(R.drawable.ic_notification)
        }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        return defaultProvider.createNotification(
            mediaSession,
            customLayout,
            actionFactory,
            onNotificationChangedCallback
        )
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        if (action == AdiMusicService.CUSTOM_COMMAND_LIKE) {
            return true
        }
        return false
    }
}
