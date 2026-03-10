package com.quietlogic.allisok.alarm.engine

import android.content.Context
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmRescheduler(private val context: Context) {

    fun rescheduleAll() {
        runBlocking {
            val db = DatabaseProvider.getDatabase(context)
            val planner = AlarmPlanner(context)

            val items = db.careItemDao().getAllDirect()

            items.forEach { item ->

                if (item.isArchived) return@forEach
                if (LocalDate.now().isAfter(item.endDate)) return@forEach

                val times = db.careTimeDao().getTimesForItem(item.id)

                times.forEach { careTime ->
                    val triggerAtMillis = findNextTriggerAtMillis(
                        startDate = item.startDate,
                        endDate = item.endDate,
                        repeatType = item.repeatType,
                        time = careTime.time
                    ) ?: return@forEach

                    planner.scheduleCareAlarm(
                        triggerAtMillis = triggerAtMillis,
                        requestCode = planner.buildRequestCode(item.id, careTime.time),
                        title = item.name,
                        text = item.instruction
                    )
                }
            }
        }
    }

    private fun findNextTriggerAtMillis(
        startDate: LocalDate,
        endDate: LocalDate,
        repeatType: String,
        time: LocalTime
    ): Long? {
        val now = LocalDateTime.now()
        var date = if (LocalDate.now().isBefore(startDate)) startDate else LocalDate.now()

        while (!date.isAfter(endDate)) {

            val dateTime = LocalDateTime.of(date, time)

            if (dateTime.isAfter(now) && matchesRepeat(date, repeatType)) {
                return dateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }

            date = date.plusDays(1)
        }

        return null
    }

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
}