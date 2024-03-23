package com.example.android.wearable.visualScheduleWatchface

import android.graphics.Rect
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.TapType
import androidx.wear.watchface.WatchFace
import com.example.android.wearable.visualScheduleWatchface.Calendar.Calendar

class TapListener(private val calendar: Calendar): WatchFace.TapListener {

    private val centerBounds = Rect(164,164,300,300)
    override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
        if(tapType !== TapType.UP) return

        val touchedEmoji = this.centerBounds.contains(tapEvent.xPos, tapEvent.yPos)
        if(touchedEmoji){
            this.calendar.getCalendarInfo()
        }
    }
}
