package com.example.android.wearable.visualScheduleWatchface.calendar

import android.content.Context
import com.android.volley.VolleyError

import com.example.android.wearable.visualScheduleWatchface.getZonedDateTime
import com.example.android.wearable.visualScheduleWatchface.notification.NotificationCreator
import com.google.gson.JsonObject
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import net.fellbaum.jemoji.EmojiManager

class Calendar(private val applicationContext: Context, private val notificationCreator: NotificationCreator) {
    var requestInProgress = false
        private set
    private val eventList: MutableList<JsonObject> = mutableListOf()
    var emoji: String = ""
    var summaryText: String = ""
    var endText: String = ""

    private val currentEvent get() = this.eventList.find { it.get("start")
        .asJsonObject.get("dateTime") != null } ?: null

    private fun getEndText(){
        if(this.currentEvent == null) return
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
        val endDateTime = getZonedDateTime(this.currentEvent!!.get("end").asJsonObject)
        val text = endDateTime.format(timeFormatter)
        this.endText = "Finishes @ $text"
    }

    fun getPercentage (zonedDateTime: ZonedDateTime): Float {
            if(this.currentEvent == null) return 0F
            val startDateTime = getZonedDateTime(this.currentEvent!!.get("start").asJsonObject)
            val endDateTime = getZonedDateTime(this.currentEvent!!.get("end").asJsonObject)

            val eventDuration = Duration.between(startDateTime, endDateTime)
            val elapsedDuration = Duration.between(startDateTime, zonedDateTime)
            val percentageDecimal = elapsedDuration.seconds.toFloat() / eventDuration.seconds.toFloat()

            return (percentageDecimal * 100).coerceAtLeast(0F)
    }


    private fun separateEmojiFromSummary() {
        val event = this.eventList.find { it.get("start")
            .asJsonObject.get("dateTime") != null }
            ?: return
        val summary = event.get("summary").asJsonPrimitive.asString
        val emoji = EmojiManager.extractEmojis(summary).elementAtOrNull(0)
        this.emoji = emoji?.emoji ?: "⬜"
        this.summaryText = EmojiManager.removeEmojis(summary, emoji)
    }

    fun getCalendarInfo() {
        this.emoji = "⏳"
        this.summaryText = ""
        this.endText = "LOADING"
//        Disabled for now because removes last notification
//        this.notificationCreator.clearNotificationAlarms()
        //this.calendarRequest.makeRequest(::onRequestSuccess, ::onRequestError)
        this.requestInProgress = true;
        AsyncDav(applicationContext).execute()
    }

    private fun onRequestSuccess(responseContent: Array<JsonObject>){
        this.requestInProgress = false
        this.eventList.clear()
        this.eventList.addAll(responseContent)
        this.separateEmojiFromSummary()
        this.getEndText()
        this.notificationCreator.createNotifications(this.currentEvent)
    }

    private fun onRequestError(error: VolleyError){
        this.requestInProgress = false
        this.emoji = "⚠"
        this.summaryText = error.message ?: "ERROR"
    }
}
