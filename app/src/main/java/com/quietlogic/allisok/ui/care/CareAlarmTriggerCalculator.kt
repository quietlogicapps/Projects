package com.quietlogic.allisok.ui.care

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object CareAlarmTriggerCalculator {

    fun buildFirstTriggerAtMillis(startDate: LocalDate, time: LocalTime): Long {
        val now = LocalDateTime.now()
        var triggerDate = if (LocalDate.now().isBefore(startDate)) startDate else LocalDate.now()
        var triggerDateTime = LocalDateTime.of(triggerDate, time)

        while (!triggerDateTime.isAfter(now)) {
            triggerDate = triggerDate.plusDays(1)
            triggerDateTime = LocalDateTime.of(triggerDate, time)
        }

        return triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
