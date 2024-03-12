package com.example.android.wearable.visualScheduleWatchface.scheduling

import java.util.concurrent.TimeUnit

class Task(val runnable: Runnable, val delay: Long, val unit: TimeUnit)
