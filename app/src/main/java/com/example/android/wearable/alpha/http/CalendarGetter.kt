package com.example.android.wearable.alpha.http;

import android.content.Context;
import android.util.Log
import com.android.volley.Request

import com.android.volley.toolbox.Volley;
import com.example.android.wearable.alpha.R
import com.google.gson.JsonObject

class CalendarGetter(applicationContext: Context) {
    private val apiKey = applicationContext.getString(R.string.CALENDAR_API_KEY)
    private val apiRequestQueue = Volley.newRequestQueue(applicationContext)

    val cal: MutableList<JsonObject> = mutableListOf()

    fun getCalendarInfo() {
        val request = GsonRequest(
            url = "https://us-central1-watch-ea9b9.cloudfunctions.net/mycalendar?KEY=${this.apiKey}",
            clazz = Array<JsonObject>::class.java,
            method = Request.Method.GET,
            listener = {
                this.cal.addAll(it)
            },
            errorListener = {
                Log.i("Volley", it.toString())
            })
        this.apiRequestQueue.add(request)
    }
}
