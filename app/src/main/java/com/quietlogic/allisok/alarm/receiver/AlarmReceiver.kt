package com.quietlogic.allisok.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quietlogic.allisok.alarm.engine.AlarmScheduler
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val text = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT) ?: "Care reminder"

        val notificationId = intent.getIntExtra(
            AlarmScheduler.EXTRA_REQUEST_CODE,
            Random.nextInt()
        )

        NotificationHelper.showAlarmNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            text = text
        )
    }
}