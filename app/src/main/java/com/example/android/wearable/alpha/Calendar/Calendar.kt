package com.example.android.wearable.alpha.Calendar;

import android.content.Context;
import android.util.Log
import com.android.volley.Request

import com.android.volley.toolbox.Volley;
import com.example.android.wearable.alpha.R
import com.google.gson.JsonObject
import com.vdurmont.emoji.EmojiParser
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class Calendar(applicationContext: Context) {
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
            val startDateTime = this.getZonedDateTime(this.currentEvent!!.get("start").asJsonObject)
            val endDateTime = this.getZonedDateTime(this.currentEvent!!.get("end").asJsonObject)

            val eventDuration = Duration.between(startDateTime, endDateTime)
            val elapsedDuration = Duration.between(startDateTime, zonedDateTime)
            val percentageDecimal = elapsedDuration.seconds.toFloat() / eventDuration.seconds.toFloat()

            return percentageDecimal * 100
    }

    private fun getZonedDateTime(googleDateTime: JsonObject): ZonedDateTime {
        val dateTimeString = googleDateTime.get("dateTime").asJsonPrimitive.asString.split('+')[0]
        val dateTimeTimeZoneString = googleDateTime.get("timeZone").asJsonPrimitive.asString

        val localDateTime = LocalDateTime.parse(dateTimeString)
        val zoneId = ZoneId.of(dateTimeTimeZoneString)
        return localDateTime.atZone(zoneId)
    }

    private fun separateEmojiFromSummary() {
        //replace ?
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
        Log.i("Volley", "called")
        val request = GsonRequest(
            url = "https://us-central1-watch-ea9b9.cloudfunctions.net/mycalendar?KEY=${this.apiKey}",
            clazz = Array<JsonObject>::class.java,
            method = Request.Method.GET,
            listener = {
                this.eventList.clear()
                this.eventList.addAll(it)
                this.separateEmojiFromSummary()
                this.requestInProgress = false
            },
            errorListener = {
                Log.i("Volley", it.toString())
                requestInProgress = false
            })
        this.apiRequestQueue.add(request)
    }
}
