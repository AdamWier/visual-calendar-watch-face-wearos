package com.example.android.wearable.visualScheduleWatchface.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.example.android.wearable.visualScheduleWatchface.getZonedDateTime
import com.google.gson.JsonObject
import java.time.Duration
import java.time.ZonedDateTime

class NotificationCreator(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntents = mutableListOf<PendingIntent>()

    private val percentagesForNotifications: List<Notification> = listOf(
        Notification(50, "Halfway there!"),
        Notification(75, "Three quarters!"),
        Notification(90, "Start wrapping up!"),
        Notification(100, "Done!")
    )

    private fun getTimeAtPercentage(percentage: Long, currentEvent: JsonObject): ZonedDateTime? {
        if(currentEvent == null) return null
        val startDateTime = getZonedDateTime(currentEvent.get("start").asJsonObject)
        val endDateTime = getZonedDateTime(currentEvent.get("end").asJsonObject)

        val eventDuration = Duration.between(startDateTime, endDateTime)
        val eventDurationAtPercentage = eventDuration.multipliedBy(percentage).dividedBy(100)
        return startDateTime.plus(eventDurationAtPercentage)
    }

    private fun getSecondsFromNow(notification: Notification, currentEvent: JsonObject): Long {
        val timeAtPercentage = this.getTimeAtPercentage(notification.percentage, currentEvent)

        val now = ZonedDateTime.now()
        return Duration.between(now, timeAtPercentage).toMillis()
    }

    private fun createNotificationPendingIntent(notification: Notification): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("message", notification.message)

        return PendingIntent.getBroadcast(
            context,
            notification.percentage.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun clearNotificationAlarms(){
        this.pendingIntents.forEach{
            alarmManager.cancel(it)
        }
        this.pendingIntents.clear()
    }

    fun createNotifications(currentEvent: JsonObject?){
        if(currentEvent == null) return

        return this.percentagesForNotifications.forEach{
            val secondsFromNow = this.getSecondsFromNow(it, currentEvent)
            if(secondsFromNow <= 0) {
                return
            }
            val pendingIntent = this.createNotificationPendingIntent(it)
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + secondsFromNow,
                pendingIntent
            )
            this.pendingIntents.add(pendingIntent)
        }
    }
}
