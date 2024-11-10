package com.example.android.wearable.visualScheduleWatchface.calendar

import java.time.ZonedDateTime

class EventItem(val start: ZonedDateTime, val end: ZonedDateTime, private val summary: String) {
}
