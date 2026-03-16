package com.quietlogic.allisok.alarm.engine

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Persists active snoozes so timezone-change rescheduling can restore them.
 *
 * We intentionally keep this scoped to timezone-change flow (not reboot)
 * to avoid changing existing reboot behavior.
 */
class SnoozeStore(private val context: Context) {

    fun saveSingle(
        requestCode: Int,
        triggerAtMillis: Long,
        careItemId: Long,
        title: String,
        text: String
    ) {
        val localDateTime = triggerAtMillis.toLocalDateTime()

        Log.d(
            "AllIsOK",
            "SnoozeStore.saveSingle rc=$requestCode triggerAt=$triggerAtMillis " +
                    "ldt=$localDateTime careItemId=$careItemId title='$title'"
        )
        val obj = JSONObject()
            .put("v", 2)
            .put("type", "single")
            .put("triggerAtMillis", triggerAtMillis)
            .put("localDate", localDateTime.toLocalDate().toString())
            .put("localTime", localDateTime.toLocalTime().toString())
            .put("careItemId", careItemId)
            .put("title", title)
            .put("text", text)

        prefs().edit()
            .putString(key(requestCode), obj.toString())
            .apply()
    }

    fun saveGrouped(
        requestCode: Int,
        triggerAtMillis: Long,
        careItemIds: LongArray,
        careItemNames: Array<String>,
        careItemInstructions: Array<String>
    ) {
        val localDateTime = triggerAtMillis.toLocalDateTime()

        Log.d(
            "AllIsOK",
            "SnoozeStore.saveGrouped rc=$requestCode triggerAt=$triggerAtMillis " +
                    "ldt=$localDateTime ids=${careItemIds.toList()}"
        )
        val obj = JSONObject()
            .put("v", 2)
            .put("type", "grouped")
            .put("triggerAtMillis", triggerAtMillis)
            .put("localDate", localDateTime.toLocalDate().toString())
            .put("localTime", localDateTime.toLocalTime().toString())
            .put("careItemIds", JSONArray(careItemIds.toList()))
            .put("careItemNames", JSONArray(careItemNames.toList()))
            .put("careItemInstructions", JSONArray(careItemInstructions.toList()))

        prefs().edit()
            .putString(key(requestCode), obj.toString())
            .apply()
    }

    fun clear(requestCode: Int) {
        Log.d("AllIsOK", "SnoozeStore.clear rc=$requestCode")
        prefs().edit().remove(key(requestCode)).apply()
    }

