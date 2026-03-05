package com.quietlogic.allisok.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class AlarmActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TIME_TEXT = "extra_time_text"
        const val EXTRA_CARE_NAME = "extra_care_name"
        const val EXTRA_INSTRUCTION = "extra_instruction"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show on lock screen + wake device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_alarm)

        val timeText = intent.getStringExtra(EXTRA_TIME_TEXT) ?: "--:--"
        val careName = intent.getStringExtra(EXTRA_CARE_NAME) ?: ""
        val instruction = intent.getStringExtra(EXTRA_INSTRUCTION) ?: ""
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        findViewById<TextView>(R.id.textAlarmTime).text = timeText
        findViewById<TextView>(R.id.textCareName).text = careName
        findViewById<TextView>(R.id.textInstruction).text = instruction

        findViewById<Button>(R.id.buttonTaken).setOnClickListener {
            sendSimpleBroadcast("com.quietlogic.allisok.ALARM_TAKEN", requestCode)
            finish()
        }

        findViewById<Button>(R.id.buttonSnooze5).setOnClickListener { snoozeMinutes(5, requestCode, timeText, careName, instruction) }
        findViewById<Button>(R.id.buttonSnooze10).setOnClickListener { snoozeMinutes(10, requestCode, timeText, careName, instruction) }
        findViewById<Button>(R.id.buttonSnooze15).setOnClickListener { snoozeMinutes(15, requestCode, timeText, careName, instruction) }
    }

    private fun snoozeMinutes(
        minutes: Int,
        requestCode: Int,
        timeText: String,
        careName: String,
        instruction: String
    ) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L

        val i = Intent(this, AlarmActivity::class.java).apply {
            putExtra(EXTRA_TIME_TEXT, timeText)
            putExtra(EXTRA_CARE_NAME, careName)
            putExtra(EXTRA_INSTRUCTION, instruction)
            putExtra(EXTRA_REQUEST_CODE, requestCode)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val pi = PendingIntent.getActivity(this, requestCode, i, flags)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            @Suppress("DEPRECATION")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }

        sendSimpleBroadcast("com.quietlogic.allisok.ALARM_SNOOZED_$minutes", requestCode)
        finish()
    }

    private fun sendSimpleBroadcast(action: String, requestCode: Int) {
        sendBroadcast(Intent(action).apply {
            putExtra(EXTRA_REQUEST_CODE, requestCode)
        })
    }
}