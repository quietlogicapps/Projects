package com.quietlogic.allisok.alarm.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.AlarmActivity
import com.quietlogic.allisok.alarm.engine.AlarmScheduler
import java.time.LocalDate
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != AlarmScheduler.ACTION_CARE_ALARM) return

        val timeText = intent.getStringExtra(AlarmScheduler.EXTRA_TIME_TEXT) ?: "--:--"
        val careName = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val instruction = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT) ?: ""
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, 0)

        val careItemId = intent.getLongExtra(
            AlarmScheduler.EXTRA_CARE_ITEM_ID,
            -1L
        )

        val logDate = LocalDate.now().toString()

        val logTime = runCatching {
            LocalTime.parse(timeText)
        }.getOrElse {
            LocalTime.now()
        }.toString()

        val activityIntent = Intent(context, AlarmActivity::class.java).apply {

            putExtra(AlarmActivity.EXTRA_TIME_TEXT, timeText)
            putExtra(AlarmActivity.EXTRA_CARE_NAME, careName)
            putExtra(AlarmActivity.EXTRA_INSTRUCTION, instruction)
            putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode)

            putExtra(AlarmActivity.EXTRA_CARE_ITEM_ID, careItemId)
            putExtra(AlarmActivity.EXTRA_LOG_DATE, logDate)
            putExtra(AlarmActivity.EXTRA_LOG_TIME, logTime)

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val piFlags =
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        PendingIntent.FLAG_IMMUTABLE else 0)

        val fullScreenPi = PendingIntent.getActivity(
            context,
            requestCode,
            activityIntent,
            piFlags
        )

        val nm =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?: return

        ensureChannel(nm)

        val notificationId =
            if (requestCode != 0)
                requestCode
            else
                (System.currentTimeMillis() and 0x7fffffff).toInt()

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
            .build()

        nm.notify(notificationId, notification)
    }

    private fun ensureChannel(nm: NotificationManager) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val existing = nm.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val soundUri: Uri = Settings.System.DEFAULT_RINGTONE_URI

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarms (ringtone + vibrate)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {

            description = "Care alarms (uses phone ringtone + vibration)"
            setSound(soundUri, attrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 800)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        nm.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "all_is_ok_alarm_ringtone_v2"
    }
}