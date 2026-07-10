package com.quietlogic.allisok.ui.care

import android.app.DatePickerDialog
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quietlogic.allisok.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CareEditRepeatAndDateHelper(
    private val activity: AppCompatActivity,
    private val selectedDays: MutableList<String>,
    private val checkedDays: BooleanArray,
    private val dayCodes: Array<String>,
    private val dayLabels: Array<String>,
    private val dateFormatterProvider: () -> DateTimeFormatter,
    private val startDateProvider: () -> LocalDate?,
    private val endDateProvider: () -> LocalDate?,
    private val groupRepeat: RadioGroup,
    private val btnPickStart: MaterialButton,
    private val btnPickEnd: MaterialButton,
    private val textStart: TextView,
    private val textEnd: TextView
) {

    fun updateDateButtonsState() {
        val isDaily = groupRepeat.checkedRadioButtonId == R.id.radioDaily

        btnPickStart.isEnabled = isDaily
        btnPickEnd.isEnabled = isDaily

        textStart.isEnabled = isDaily
        textEnd.isEnabled = isDaily

        if (isDaily) {
            val startDate = startDateProvider()
            if (startDate == null) {
                textStart.text = activity.getString(R.string.care_start_not_set)
            } else {
                textStart.text = activity.getString(
                    R.string.care_start_value,
                    startDate.format(dateFormatterProvider())
                )
            }

            val endDate = endDateProvider()
            if (endDate == null) {
                textEnd.text = activity.getString(R.string.care_end_not_set)
            } else {
                textEnd.text = activity.getString(
                    R.string.care_end_value,
                    endDate.format(dateFormatterProvider())
                )
            }
        }
    }

    fun openDaysDialog(textRepeatDays: TextView) {
        val checkedItems = checkedDays.copyOf()

        val dialog = MaterialAlertDialogBuilder(activity, R.style.AllIsOK_MaterialAlertDialog)
            .setTitle(activity.getString(R.string.care_select_days))
            .setMultiChoiceItems(dayLabels, checkedItems) { _, which, isChecked ->
                checkedDays[which] = isChecked
            }
            .setPositiveButton(activity.getString(R.string.dialog_ok)) { _, _ ->
                selectedDays.clear()
                for (i in dayCodes.indices) {
                    if (checkedDays[i]) {
                        selectedDays.add(dayCodes[i])
                    }
                }

                if (selectedDays.isEmpty()) {
                    textRepeatDays.text = activity.getString(R.string.care_days_not_selected)
                } else {
                    val selectedDayLabels = selectedDays.map { code -> mapDayCodeToLabel(code) }
                    textRepeatDays.text = activity.getString(
                        R.string.care_days_selected,
                        selectedDayLabels.joinToString(", ")
                    )
                }
            }
            .setNegativeButton(activity.getString(R.string.dialog_cancel)) { _, _ ->
                if (
                    selectedDays.isEmpty() &&
                    groupRepeat.checkedRadioButtonId == R.id.radioSpecific
                ) {
                    textRepeatDays.text = activity.getString(R.string.care_days_not_selected)
                }
            }
            .show()

        val listView = dialog.listView
        listView?.post {
            for (i in 0 until listView.count) {
                val child = listView.getChildAt(i)
                if (child is TextView) {
                    child.setTextColor(android.graphics.Color.parseColor("#111111"))
                }
            }
        }
    }

    fun openDatePicker(onPicked: (LocalDate) -> Unit) {
        val now = LocalDate.now()
        val dialog = DatePickerDialog(
            activity,
            R.style.AllIsOK_DatePickerDialog,
            { _, year, month, day ->
                onPicked(LocalDate.of(year, month + 1, day))
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        dialog.datePicker.minDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun mapDayCodeToLabel(code: String): String {
        return when (code.trim()) {
            "MON" -> activity.getString(R.string.care_day_mon)
            "TUE" -> activity.getString(R.string.care_day_tue)
            "WED" -> activity.getString(R.string.care_day_wed)
            "THU" -> activity.getString(R.string.care_day_thu)
            "FRI" -> activity.getString(R.string.care_day_fri)
            "SAT" -> activity.getString(R.string.care_day_sat)
            "SUN" -> activity.getString(R.string.care_day_sun)
            else -> code
        }
    }
}
