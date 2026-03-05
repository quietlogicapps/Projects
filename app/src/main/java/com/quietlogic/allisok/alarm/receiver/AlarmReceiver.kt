package com.quietlogic.allisok.alarm.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.AlarmActivity
import com.quietlogic.allisok.alarm.engine.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != AlarmScheduler.ACTION_CARE_ALARM) return

        val timeText = intent.getStringExtra(AlarmScheduler.EXTRA_TIME_TEXT) ?: "--:--"
        val careName = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val instruction = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT) ?: ""
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, 0)

        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmActivity.EXTRA_TIME_TEXT, timeText)
            putExtra(AlarmActivity.EXTRA_CARE_NAME, careName)
            putExtra(AlarmActivity.EXTRA_INSTRUCTION, instruction)
            putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val fullScreenPi = PendingIntent.getActivity(
            context,
            requestCode,
            activityIntent,
            piFlags
        )

        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return
        ensureChannel(nm)

        val notificationId =
            if (requestCode != 0) requestCode else (System.currentTimeMillis() and 0x7fffffff).toInt()

        val snooze5 = PendingIntent.getActivity(
            context,
            requestCode + 5,
            activityIntent.putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode),
            piFlags
        )

        val snooze10 = PendingIntent.getActivity(
            context,
            requestCode + 10,
            activityIntent.putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode),
            piFlags
        )

        val snooze15 = PendingIntent.getActivity(
            context,
            requestCode + 15,
            activityIntent.putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode),
            piFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(careName)
            .setContentText(instruction.ifBlank { "Alarm" })
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPi)
            .setFullScreenIntent(fullScreenPi, true)
            .addAction(0, "SNOOZE 5", snooze5)
            .addAction(0, "SNOOZE 10", snooze10)
            .addAction(0, "SNOOZE 15", snooze15)
            .build()

        nm.notify(notificationId, notification)
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Care alarms"
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        nm.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "all_is_ok_alarm"
    }
}