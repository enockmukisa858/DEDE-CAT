package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.MusicRepository
import com.example.data.local.AppDatabase

class BlackDudeMusicApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var musicRepository: MusicRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        musicRepository = MusicRepository(this, database)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BLACK DUDE MUSIC Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls and information"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "black_dude_music_playback_channel"
        lateinit var instance: BlackDudeMusicApplication
            private set
    }
}
