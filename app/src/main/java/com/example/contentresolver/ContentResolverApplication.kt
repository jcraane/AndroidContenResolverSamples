package com.example.contentresolver

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.contentresolver.Constants.NOTIFICATION_CHANNEL_ID

class ContentResolverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Query Channel",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}