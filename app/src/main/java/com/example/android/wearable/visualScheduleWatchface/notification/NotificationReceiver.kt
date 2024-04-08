/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.visualScheduleWatchface.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationReceiver: BroadcastReceiver() {
    private val wearableExtender = NotificationCompat.WearableExtender()
    private val channel = NotificationChannel("percentage", "Watch Percentage Notification", NotificationManager.IMPORTANCE_DEFAULT)

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.createNotificationChannel(this.channel)

        val notificationBuilder = NotificationCompat.Builder(
            context,
            this.channel.id
        ).setSmallIcon(android.R.drawable.alert_dark_frame)
            .setContentTitle(intent.getStringExtra("message"))
            .extend(this.wearableExtender)
            .build()

        notificationManager.notify(1, notificationBuilder)


    }

}
