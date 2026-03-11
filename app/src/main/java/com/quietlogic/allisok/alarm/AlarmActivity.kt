package com.quietlogic.allisok.alarm

import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmScheduler
import com.quietlogic.allisok.alarm.receiver.AlarmTakenReceiver

class AlarmActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TIME_TEXT = "extra_time_text"
        const val EXTRA_CARE_NAME = "extra_care_name"
        const val EXTRA_INSTRUCTION = "extra_instruction"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_CARE_ITEM_ID = "extra_care_item_id"
        const val EXTRA_LOG_DATE = "extra_log_date"
        const val EXTRA_LOG_TIME = "extra_log_time"
    }

    private var ringtone: Ringtone? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val careItemId = intent.getLongExtra(EXTRA_CARE_ITEM_ID, -1L)

        val logDate = intent.getStringExtra(EXTRA_LOG_DATE) ?: ""
        val logTime = intent.getStringExtra(EXTRA_LOG_TIME) ?: ""

        findViewById<TextView>(R.id.textAlarmTime).text = timeText
        findViewById<TextView>(R.id.textCareName).text = careName
        findViewById<TextView>(R.id.textInstruction).text = instruction

        findViewById<Button>(R.id.buttonTaken).setOnClickListener {
            stopRingtone()
            sendTakenBroadcast(requestCode, careItemId, logDate, logTime)
            finish()
        }

        findViewById<Button>(R.id.buttonSnooze5).setOnClickListener {
            stopRingtone()
            snoozeMinutes(5, requestCode, careItemId, careName, instruction)
        }

        findViewById<Button>(R.id.buttonSnooze10).setOnClickListener {
            stopRingtone()
            snoozeMinutes(10, requestCode, careItemId, careName, instruction)
        }

        findViewById<Button>(R.id.buttonSnooze15).setOnClickListener {
            stopRingtone()
            snoozeMinutes(15, requestCode, careItemId, careName, instruction)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed({ startRingtone() }, 120)
    }

    override fun onPause() {
        stopRingtone()
        super.onPause()
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    private fun startRingtone() {

        if (ringtone?.isPlaying == true) return

        val uri = RingtoneManager.getActualDefaultRingtoneUri(
            this,
            RingtoneManager.TYPE_RINGTONE
        ) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val rt = RingtoneManager.getRingtone(this, uri) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rt.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }

        ringtone = rt

        try {
            rt.play()
        } catch (_: Throwable) {
        }
    }

    private fun stopRingtone() {

        handler.removeCallbacksAndMessages(null)

        try {
            ringtone?.stop()
        } catch (_: Throwable) {
        }

        ringtone = null
    }

    private fun snoozeMinutes(
        minutes: Int,
        requestCode: Int,
        careItemId: Long,
        careName: String,
        instruction: String
    ) {

        val triggerAtMillis =
            System.currentTimeMillis() + minutes * 60_000L

        AlarmScheduler(this).scheduleExact(
            triggerAtMillis = triggerAtMillis,
            careItemId = careItemId,
            requestCode = requestCode,
            title = careName.ifBlank { "Reminder" },
            text = instruction.ifBlank { "Care reminder" }
        )

        finish()
    }

    private fun sendTakenBroadcast(
        requestCode: Int,
        careItemId: Long,
        logDate: String,
        logTime: String
    ) {

        sendBroadcast(
            Intent(this, AlarmTakenReceiver::class.java).apply {

                putExtra(EXTRA_REQUEST_CODE, requestCode)
                putExtra(AlarmTakenReceiver.EXTRA_CARE_ITEM_ID, careItemId)
                putExtra(AlarmTakenReceiver.EXTRA_LOG_DATE, logDate)
                putExtra(AlarmTakenReceiver.EXTRA_LOG_TIME, logTime)
            }
        )
    }
}