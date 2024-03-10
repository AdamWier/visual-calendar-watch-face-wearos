package com.example.android.wearable.alpha.Calendar

import android.content.Context
import android.util.Log
import com.android.volley.Request

import com.android.volley.toolbox.Volley
import com.example.android.wearable.alpha.R
import com.example.android.wearable.alpha.getZonedDateTime
import com.example.android.wearable.alpha.notification.NotificationCreator
import com.example.android.wearable.alpha.scheduling.Scheduler
import com.google.gson.JsonObject
import com.vdurmont.emoji.EmojiParser
import java.time.Duration
import java.time.ZonedDateTime

class Calendar(applicationContext: Context, private val notificationCreator: NotificationCreator, private val scheduler: Scheduler) {
    private val apiKey = applicationContext.getString(R.string.CALENDAR_API_KEY)
    private val apiRequestQueue = Volley.newRequestQueue(applicationContext)
    private var requestInProgress = false

    private val eventList: MutableList<JsonObject> = mutableListOf()
    var emoji: String = ""
    var summaryText: String = ""
    private val currentEvent get() = this.eventList.find { it.get("start")
        .asJsonObject.get("dateTime") != null } ?: null

    fun getPercentage (zonedDateTime: ZonedDateTime): Float {
            if(this.currentEvent == null) return 0F
            val startDateTime = getZonedDateTime(this.currentEvent!!.get("start").asJsonObject)
            val endDateTime = getZonedDateTime(this.currentEvent!!.get("end").asJsonObject)

            val eventDuration = Duration.between(startDateTime, endDateTime)
            val elapsedDuration = Duration.between(startDateTime, zonedDateTime)
            val percentageDecimal = elapsedDuration.seconds.toFloat() / eventDuration.seconds.toFloat()

            return Math.max(percentageDecimal * 100, 0F)
    }


    private fun separateEmojiFromSummary() {
        val event = this.eventList.find { it.get("start")
            .asJsonObject.get("dateTime") != null }
            ?: return
        val summary = event.get("summary").asJsonPrimitive.asString
        this.emoji = EmojiParser.extractEmojis(summary)[0] ?: ""
        val summaryWithFirstEmojiRemoved = EmojiParser.parseToAliases(summary).replaceFirst(Regex(":(\\w|-)+:"), "")
        this.summaryText = EmojiParser.parseToUnicode(summaryWithFirstEmojiRemoved)
    }

    fun getCalendarInfo() {
        if(requestInProgress) return
        requestInProgress = true
        this.emoji = "⏳"
        this.summaryText = "LOADING"
        val request = GsonRequest(
            url = "https://us-central1-watch-ea9b9.cloudfunctions.net/mycalendar?KEY=${this.apiKey}",
            clazz = Array<JsonObject>::class.java,
            method = Request.Method.GET,
            listener = {
                this.requestInProgress = false
                this.eventList.clear()
                this.scheduler.cancelAllTasks()
                this.eventList.addAll(it)
                this.separateEmojiFromSummary()
                val notificationTasks = this.notificationCreator.createNotificationTasks(this.currentEvent)
                notificationTasks.forEach(this.scheduler::scheduleTask)
            },
            errorListener = {
                this.emoji = "⚠"
                this.summaryText = it.cause?.message ?: "ERROR"
                requestInProgress = false
            })
        this.apiRequestQueue.add(request)
    }
}
