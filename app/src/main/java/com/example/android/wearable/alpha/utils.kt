package com.example.android.wearable.alpha

import com.google.gson.JsonObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun getZonedDateTime(googleDateTime: JsonObject): ZonedDateTime {
    val dateTimeString = googleDateTime.get("dateTime").asJsonPrimitive.asString.split('+')[0]
    val dateTimeTimeZoneString = googleDateTime.get("timeZone").asJsonPrimitive.asString

    val localDateTime = LocalDateTime.parse(dateTimeString)
    val zoneId = ZoneId.of(dateTimeTimeZoneString)
    return localDateTime.atZone(zoneId)
}
