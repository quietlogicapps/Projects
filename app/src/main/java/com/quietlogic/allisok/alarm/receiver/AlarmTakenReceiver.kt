package com.quietlogic.allisok.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class AlarmTakenReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_CARE_ITEM_ID = "extra_care_item_id"
        const val EXTRA_LOG_DATE = "extra_log_date"
        const val EXTRA_LOG_TIME = "extra_log_time"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val careItemId = intent.getLongExtra(EXTRA_CARE_ITEM_ID, -1L)
        val dateText = intent.getStringExtra(EXTRA_LOG_DATE)
        val timeText = intent.getStringExtra(EXTRA_LOG_TIME)

        if (careItemId <= 0L) return
        if (dateText.isNullOrBlank()) return
        if (timeText.isNullOrBlank()) return

        val logDate = runCatching {
            LocalDate.parse(dateText)
        }.getOrNull() ?: return

        val logTime = runCatching {
            LocalTime.parse(timeText)
        }.getOrNull() ?: return

        CoroutineScope(Dispatchers.IO).launch {

            val database = DatabaseProvider.getDatabase(
                context.applicationContext
            )

            database.careLogDao().insert(
                CareLogEntity(
                    careItemId = careItemId,
                    date = logDate,
                    scheduledTime = logTime
                )
            )
        }
    }
}