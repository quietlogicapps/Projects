package com.quietlogic.allisok.alarm

import android.app.NotificationManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmScheduler
import com.quietlogic.allisok.alarm.engine.SnoozeStore
import com.quietlogic.allisok.alarm.receiver.AlarmTakenReceiver

class AlarmActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TIME_TEXT = "extra_time_text"
        const val EXTRA_CARE_NAME = "extra_care_name"
        const val EXTRA_INSTRUCTION = "extra_instruction"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_CARE_ITEM_ID = "extra_care_item_id"
        const val EXTRA_CARE_ITEM_IDS = "extra_care_item_ids"
        const val EXTRA_CARE_ITEM_NAMES = "extra_care_item_names"
        const val EXTRA_CARE_ITEM_INSTRUCTIONS = "extra_care_item_instructions"
        const val EXTRA_LOG_DATE = "extra_log_date"
        const val EXTRA_LOG_TIME = "extra_log_time"
    }

    private var ringtone: Ringtone? = null
    private val handler = Handler(Looper.getMainLooper())
    private var audioKey: String = ""
    private var audioAcquired: Boolean = false

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
        val primaryCareItemId = intent.getLongExtra(EXTRA_CARE_ITEM_ID, -1L)

        val careItemIds = intent.getLongArrayExtra(EXTRA_CARE_ITEM_IDS)
        val careItemNames = intent.getStringArrayExtra(EXTRA_CARE_ITEM_NAMES)
        val careItemInstructions = intent.getStringArrayExtra(EXTRA_CARE_ITEM_INSTRUCTIONS)

        val logDate = intent.getStringExtra(EXTRA_LOG_DATE) ?: ""
        val logTime = intent.getStringExtra(EXTRA_LOG_TIME) ?: ""
        audioKey = "alarm|$logDate|$logTime"

        val textAlarmTime = findViewById<TextView>(R.id.textAlarmTime)
        val textCareName = findViewById<TextView>(R.id.textCareName)
        val textInstruction = findViewById<TextView>(R.id.textInstruction)

        textAlarmTime.text = timeText

        val hasGroupedItems =
            careItemIds != null &&
                    careItemIds.isNotEmpty() &&
                    careItemNames != null &&
                    careItemNames.size == careItemIds.size

        if (hasGroupedItems) {
            textCareName.text = "${careItemIds!!.size} reminders"
            textInstruction.text = careItemNames!!
                .joinToString(separator = "\n") { "• $it" }
        } else {
            textCareName.text = careName
            textInstruction.text = instruction
        }

        findViewById<Button>(R.id.buttonTaken).setOnClickListener {
            stopRingtone()
            val idsToLog = when {
                hasGroupedItems -> careItemIds!!
                primaryCareItemId > 0L -> longArrayOf(primaryCareItemId)
                else -> longArrayOf()
            }

            idsToLog.forEach { id ->
                sendTakenBroadcast(requestCode, id, logDate, logTime)
            }
            if (requestCode != 0) {
                val nm = getSystemService(NotificationManager::class.java)
                nm?.cancel(requestCode)
            }
            finish()
        }

        findViewById<Button>(R.id.buttonSnooze5).setOnClickListener {
            stopRingtone()
            if (hasGroupedItems &&
                careItemIds != null &&
                careItemNames != null &&
                careItemInstructions != null
            ) {
                Log.d("AllIsOK", "AlarmActivity.snooze5 grouped rc=$requestCode ids=${careItemIds.joinToString()}")
                snoozeMinutesGrouped(
                    minutes = 5,
                    requestCode = requestCode,
                    careItemIds = careItemIds,
                    careItemNames = careItemNames,
                    careItemInstructions = careItemInstructions
                )
            } else {
                Log.d("AllIsOK", "AlarmActivity.snooze5 single rc=$requestCode careItemId=$primaryCareItemId")
                snoozeMinutesSingle(
                    minutes = 5,
                    requestCode = requestCode,
                    careItemId = primaryCareItemId,
                    careName = careName,
                    instruction = instruction
                )
            }
        }

        findViewById<Button>(R.id.buttonSnooze10).setOnClickListener {
            stopRingtone()
            if (hasGroupedItems &&
                careItemIds != null &&
                careItemNames != null &&
                careItemInstructions != null
            ) {
                Log.d("AllIsOK", "AlarmActivity.snooze10 grouped rc=$requestCode ids=${careItemIds.joinToString()}")
                snoozeMinutesGrouped(
                    minutes = 10,
                    requestCode = requestCode,
                    careItemIds = careItemIds,
                    careItemNames = careItemNames,
                    careItemInstructions = careItemInstructions
                )
            } else {
                Log.d("AllIsOK", "AlarmActivity.snooze10 single rc=$requestCode careItemId=$primaryCareItemId")
                snoozeMinutesSingle(
                    minutes = 10,
                    requestCode = requestCode,
                    careItemId = primaryCareItemId,
                    careName = careName,
                    instruction = instruction
                )
            }
        }

        findViewById<Button>(R.id.buttonSnooze15).setOnClickListener {
            stopRingtone()
            if (hasGroupedItems &&
                careItemIds != null &&
                careItemNames != null &&
                careItemInstructions != null
            ) {
                Log.d("AllIsOK", "AlarmActivity.snooze15 grouped rc=$requestCode ids=${careItemIds.joinToString()}")
                snoozeMinutesGrouped(
                    minutes = 15,
                    requestCode = requestCode,
                    careItemIds = careItemIds,
                    careItemNames = careItemNames,
                    careItemInstructions = careItemInstructions
                )
            } else {
                Log.d("AllIsOK", "AlarmActivity.snooze15 single rc=$requestCode careItemId=$primaryCareItemId")
                snoozeMinutesSingle(
                    minutes = 15,
                    requestCode = requestCode,
                    careItemId = primaryCareItemId,
                    careName = careName,
                    instruction = instruction
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed({ startRingtone() }, 120)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        stopRingtone()
        super.onStop()
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }

    private fun startRingtone() {

        if (ringtone?.isPlaying == true) return

        val shouldPlayAudio: Boolean =
            if (audioAcquired) {
                true
            } else {
                try {
                    AlarmAudioSession.tryAcquire(audioKey).also { acquired ->
                        audioAcquired = acquired
                    }
                } catch (_: Throwable) {
                    // Fail-safe: never allow an exception in the guard to cause a silent alarm.
                    true
                }
            }

        if (!shouldPlayAudio) return

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
        if (audioAcquired) {
            AlarmAudioSession.release(audioKey)
        } else {
            // Safe even if we never acquired; release() is ownership-checked.
            AlarmAudioSession.release(audioKey)
        }
        audioAcquired = false
    }

    private fun snoozeMinutesSingle(
        minutes: Int,
        requestCode: Int,
        careItemId: Long,
        careName: String,
        instruction: String
    ) {

        val triggerAtMillis =
            System.currentTimeMillis() + minutes * 60_000L

        Log.d(
            "AllIsOK",
            "AlarmActivity.snoozeMinutesSingle rc=$requestCode careItemId=$careItemId " +
                    "minutes=$minutes triggerAt=$triggerAtMillis"
        )

        SnoozeStore(this).saveSingle(
            requestCode = requestCode,
            triggerAtMillis = triggerAtMillis,
            careItemId = careItemId,
            title = careName.ifBlank { "Reminder" },
            text = instruction.ifBlank { "Care reminder" }
        )

        AlarmScheduler(this).scheduleExact(
            triggerAtMillis = triggerAtMillis,
            careItemId = careItemId,
            requestCode = requestCode,
            title = careName.ifBlank { "Reminder" },
            text = instruction.ifBlank { "Care reminder" }
        )

        finish()
    }

    private fun snoozeMinutesGrouped(
        minutes: Int,
        requestCode: Int,
        careItemIds: LongArray,
        careItemNames: Array<String>,
        careItemInstructions: Array<String>
    ) {

        val triggerAtMillis =
            System.currentTimeMillis() + minutes * 60_000L

        Log.d(
            "AllIsOK",
            "AlarmActivity.snoozeMinutesGrouped rc=$requestCode minutes=$minutes " +
                    "triggerAt=$triggerAtMillis ids=${careItemIds.joinToString()}"
        )

        SnoozeStore(this).saveGrouped(
            requestCode = requestCode,
            triggerAtMillis = triggerAtMillis,
            careItemIds = careItemIds,
            careItemNames = careItemNames,
            careItemInstructions = careItemInstructions
        )

        AlarmScheduler(this).scheduleExactGrouped(
            triggerAtMillis = triggerAtMillis,
            requestCode = requestCode,
            careItemIds = careItemIds,
            careItemNames = careItemNames,
            careItemInstructions = careItemInstructions
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