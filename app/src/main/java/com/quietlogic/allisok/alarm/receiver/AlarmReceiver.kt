package com.quietlogic.allisok.alarm.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.AlarmActivity
import com.quietlogic.allisok.alarm.engine.AlarmScheduler
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != AlarmScheduler.ACTION_CARE_ALARM) return

        val timeText = intent.getStringExtra(AlarmScheduler.EXTRA_TIME_TEXT) ?: "--:--"
        val baseCareName = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Reminder"
        val baseInstruction = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT) ?: ""
        val requestCode = intent.getIntExtra(AlarmScheduler.EXTRA_REQUEST_CODE, 0)

        val baseCareItemId = intent.getLongExtra(
            AlarmScheduler.EXTRA_CARE_ITEM_ID,
            -1L
        )
        val payloadCareItemIds =
            intent.getLongArrayExtra(AlarmScheduler.EXTRA_CARE_ITEM_IDS)
        val payloadCareItemNames =
            intent.getStringArrayExtra(AlarmScheduler.EXTRA_CARE_ITEM_NAMES)
        val payloadCareItemInstructions =
            intent.getStringArrayExtra(AlarmScheduler.EXTRA_CARE_ITEM_INSTRUCTIONS)

        val parsedTime = runCatching {
            LocalTime.parse(timeText)
        }.getOrNull()

        val logDate = LocalDate.now().toString()

        val logTime = (parsedTime ?: LocalTime.now()).toString()

        CoroutineScope(Dispatchers.IO).launch {

            val effectiveItems: List<SlotItem> =
                if (payloadCareItemIds != null &&
                    payloadCareItemNames != null &&
                    payloadCareItemInstructions != null &&
                    payloadCareItemIds.isNotEmpty() &&
                    payloadCareItemNames.size == payloadCareItemIds.size &&
                    payloadCareItemInstructions.size == payloadCareItemIds.size
                ) {
                    payloadCareItemIds.indices.map { index ->
                        SlotItem(
                            id = payloadCareItemIds[index],
                            name = payloadCareItemNames[index],
                            instruction = payloadCareItemInstructions[index]
                        )
                    }
                } else {
                    val database = DatabaseProvider.getDatabase(context.applicationContext)

                    val today = LocalDate.now()

                    val slotItems: List<SlotItem> =
                        if (parsedTime != null) {
                            val allItems = database.careItemDao().getAllDirect()
                            val allTimes = database.careTimeDao().getAllDirect()

                            val idsForTime = allTimes
                                .filter { it.time == parsedTime }
                                .map { it.careItemId }
                                .toSet()

                            allItems
                                .filter { item ->
                                    idsForTime.contains(item.id) &&
                                            !item.isArchived &&
                                            !today.isBefore(item.startDate) &&
                                            !today.isAfter(item.endDate) &&
                                            matchesRepeat(today, item.repeatType)
                                }
                                .map { item ->
                                    SlotItem(
                                        id = item.id,
                                        name = item.name,
                                        instruction = item.instruction
                                    )
                                }
                        } else {
                            emptyList()
                        }

                    if (slotItems.isNotEmpty()) {
                        slotItems
                    } else if (baseCareItemId > 0L) {
                        listOf(
                            SlotItem(
                                id = baseCareItemId,
                                name = baseCareName,
                                instruction = baseInstruction
                            )
                        )
                    } else {
                        emptyList()
                    }
                }

            val careItemIdsForKey = effectiveItems.map { it.id }.sorted()
            if (careItemIdsForKey.isNotEmpty()) {
                val shouldProceed = markFirstForCanonicalKey(
                    context = context.applicationContext,
                    logDate = logDate,
                    logTime = logTime,
                    careItemIdsSorted = careItemIdsForKey
                )
                if (!shouldProceed) {
                    return@launch
                }
            }

            val primary = effectiveItems.firstOrNull()

            val careNameForUi = when {
                effectiveItems.isEmpty() -> baseCareName
                effectiveItems.size == 1 -> primary?.name ?: baseCareName
                else -> "${effectiveItems.size} reminders"
            }

            val instructionForUi = when {
                effectiveItems.isEmpty() -> baseInstruction
                effectiveItems.size == 1 -> primary?.instruction ?: baseInstruction
                else -> effectiveItems.joinToString(separator = "\n") { "• ${it.name}" }
            }

            val activityIntent = Intent(context, AlarmActivity::class.java).apply {

                putExtra(AlarmActivity.EXTRA_TIME_TEXT, timeText)
                putExtra(AlarmActivity.EXTRA_CARE_NAME, careNameForUi)
                putExtra(AlarmActivity.EXTRA_INSTRUCTION, instructionForUi)
                putExtra(AlarmActivity.EXTRA_REQUEST_CODE, requestCode)

                putExtra(
                    AlarmActivity.EXTRA_CARE_ITEM_ID,
                    primary?.id ?: baseCareItemId
                )
                putExtra(
                    AlarmActivity.EXTRA_CARE_ITEM_IDS,
                    effectiveItems.map { it.id }.toLongArray()
                )
                putExtra(
                    AlarmActivity.EXTRA_CARE_ITEM_NAMES,
                    effectiveItems.map { it.name }.toTypedArray()
                )
                putExtra(
                    AlarmActivity.EXTRA_CARE_ITEM_INSTRUCTIONS,
                    effectiveItems.map { it.instruction }.toTypedArray()
                )

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
                    ?: return@launch

            ensureChannel(nm)

            val notificationId =
                if (requestCode != 0)
                    requestCode
                else
                    (System.currentTimeMillis() and 0x7fffffff).toInt()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(careNameForUi)
                .setContentText(instructionForUi.ifBlank { "Alarm" })
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
        private const val DEDUPE_PREFS_NAME = "alarm_receiver_dedupe"
        private const val DEDUP_PREFS_NAME = "alarm_dedupe_prefs"
    }

    private data class SlotItem(
        val id: Long,
        val name: String,
        val instruction: String
    )

    private fun matchesRepeat(date: LocalDate, repeatType: String): Boolean {
        if (repeatType == "DAILY") return true

        if (!repeatType.startsWith("DAYS:")) return false

        val allowedDays = repeatType
            .removePrefix("DAYS:")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val currentDay = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "MON"
            DayOfWeek.TUESDAY -> "TUE"
            DayOfWeek.WEDNESDAY -> "WED"
            DayOfWeek.THURSDAY -> "THU"
            DayOfWeek.FRIDAY -> "FRI"
            DayOfWeek.SATURDAY -> "SAT"
            DayOfWeek.SUNDAY -> "SUN"
        }

        return allowedDays.contains(currentDay)
    }

    private fun markFirstForCanonicalKey(
        context: Context,
        logDate: String,
        logTime: String,
        careItemIdsSorted: List<Long>
    ): Boolean {

        if (careItemIdsSorted.isEmpty()) return true

        val key = buildCanonicalKey(logDate, logTime, careItemIdsSorted)

        val prefs: SharedPreferences =
            context.getSharedPreferences(DEDUP_PREFS_NAME, Context.MODE_PRIVATE)

        synchronized(AlarmReceiver::class.java) {
            if (prefs.getBoolean(key, false)) {
                return false
            }

            val editor = prefs.edit()
            editor.putBoolean(key, true)

            return editor.commit()
        }
    }

    private fun buildCanonicalKey(
        logDate: String,
        logTime: String,
        careItemIdsSorted: List<Long>
    ): String {
        val idsPart = careItemIdsSorted.joinToString(separator = ",")
        return "$logDate|$logTime|$idsPart"
    }
}