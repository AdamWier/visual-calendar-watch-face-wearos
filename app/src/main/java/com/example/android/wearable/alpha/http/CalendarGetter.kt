package com.example.android.wearable.alpha.http;

import android.content.Context;
import android.util.Log
import com.android.volley.Request

import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject

class CalendarGetter(applicationContext: Context) {
    private val apiRequestQueue = Volley.newRequestQueue(applicationContext)

    var cal: Array<JsonObject>? = null

    fun getCalendarInfo() {
        val request = GsonRequest(
            url = "https://us-central1-watch-ea9b9.cloudfunctions.net/mycalendar?KEY=cb991ef5-c8ee-4d7f-96d2-2ec37d114058",
            clazz = Array<JsonObject>::class.java,
            method = Request.Method.GET,
            listener = {
                this.cal = it
            },
            errorListener = {
                Log.i("Volley", it.toString())
            })
        this.apiRequestQueue.add(request)
    }
}
