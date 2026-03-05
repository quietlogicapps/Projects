package com.quietlogic.allisok.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quietlogic.allisok.alarm.AlarmActivity
import com.quietlogic.allisok.alarm.engine.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val careName = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val instruction = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT) ?: ""
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, 0)

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmActivity.EXTRA_TIME_TEXT, "--:--")
            putExtra(AlarmActivity.EXTRA_CARE_NAME, careName)
            putExtra(AlarmActivity.EXTRA_INSTRUCTION, instruction)
            putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(alarmIntent)
    }
}