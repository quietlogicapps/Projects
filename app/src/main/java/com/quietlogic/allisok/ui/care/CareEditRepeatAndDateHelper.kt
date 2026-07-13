package com.quietlogic.allisok.ui.care

import android.app.DatePickerDialog
import android.view.View
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private val btnPickStart: View,
    private val btnPickEnd: View,
    private val textStart: TextView,
    private val textEnd: TextView
) {

    private var suppressDaysDialogOpen = false

    fun shouldSuppressDaysDialogOpen(): Boolean = suppressDaysDialogOpen

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
        if (suppressDaysDialogOpen) return

        val checkedItems = checkedDays.copyOf()

        lateinit var daysDialog: AlertDialog

        daysDialog = MaterialAlertDialogBuilder(activity, R.style.AllIsOK_MaterialAlertDialog_CareDays)
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
            .setNegativeButton(activity.getString(R.string.dialog_cancel), null)
            .setBackground(
                ContextCompat.getDrawable(activity, R.drawable.bg_care_select_days_dialog)
            )
            .create()

        daysDialog.setOnShowListener {
            daysDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
                suppressDaysDialogOpen = true
                daysDialog.dismiss()
                if (groupRepeat.checkedRadioButtonId == R.id.radioSpecific) {
                    groupRepeat.check(R.id.radioDaily)
                }
                groupRepeat.post {
                    suppressDaysDialogOpen = false
                }
            }
        }

        daysDialog.show()

        applyCareDaysPopupGradient(daysDialog)

        val listView = daysDialog.listView
        listView?.post {
            applyCareDaysListItemGradient(listView)
        }
    }

    private fun applyCareDaysPopupGradient(dialog: AlertDialog) {
        val gradientId = R.drawable.bg_care_select_days_gradient
        dialog.findViewById<View>(androidx.appcompat.R.id.topPanel)?.setBackgroundResource(gradientId)
        dialog.findViewById<View>(androidx.appcompat.R.id.contentPanel)?.setBackgroundResource(gradientId)
        dialog.findViewById<View>(androidx.appcompat.R.id.buttonPanel)?.setBackgroundResource(gradientId)
        dialog.listView?.setBackgroundResource(gradientId)
    }

    private fun applyCareDaysListItemGradient(listView: ListView) {
        val gradientId = R.drawable.bg_care_select_days_gradient
        for (i in 0 until listView.childCount) {
            val child = listView.getChildAt(i)
            child.setBackgroundResource(gradientId)
            if (child is TextView) {
                child.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
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
        dialog.datePicker?.post {
            applyDatePickerCareDaysBackground(dialog)
        }
    }

    private fun applyDatePickerCareDaysBackground(dialog: DatePickerDialog) {
        val panelBackground = R.drawable.bg_care_select_days_gradient
        val datePicker = dialog.datePicker

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(activity, R.drawable.bg_care_select_days_dialog)
        )

        val headerId = datePicker.resources.getIdentifier("date_picker_header", "id", "android")
        if (headerId != 0) {
            datePicker.findViewById<View>(headerId)?.setBackgroundResource(panelBackground)
        }

        val calendarId = datePicker.resources.getIdentifier("date_picker_view_animator", "id", "android")
        if (calendarId != 0) {
            datePicker.findViewById<View>(calendarId)?.setBackgroundResource(panelBackground)
        } else if (datePicker.childCount > 1) {
            datePicker.getChildAt(0)?.setBackgroundResource(panelBackground)
            datePicker.getChildAt(1)?.setBackgroundResource(panelBackground)
        }

        dialog.findViewById<View>(androidx.appcompat.R.id.buttonPanel)
            ?.setBackgroundResource(panelBackground)
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
