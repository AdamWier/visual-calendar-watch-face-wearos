package com.example.android.wearable.visualScheduleWatchface

import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.WatchFace
import com.example.android.wearable.visualScheduleWatchface.Calendar.Calendar

class TapListener(private val calendar: Calendar): WatchFace.TapListener {
    override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
        this.calendar.getCalendarInfo()
    }
}
