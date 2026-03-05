package com.quietlogic.allisok.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.quietlogic.allisok.alarm.engine.AlarmRescheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            Log.d(
                "AllIsOK",
                "Boot completed — alarm reschedule will run here later"
            )

            AlarmRescheduler(context).rescheduleAll()

        }
    }
}