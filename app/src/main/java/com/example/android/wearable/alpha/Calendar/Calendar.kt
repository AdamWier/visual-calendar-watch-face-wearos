package com.example.android.wearable.alpha.Calendar;

import android.content.Context;
import android.util.Log
import com.android.volley.Request

import com.android.volley.toolbox.Volley;
import com.example.android.wearable.alpha.R
import com.google.gson.JsonObject
import com.vdurmont.emoji.EmojiParser

class Calendar(applicationContext: Context) {
    private val apiKey = applicationContext.getString(R.string.CALENDAR_API_KEY)
    private val apiRequestQueue = Volley.newRequestQueue(applicationContext)

    private val eventList: MutableList<JsonObject> = mutableListOf()
    var emoji: String = ""
    var summaryText: String = ""

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
        val request = GsonRequest(
            url = "https://us-central1-watch-ea9b9.cloudfunctions.net/mycalendar?KEY=${this.apiKey}",
            clazz = Array<JsonObject>::class.java,
            method = Request.Method.GET,
            listener = {
                this.eventList.addAll(it)
                this.separateEmojiFromSummary()
            },
            errorListener = {
                Log.i("Volley", it.toString())
            })
        this.apiRequestQueue.add(request)
    }
}
