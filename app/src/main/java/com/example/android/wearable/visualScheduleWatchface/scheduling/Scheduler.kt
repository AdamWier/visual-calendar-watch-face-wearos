package com.example.android.wearable.visualScheduleWatchface.scheduling

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture

class Scheduler {
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val scheduledTasks: MutableList<ScheduledFuture<*>> = mutableListOf()

    fun cancelAllTasks() {
        this.scheduledTasks.forEach { it -> it.cancel(true)}
    }

    fun scheduleTask(task: Task) {
        val future = this.scheduler.schedule(task.runnable, task.delay, task.unit)
        this.scheduledTasks.add(future)
    }
}
