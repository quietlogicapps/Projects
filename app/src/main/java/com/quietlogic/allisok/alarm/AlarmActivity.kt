package com.quietlogic.allisok.alarm

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmScheduler

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

        findViewById<Button>(R.id.buttonSnooze5).setOnClickListener { snoozeMinutes(5, requestCode, careName, instruction) }
        findViewById<Button>(R.id.buttonSnooze10).setOnClickListener { snoozeMinutes(10, requestCode, careName, instruction) }
        findViewById<Button>(R.id.buttonSnooze15).setOnClickListener { snoozeMinutes(15, requestCode, careName, instruction) }
    }

    private fun snoozeMinutes(
        minutes: Int,
        requestCode: Int,
        careName: String,
        instruction: String
    ) {
        val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L

        val scheduler = AlarmScheduler(this)

        scheduler.scheduleExact(
            triggerAtMillis = triggerAtMillis,
            requestCode = requestCode,
            title = careName.ifBlank { "Reminder" },
            text = instruction.ifBlank { "Care reminder" }
        )

        sendSimpleBroadcast("com.quietlogic.allisok.ALARM_SNOOZED_$minutes", requestCode)
        finish()
    }

    private fun sendSimpleBroadcast(action: String, requestCode: Int) {
        sendBroadcast(android.content.Intent(action).apply {
            putExtra(EXTRA_REQUEST_CODE, requestCode)
        })
    }
}