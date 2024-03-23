package com.example.android.wearable.visualScheduleWatchface.calendar

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.android.wearable.visualScheduleWatchface.R
import com.google.gson.JsonObject

class CalendarRequester(applicationContext: Context) {
    private val apiKey = applicationContext.getString(R.string.CALENDAR_API_KEY)
    private val apiRequestQueue = Volley.newRequestQueue(applicationContext)
    var requestInProgress = false
        private set

    fun makeRequest(successCallback: (Array<JsonObject>) -> Unit, errorCallback: (VolleyError) -> Unit){
        if(requestInProgress) return
        requestInProgress = true

        val request = GsonRequest(
            url = "https://us-central1-watch-ea9b9.cloudfunctions.net/mycalendar?KEY=${this.apiKey}",
            clazz = Array<JsonObject>::class.java,
            method = Request.Method.GET,
            listener = {
                this.requestInProgress = false
                successCallback(it)
            },
            errorListener = {
                requestInProgress = false
                errorCallback(it)
            })
        this.apiRequestQueue.add(request)
    }
}
