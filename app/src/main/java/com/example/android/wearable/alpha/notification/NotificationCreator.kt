package com.example.android.wearable.alpha.notification

import android.content.Context
import com.example.android.wearable.alpha.getZonedDateTime
import com.example.android.wearable.alpha.scheduling.Task
import com.google.gson.JsonObject
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class NotificationCreator(private val applicationContext: Context) {
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

    private fun getSecondsFromNow(time: ZonedDateTime?): Long {
        val now = ZonedDateTime.now()
        return Duration.between(now, time).toMillis()
    }

    private fun createNotificationTask(notification: Notification, currentEvent: JsonObject): Task? {
        val timeAtPercentage = this.getTimeAtPercentage(notification.percentage, currentEvent)
        val secondsFromNow = this.getSecondsFromNow(timeAtPercentage)
        if(secondsFromNow <= 0) {
            return null
        }
        return Task(
            NotificationSender(applicationContext, notification.message),
            secondsFromNow,
            TimeUnit.MILLISECONDS
        )
    }

    fun createNotificationTasks(currentEvent: JsonObject?): List<Task>{
        if(currentEvent == null) return listOf()

        return this.percentagesForNotifications.mapNotNull {
            this.createNotificationTask(
                it,
                currentEvent
            )
        }
    }
}
