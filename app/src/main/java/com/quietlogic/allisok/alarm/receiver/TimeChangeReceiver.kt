package com.quietlogic.allisok.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.quietlogic.allisok.alarm.engine.AlarmRescheduler

class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.d(
            "AllIsOK",
            "Time or timezone changed — alarm reschedule will run here later"
        )

        AlarmRescheduler(context).rescheduleAll()

    }
}