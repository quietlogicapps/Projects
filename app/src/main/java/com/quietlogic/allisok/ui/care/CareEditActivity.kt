package com.quietlogic.allisok.ui.care

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmPlanner
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.ui.home.Button3D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CareEditActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private val times: MutableList<LocalTime> = mutableListOf()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private val selectedDays: MutableList<String> = mutableListOf()

    private lateinit var dayCodes: Array<String>
    private lateinit var dayLabels: Array<String>

    private lateinit var checkedDays: BooleanArray

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            java.util.Locale(parts[0], parts[1])
        } else {
            java.util.Locale(languageCode)
        }
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AdminSession.isActive()) {
            LockGate.requireAdminUnlock(this)
        }

        setContentView(R.layout.activity_care_edit)

        title = getString(R.string.care_edit_title)

        db = DatabaseProvider.getDatabase(applicationContext)

        dayCodes = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        dayLabels = arrayOf(
            getString(R.string.care_day_mon),
            getString(R.string.care_day_tue),
            getString(R.string.care_day_wed),
            getString(R.string.care_day_thu),
            getString(R.string.care_day_fri),
            getString(R.string.care_day_sat),
            getString(R.string.care_day_sun)
        )
        checkedDays = BooleanArray(dayCodes.size)

        val nameInput = findViewById<EditText>(R.id.inputName)

        val groupInstruction = findViewById<RadioGroup>(R.id.groupInstruction)
        val groupRepeat = findViewById<RadioGroup>(R.id.groupRepeat)

        val btnPickDays = findViewById<MaterialButton>(R.id.btnPickDays)
        val textRepeatDays = findViewById<TextView>(R.id.textRepeatDays)

        val btnPickStart = findViewById<MaterialButton>(R.id.btnPickStart)
        val btnPickEnd = findViewById<MaterialButton>(R.id.btnPickEnd)

        val textStart = findViewById<TextView>(R.id.textStart)
        val textEnd = findViewById<TextView>(R.id.textEnd)

        val btnAddTime = findViewById<MaterialButton>(R.id.btnAddTime)
        val layoutTimes = findViewById<LinearLayout>(R.id.layoutTimes)
        val textNoTimes = findViewById<TextView>(R.id.textNoTimes)

        val btnSave = findViewById<MaterialButton>(R.id.btnSaveCare)

        Button3D.apply(btnPickDays, 12f)
        Button3D.apply(btnPickStart, 12f)
        Button3D.apply(btnPickEnd, 12f)
        Button3D.apply(btnAddTime, 12f)
        Button3D.apply(btnSave, 12f)

        btnPickDays.visibility = View.GONE
        updateDateButtonsState(
            groupRepeat = groupRepeat,
            btnPickStart = btnPickStart,
            btnPickEnd = btnPickEnd,
            textStart = textStart,
            textEnd = textEnd
        )
        updateAddTimeUi(btnAddTime, textNoTimes)

        groupInstruction.setOnCheckedChangeListener { _, _ ->
            hideKeyboardAndClearFocus(nameInput)
        }

        groupRepeat.setOnCheckedChangeListener { _, checkedId ->

            hideKeyboardAndClearFocus(nameInput)

            if (checkedId == R.id.radioDaily) {
                btnPickDays.visibility = View.GONE
                textRepeatDays.text = getString(R.string.care_days_daily)
                selectedDays.clear()

                for (i in checkedDays.indices) {
                    checkedDays[i] = false
                }

                updateDateButtonsState(
                    groupRepeat = groupRepeat,
                    btnPickStart = btnPickStart,
                    btnPickEnd = btnPickEnd,
                    textStart = textStart,
                    textEnd = textEnd
                )
            }

            if (checkedId == R.id.radioSpecific) {
                btnPickDays.visibility = View.GONE

                startDate = null
                endDate = null
                textStart.text = getString(R.string.care_start_not_set)
                textEnd.text = getString(R.string.care_end_not_set)

                updateDateButtonsState(
                    groupRepeat = groupRepeat,
                    btnPickStart = btnPickStart,
                    btnPickEnd = btnPickEnd,
                    textStart = textStart,
                    textEnd = textEnd
                )

                openDaysDialog(textRepeatDays)
            }
        }

        renderTimes(layoutTimes, textNoTimes, btnAddTime)

        btnPickStart.setOnClickListener {
            hideKeyboardAndClearFocus(nameInput)

            if (groupRepeat.checkedRadioButtonId != R.id.radioDaily) return@setOnClickListener

            openDatePicker { date ->
                startDate = date
                textStart.text = getString(R.string.care_start_value, date.format(dateFormatter))
            }
        }

        btnPickEnd.setOnClickListener {
            hideKeyboardAndClearFocus(nameInput)

            if (groupRepeat.checkedRadioButtonId != R.id.radioDaily) return@setOnClickListener

            openDatePicker { date ->
                endDate = date
                textEnd.text = getString(R.string.care_end_value, date.format(dateFormatter))
            }
        }

        btnAddTime.setOnClickListener {
            hideKeyboardAndClearFocus(nameInput)
            openTimePicker(layoutTimes, textNoTimes, btnAddTime)
        }

        btnSave.setOnClickListener {

            hideKeyboardAndClearFocus(nameInput)

            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.care_name_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (times.isEmpty()) {
                Toast.makeText(this, getString(R.string.care_add_at_least_one_time), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isDaily = groupRepeat.checkedRadioButtonId == R.id.radioDaily

            if (isDaily && startDate == null) {
                Toast.makeText(this, getString(R.string.care_start_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isDaily && endDate == null) {
                Toast.makeText(this, getString(R.string.care_end_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val start = if (isDaily) {
                startDate ?: LocalDate.now()
            } else {
                LocalDate.now()
            }

            val end = if (isDaily) {
                endDate ?: start.plusDays(30)
            } else {
                start.plusDays(30)
            }

            val instruction = when (groupInstruction.checkedRadioButtonId) {
                R.id.radioBefore -> getString(R.string.care_instruction_before_food)
                R.id.radioAfter -> getString(R.string.care_instruction_after_food)
                else -> getString(R.string.care_instruction_none)
            }

            val repeatType = if (isDaily) {
                "DAILY"
            } else {
                if (selectedDays.isEmpty()) {
                    Toast.makeText(this, getString(R.string.care_select_days), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                "DAYS:" + selectedDays.joinToString(",")
            }

            val item = CareItemEntity(
                name = name,
                instruction = instruction,
                startDate = start,
                endDate = end,
                repeatType = repeatType
            )

            lifecycleScope.launch {

                withContext(Dispatchers.IO) {

                    val itemId = db.careItemDao().insert(item)

                    val planner = AlarmPlanner(this@CareEditActivity)

                    times.forEach { t ->

                        db.careTimeDao().insert(
                            CareTimeEntity(
                                careItemId = itemId,
                                time = t
                            )
                        )

                        val triggerAtMillis = buildFirstTriggerAtMillis(
                            startDate = start,
                            time = t
                        )

                        val requestCode = planner.buildRequestCode(itemId, t)

                        planner.scheduleCareAlarm(
                            triggerAtMillis = triggerAtMillis,
                            careItemId = itemId,
                            requestCode = requestCode,
                            title = name,
                            text = instruction
                        )
                    }
                }

                Toast.makeText(this@CareEditActivity, getString(R.string.care_saved), Toast.LENGTH_SHORT).show()

                finish()
            }
        }
    }

    private fun updateDateButtonsState(
        groupRepeat: RadioGroup,
        btnPickStart: MaterialButton,
        btnPickEnd: MaterialButton,
        textStart: TextView,
        textEnd: TextView
    ) {
        val isDaily = groupRepeat.checkedRadioButtonId == R.id.radioDaily

        btnPickStart.isEnabled = isDaily
        btnPickEnd.isEnabled = isDaily

        textStart.isEnabled = isDaily
        textEnd.isEnabled = isDaily

        if (isDaily) {
            if (startDate == null) {
                textStart.text = getString(R.string.care_start_not_set)
            } else {
                textStart.text = getString(R.string.care_start_value, startDate!!.format(dateFormatter))
            }

            if (endDate == null) {
                textEnd.text = getString(R.string.care_end_not_set)
            } else {
                textEnd.text = getString(R.string.care_end_value, endDate!!.format(dateFormatter))
            }
        }
    }

    private fun openDaysDialog(textRepeatDays: TextView) {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.care_select_days))
            .setMultiChoiceItems(dayLabels, checkedDays) { _, which, isChecked ->
                checkedDays[which] = isChecked
            }
            .setPositiveButton(getString(R.string.dialog_ok)) { _, _ ->

                selectedDays.clear()

                for (i in dayCodes.indices) {
                    if (checkedDays[i]) {
                        selectedDays.add(dayCodes[i])
                    }
                }

                if (selectedDays.isEmpty()) {
                    textRepeatDays.text = getString(R.string.care_days_not_selected)
                } else {
                    val selectedDayLabels = selectedDays.map { code -> mapDayCodeToLabel(code) }

                    textRepeatDays.text = getString(
                        R.string.care_days_selected,
                        selectedDayLabels.joinToString(", ")
                    )
                }
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ ->
                if (
                    selectedDays.isEmpty() &&
                    findViewById<RadioGroup>(R.id.groupRepeat).checkedRadioButtonId == R.id.radioSpecific
                ) {
                    textRepeatDays.text = getString(R.string.care_days_not_selected)
                }
            }
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_ADMIN_UNLOCK) {
            if (resultCode != RESULT_OK) {
                finish()
            }
        }
    }

    private fun renderTimes(
        layoutTimes: LinearLayout,
        textNoTimes: TextView,
        btnAddTime: MaterialButton
    ) {

        layoutTimes.removeAllViews()
        layoutTimes.orientation = LinearLayout.VERTICAL

        if (times.isEmpty()) {
            updateAddTimeUi(btnAddTime, textNoTimes)
            return
        }

        updateAddTimeUi(btnAddTime, textNoTimes)

        var index = 0

        while (index < times.size) {

            val row = LinearLayout(this).apply {
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
                layoutTimes = layoutTimes,
                textNoTimes = textNoTimes,
                btnAddTime = btnAddTime
            )

            row.addView(leftCell)

            if (index + 1 < times.size) {
                val rightCell = createTimeCell(
                    time = times[index + 1],
                    layoutTimes = layoutTimes,
                    textNoTimes = textNoTimes,
                    btnAddTime = btnAddTime,
                    addInnerStartPadding = true
                )
                row.addView(rightCell)
            } else {
                val emptyCell = LinearLayout(this).apply {
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

    private fun createTimeCell(
        time: LocalTime,
        layoutTimes: LinearLayout,
        textNoTimes: TextView,
        btnAddTime: MaterialButton,
        addInnerStartPadding: Boolean = false
    ): LinearLayout {
        return LinearLayout(this).apply {
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

            val timeText = TextView(this@CareEditActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                text = time.format(timeFormatter)
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@CareEditActivity, android.R.color.black))
            }

            val deleteBtn = TextView(this@CareEditActivity).apply {
                text = getString(R.string.care_delete_time)
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@CareEditActivity, android.R.color.holo_red_dark))
                setPadding(dpToPx(12), dpToPx(4), dpToPx(4), dpToPx(4))

                setOnClickListener {
                    times.remove(time)
                    renderTimes(layoutTimes, textNoTimes, btnAddTime)
                }
            }

            addView(timeText)
            addView(deleteBtn)
        }
    }

    private fun updateAddTimeUi(btnAddTime: MaterialButton, textNoTimes: TextView) {
        if (times.isEmpty()) {
            btnAddTime.text = getString(R.string.care_add_time)
            textNoTimes.text = getString(R.string.care_no_times_added)
            textNoTimes.visibility = View.VISIBLE
        } else {
            btnAddTime.text = getString(R.string.care_add_another_time)
            textNoTimes.text = getString(R.string.care_add_another_time_hint)
            textNoTimes.visibility = View.VISIBLE
        }
    }

    private fun openTimePicker(
        layoutTimes: LinearLayout,
        textNoTimes: TextView,
        btnAddTime: MaterialButton
    ) {

        val now = LocalTime.now()

        val dialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->

                val picked = LocalTime.of(hourOfDay, minute)

                if (times.contains(picked)) {
                    Toast.makeText(this, getString(R.string.care_time_already_added), Toast.LENGTH_SHORT).show()
                } else {
                    times.add(picked)
                    times.sort()
                    renderTimes(layoutTimes, textNoTimes, btnAddTime)
                }
            },
            now.hour,
            now.minute,
            true
        )

        dialog.setButton(
            TimePickerDialog.BUTTON_NEGATIVE,
            getString(R.string.dialog_cancel)
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialog.show()
    }

    private fun openDatePicker(onPicked: (LocalDate) -> Unit) {

        val now = LocalDate.now()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                onPicked(LocalDate.of(year, month + 1, day))
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        ).show()
    }

    private fun buildFirstTriggerAtMillis(startDate: LocalDate, time: LocalTime): Long {
        val now = LocalDateTime.now()
        var triggerDate = startDate
        var triggerDateTime = LocalDateTime.of(triggerDate, time)

        if (!triggerDateTime.isAfter(now)) {
            triggerDate = triggerDate.plusDays(1)
            triggerDateTime = LocalDateTime.of(triggerDate, time)
        }

        return triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun mapDayCodeToLabel(code: String): String {
        return when (code.trim()) {
            "MON" -> getString(R.string.care_day_mon)
            "TUE" -> getString(R.string.care_day_tue)
            "WED" -> getString(R.string.care_day_wed)
            "THU" -> getString(R.string.care_day_thu)
            "FRI" -> getString(R.string.care_day_fri)
            "SAT" -> getString(R.string.care_day_sat)
            "SUN" -> getString(R.string.care_day_sun)
            else -> code
        }
    }

    private fun hideKeyboardAndClearFocus(view: View) {
        view.clearFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun dpToPx(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}