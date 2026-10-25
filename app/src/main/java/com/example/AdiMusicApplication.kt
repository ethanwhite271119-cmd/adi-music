package com.example

import android.app.Application
import com.example.data.repository.MusicRepository
import com.example.service.MusicController
import com.example.service.NotificationHelper

class AdiMusicApplication : Application() {
    lateinit var musicRepository: MusicRepository
        private set

    lateinit var musicController: MusicController
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        musicRepository = MusicRepository(this)
        musicController = MusicController(this)
    }

    override fun onTerminate() {
        musicController.release()
        super.onTerminate()
    }
}
