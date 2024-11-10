package com.example.android.wearable.visualScheduleWatchface.calendar

import android.content.Context
import android.util.Log
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
    private val eventList: MutableList<EventItem> = mutableListOf()
    var emoji: String = ""
    var summaryText: String = ""
    var endText: String = ""

    private val currentEvent get() = this.eventList.getOrNull(0);

    private fun getEndText(){
        if(this.currentEvent == null) return
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
        val text = this.currentEvent!!.end.format(timeFormatter)
        this.endText = "Finishes @ $text"
    }

    fun getPercentage (zonedDateTime: ZonedDateTime): Float {
            if(this.currentEvent == null) return 0F
            val startDateTime = this.currentEvent!!.start
            val endDateTime = this.currentEvent!!.end

            val eventDuration = Duration.between(startDateTime, endDateTime)
            val elapsedDuration = Duration.between(startDateTime, zonedDateTime)
            val percentageDecimal = elapsedDuration.seconds.toFloat() / eventDuration.seconds.toFloat()

            return (percentageDecimal * 100).coerceAtLeast(0F)
    }


    private fun separateEmojiFromSummary() {
        if(this.currentEvent == null) return
        val summary = this.currentEvent!!.summary
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
        if(this.requestInProgress) return
        this.requestInProgress = true;
        AsyncDav(applicationContext).execute(::onRequestSuccess)
    }

    private fun onRequestSuccess(eventItems: Array<EventItem>){
        this.requestInProgress = false
        this.eventList.clear()
        this.eventList.addAll(eventItems)
        this.separateEmojiFromSummary()
        this.getEndText()
        if(this.currentEvent != null){
            this.notificationCreator.createNotifications(this.currentEvent!!)

        }
    }

    private fun onRequestError(error: VolleyError){
        this.requestInProgress = false
        this.emoji = "⚠"
        this.summaryText = error.message ?: "ERROR"
    }
}
