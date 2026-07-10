package com.quietlogic.allisok.ui.care

import android.content.Context
import androidx.annotation.StringRes
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmPlanner
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

data class CareEditSaveInput(
    val name: String,
    val instruction: String,
    val isDaily: Boolean,
    val selectedDays: List<String>,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val times: List<LocalTime>
)

sealed class CareEditSaveResult {
    data object Success : CareEditSaveResult()
    data class ValidationError(@StringRes val messageResId: Int) : CareEditSaveResult()
}

class CareEditSaveExecutor(
    private val context: Context,
    private val db: AppDatabase
) {

    suspend fun validateAndSave(input: CareEditSaveInput): CareEditSaveResult {
        if (input.name.isEmpty()) {
            return CareEditSaveResult.ValidationError(R.string.care_name_required)
        }

        if (input.times.isEmpty()) {
            return CareEditSaveResult.ValidationError(R.string.care_add_at_least_one_time)
        }

        if (input.isDaily && input.startDate == null) {
            return CareEditSaveResult.ValidationError(R.string.care_start_required)
        }

        if (input.isDaily && input.endDate == null) {
            return CareEditSaveResult.ValidationError(R.string.care_end_required)
        }

        val start = if (input.isDaily) {
            input.startDate ?: LocalDate.now()
        } else {
            LocalDate.now()
        }

        val end = if (input.isDaily) {
            input.endDate ?: start.plusDays(30)
        } else {
            start.plusDays(30)
        }

        val repeatType = if (input.isDaily) {
            "DAILY"
        } else {
            if (input.selectedDays.isEmpty()) {
                return CareEditSaveResult.ValidationError(R.string.care_select_days)
            }
            "DAYS:" + input.selectedDays.joinToString(",")
        }

        val item = CareItemEntity(
            name = input.name,
            instruction = input.instruction,
            startDate = start,
            endDate = end,
            repeatType = repeatType
        )

        withContext(Dispatchers.IO) {
            val itemId = db.careItemDao().insert(item)

            val planner = AlarmPlanner(context)

            input.times.forEach { t ->
                db.careTimeDao().insert(
                    CareTimeEntity(
                        careItemId = itemId,
                        time = t
                    )
                )

                val triggerAtMillis = CareAlarmTriggerCalculator.buildFirstTriggerAtMillis(
                    startDate = start,
                    time = t
                )

                val requestCode = planner.buildRequestCode(itemId, t)

                planner.scheduleCareAlarm(
                    triggerAtMillis = triggerAtMillis,
                    careItemId = itemId,
                    requestCode = requestCode,
                    title = input.name,
                    text = input.instruction
                )
            }
        }

        return CareEditSaveResult.Success
    }
}
