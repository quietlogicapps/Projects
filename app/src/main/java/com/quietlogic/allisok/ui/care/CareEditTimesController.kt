package com.quietlogic.allisok.ui.care

import android.app.TimePickerDialog
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.quietlogic.allisok.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CareEditTimesController(
    private val activity: AppCompatActivity,
    private val times: MutableList<LocalTime>,
    private val layoutTimes: LinearLayout,
    private val textAddTime: TextView,
    private val textNoTimes: TextView,
    private val timeFormatter: DateTimeFormatter
) {

    fun renderTimes() {
        layoutTimes.removeAllViews()
        layoutTimes.orientation = LinearLayout.VERTICAL

        if (times.isEmpty()) {
            updateAddTimeUi()
            return
        }

        updateAddTimeUi()

        var index = 0

        while (index < times.size) {
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(8)
                }
            }

            val leftCell = createTimeCell(
                time = times[index],
                addInnerStartPadding = false
            )

            row.addView(leftCell)

            if (index + 1 < times.size) {
                val rightCell = createTimeCell(
                    time = times[index + 1],
                    addInnerStartPadding = true
                )
                row.addView(rightCell)
            } else {
                val emptyCell = LinearLayout(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                row.addView(emptyCell)
            }

            layoutTimes.addView(row)
            index += 2
        }
    }

    fun updateAddTimeUi() {
        if (times.isEmpty()) {
            textAddTime.text = activity.getString(R.string.care_add_time)
            textNoTimes.text = activity.getString(R.string.care_no_times_added)
            textNoTimes.setTextColor(ContextCompat.getColor(activity, android.R.color.white))
            textNoTimes.visibility = android.view.View.VISIBLE
        } else {
            textAddTime.text = activity.getString(R.string.care_add_another_time)
            textNoTimes.text = activity.getString(R.string.care_add_another_time_hint)
            textNoTimes.setTextColor(ContextCompat.getColor(activity, android.R.color.white))
            textNoTimes.visibility = android.view.View.VISIBLE
        }
    }

    fun openTimePicker() {
        val now = LocalTime.now()

        val dialog = TimePickerDialog(
            activity,
            R.style.AllIsOK_TimePickerDialog,
            { _, hourOfDay, minute ->
                val picked = LocalTime.of(hourOfDay, minute)

                if (times.contains(picked)) {
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.care_time_already_added),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    times.add(picked)
                    times.sort()
                    renderTimes()
                }
            },
            now.hour,
            now.minute,
            true
        )

        dialog.setButton(
            TimePickerDialog.BUTTON_NEGATIVE,
            activity.getString(R.string.dialog_cancel)
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialog.show()
        dialog.findViewById<TimePicker>(
            activity.resources.getIdentifier("timePicker", "id", "android")
        )?.post {
            applyTimePickerCareBackground(dialog)
        }
    }

    private fun applyTimePickerCareBackground(dialog: TimePickerDialog) {
        val panelBackground = R.drawable.bg_care_select_days_gradient
        val timePickerId = activity.resources.getIdentifier("timePicker", "id", "android")
        val timePicker = if (timePickerId != 0) {
            dialog.findViewById<TimePicker>(timePickerId)
        } else {
            null
        } ?: return

        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(activity, R.drawable.bg_care_select_days_dialog)
        )

        val headerId = timePicker.resources.getIdentifier("time_header", "id", "android")
        if (headerId != 0) {
            timePicker.findViewById<View>(headerId)?.setBackgroundResource(panelBackground)
        }

        val radialId = timePicker.resources.getIdentifier("radial_picker", "id", "android")
        if (radialId != 0) {
            timePicker.findViewById<View>(radialId)?.setBackgroundResource(panelBackground)
        }

        val toggleId = timePicker.resources.getIdentifier("toggle_mode", "id", "android")
        if (toggleId != 0) {
            (timePicker.findViewById<View>(toggleId)?.parent as? View)
                ?.setBackgroundResource(panelBackground)
        }

        dialog.findViewById<View>(androidx.appcompat.R.id.buttonPanel)
            ?.setBackgroundResource(panelBackground)
    }

    private fun createTimeCell(
        time: LocalTime,
        addInnerStartPadding: Boolean
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            if (addInnerStartPadding) {
                setPadding(dpToPx(28), 0, 0, 0)
            }

            val timeText = TextView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                text = time.format(timeFormatter)
                textSize = 18f
                setTextColor(ContextCompat.getColor(activity, android.R.color.white))
            }

            val deleteBtn = TextView(activity).apply {
                text = activity.getString(R.string.care_delete_time)
                textSize = 18f
                setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark))
                setPadding(dpToPx(12), dpToPx(4), dpToPx(4), dpToPx(4))

                setOnClickListener {
                    times.remove(time)
                    renderTimes()
                }
            }

            addView(timeText)
            addView(deleteBtn)
        }
    }

    private fun dpToPx(value: Int): Int {
        return (value * activity.resources.displayMetrics.density).toInt()
    }
}
