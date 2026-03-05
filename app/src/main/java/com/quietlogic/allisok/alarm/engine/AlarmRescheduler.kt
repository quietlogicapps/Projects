package com.quietlogic.allisok.alarm.engine

import android.content.Context

class AlarmRescheduler(private val context: Context) {

    fun rescheduleAll() {
        AlarmPlanner(context).scheduleSimpleTestAlarm()
    }
}