    /**
     * Re-schedules all active snoozes that are still in the future.
     * Intended to run after the normal reschedule on timezone change.
     */
    fun rescheduleAllActive(): Int {
        val now = System.currentTimeMillis()
        Log.d("AllIsOK", "SnoozeStore.rescheduleAllActive start now=$now")
        val p = prefs()
        val scheduler = AlarmScheduler(context)

        var restored = 0
        val editor = p.edit()

        p.all.forEach { (prefKey, value) ->
            if (!prefKey.startsWith(KEY_PREFIX)) return@forEach
            val payload = value as? String ?: return@forEach

            val obj = runCatching { JSONObject(payload) }.getOrNull()
                ?: run { editor.remove(prefKey); return@forEach }

            val triggerAtMillis = obj.recomputeTriggerAtMillisForCurrentTimezone()

            Log.d(
                "AllIsOK",
                "SnoozeStore.restore prefKey=$prefKey " +
                        "storedMillis=${obj.optLong("triggerAtMillis", -1L)} " +
                        "recomputedMillis=$triggerAtMillis now=$now"
            )
            if (triggerAtMillis <= now + 500L) {
                Log.d(
                    "AllIsOK",
                    "SnoozeStore.restore dropExpired prefKey=$prefKey recomputedMillis=$triggerAtMillis"
                )
                editor.remove(prefKey)
                return@forEach
            }

            val requestCode = prefKey.removePrefix(KEY_PREFIX).toIntOrNull()
            if (requestCode == null) {
                Log.d(
                    "AllIsOK",
                    "SnoozeStore.restore invalidRequestCode prefKey=$prefKey"
                )
                editor.remove(prefKey)
                return@forEach
            }

            when (obj.optString("type", "")) {
                "single" -> {
                    val careItemId = obj.optLong("careItemId", -1L)
                    val title = obj.optString("title", "Reminder")
                    val text = obj.optString("text", "Care reminder")
                    if (careItemId > 0L) {
                        Log.d(
                            "AllIsOK",
                            "SnoozeStore.restore single rc=$requestCode careItemId=$careItemId " +
                                    "triggerAt=$triggerAtMillis"
                        )
                        scheduler.scheduleExact(
                            triggerAtMillis = triggerAtMillis,
                            careItemId = careItemId,
                            requestCode = requestCode,
                            title = title,
                            text = text
                        )
                        restored += 1
                    } else {
                        Log.d(
                            "AllIsOK",
                            "SnoozeStore.restore single invalidCareItemId rc=$requestCode careItemId=$careItemId"
                        )
                        editor.remove(prefKey)
                    }
                }

                "grouped" -> {
                    val idsJson = obj.optJSONArray("careItemIds")
                    val namesJson = obj.optJSONArray("careItemNames")
                    val instructionsJson = obj.optJSONArray("careItemInstructions")

                    val careItemIds = idsJson?.toLongArray()
                    val careItemNames = namesJson?.toStringArray()
                    val careItemInstructions = instructionsJson?.toStringArray()

                    if (careItemIds != null &&
                        careItemNames != null &&
                        careItemInstructions != null &&
                        careItemIds.isNotEmpty() &&
                        careItemNames.size == careItemIds.size &&
                        careItemInstructions.size == careItemIds.size
                    ) {
                        Log.d(
                            "AllIsOK",
                            "SnoozeStore.restore grouped rc=$requestCode " +
                                    "triggerAt=$triggerAtMillis ids=${careItemIds.toList()}"
                        )
                        scheduler.scheduleExactGrouped(
                            triggerAtMillis = triggerAtMillis,
                            requestCode = requestCode,
                            careItemIds = careItemIds,
                            careItemNames = careItemNames,
                            careItemInstructions = careItemInstructions
                        )
                        restored += 1
                    } else {
                        Log.d(
                            "AllIsOK",
                            "SnoozeStore.restore grouped invalidPayload rc=$requestCode"
                        )
                        editor.remove(prefKey)
                    }
                }

                else -> {
                    Log.d(
                        "AllIsOK",
                        "SnoozeStore.restore unknownType prefKey=$prefKey type=${obj.optString("type", "")}"
                    )
                    editor.remove(prefKey)
                }
            }

            // Keep stored millis in sync with the current timezone so future logic
            // (and any UI that reads triggerAtMillis) remains consistent.
            obj.put("triggerAtMillis", triggerAtMillis)
            editor.putString(prefKey, obj.toString())
        }

        editor.apply()
        Log.d("AllIsOK", "SnoozeStore.rescheduleAllActive done restored=$restored")
        return restored
    }

    private fun prefs(): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(requestCode: Int): String = "$KEY_PREFIX$requestCode"

    private fun JSONArray.toLongArray(): LongArray {
        val out = LongArray(length())
        for (i in 0 until length()) out[i] = optLong(i)
        return out
    }

    private fun JSONArray.toStringArray(): Array<String> {
        val out = Array(length()) { "" }
        for (i in 0 until length()) out[i] = optString(i, "")
        return out
    }

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

    private fun JSONObject.recomputeTriggerAtMillisForCurrentTimezone(): Long {
        val localDateStr = optString("localDate", "")
        val localTimeStr = optString("localTime", "")

        if (localDateStr.isNotBlank() && localTimeStr.isNotBlank()) {
            val localDate = runCatching { LocalDate.parse(localDateStr) }.getOrNull()
            val localTime = runCatching { LocalTime.parse(localTimeStr) }.getOrNull()
            if (localDate != null && localTime != null) {
                return LocalDateTime.of(localDate, localTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        }

        return optLong("triggerAtMillis", -1L)
    }

    companion object {
        private const val PREFS_NAME = "alarm_snooze_store"
        private const val KEY_PREFIX = "snooze_"
    }
}

