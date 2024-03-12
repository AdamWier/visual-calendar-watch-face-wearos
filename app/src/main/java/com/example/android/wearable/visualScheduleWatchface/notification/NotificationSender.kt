package com.example.android.wearable.visualScheduleWatchface.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService


class NotificationSender(applicationContext: Context, private val message: String): Runnable {
    private val context = applicationContext
    private val notificationManager: NotificationManager? = getSystemService(applicationContext, NotificationManager::class.java)
    private val channel = NotificationChannel("percentage", "Watch Percentage Notification", NotificationManager.IMPORTANCE_DEFAULT)
    private val wearableExtender = NotificationCompat.WearableExtender()

    init{
        val description = "Sends notifications at 50, 70, 90 and 100%"
        channel.description = description
        notificationManager?.createNotificationChannel(channel)
    }

    override fun run(){
        val notificationBuilder = NotificationCompat.Builder(
            this.context,
            this.channel.id
        ).setSmallIcon(android.R.drawable.alert_dark_frame)
        .setContentTitle(this.message)
        .extend(this.wearableExtender)

        notificationManager?.notify(1, notificationBuilder.build())
    }
}
