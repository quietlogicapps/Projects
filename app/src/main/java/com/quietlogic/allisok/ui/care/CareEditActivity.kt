package com.quietlogic.allisok.ui.care

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmPlanner
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
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

    private val days = arrayOf(
        "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"
    )

    private val checkedDays = BooleanArray(days.size)

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AdminSession.isActive()) {
            LockGate.requireAdminUnlock(this)
        }

        setContentView(R.layout.activity_care_edit)

        title = "Add Care Item"

        db = DatabaseProvider.getDatabase(applicationContext)

        val nameInput = findViewById<EditText>(R.id.inputName)

        val groupInstruction = findViewById<RadioGroup>(R.id.groupInstruction)

        val groupRepeat = findViewById<RadioGroup>(R.id.groupRepeat)
        val radioDaily = findViewById<RadioButton>(R.id.radioDaily)
        val radioSpecific = findViewById<RadioButton>(R.id.radioSpecific)

        val btnPickDays = findViewById<Button>(R.id.btnPickDays)
        val textRepeatDays = findViewById<TextView>(R.id.textRepeatDays)

        val btnPickStart = findViewById<Button>(R.id.btnPickStart)
        val btnPickEnd = findViewById<Button>(R.id.btnPickEnd)

        val textStart = findViewById<TextView>(R.id.textStart)
        val textEnd = findViewById<TextView>(R.id.textEnd)

        val btnAddTime = findViewById<Button>(R.id.btnAddTime)
        val textTimes = findViewById<TextView>(R.id.textTimes)

        val btnSave = findViewById<Button>(R.id.btnSaveCare)

        btnPickDays.visibility = View.GONE

        groupRepeat.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.radioDaily) {
                btnPickDays.visibility = View.GONE
                textRepeatDays.text = "Days: Daily"
                selectedDays.clear()
            }

            if (checkedId == R.id.radioSpecific) {
                btnPickDays.visibility = View.VISIBLE

                openDaysDialog(textRepeatDays)
            }
        }

        btnPickDays.setOnClickListener {
            openDaysDialog(textRepeatDays)
        }

        renderTimes(textTimes)

        btnPickStart.setOnClickListener {
            openDatePicker { date ->
                startDate = date
                textStart.text = "Start: ${date.format(dateFormatter)}"
            }
        }

        btnPickEnd.setOnClickListener {
            openDatePicker { date ->
                endDate = date
                textEnd.text = "End: ${date.format(dateFormatter)}"
            }
        }

        btnAddTime.setOnClickListener {

            openTimePicker { picked ->

                if (times.contains(picked)) {
                    Toast.makeText(this, "Time already added", Toast.LENGTH_SHORT).show()
                    return@openTimePicker
                }

                times.add(picked)
                times.sort()

                renderTimes(textTimes)
            }
        }

        btnSave.setOnClickListener {

            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (times.isEmpty()) {
                Toast.makeText(this, "Add at least one time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val start = startDate ?: LocalDate.now()
            val end = endDate ?: start.plusDays(30)

            val instruction = when (groupInstruction.checkedRadioButtonId) {
                R.id.radioBefore -> "Before food"
                R.id.radioAfter -> "After food"
                else -> "None"
            }

            val repeatType = if (groupRepeat.checkedRadioButtonId == R.id.radioDaily) {
                "DAILY"
            } else {
                if (selectedDays.isEmpty()) {
                    Toast.makeText(this, "Select days", Toast.LENGTH_SHORT).show()
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

                Toast.makeText(this@CareEditActivity, "Saved", Toast.LENGTH_SHORT).show()

                finish()
            }
        }
    }

    private fun openDaysDialog(textRepeatDays: TextView) {

        AlertDialog.Builder(this)
            .setTitle("Select days")
            .setMultiChoiceItems(days, checkedDays) { _, which, isChecked ->
                checkedDays[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->

                selectedDays.clear()

                for (i in days.indices) {
                    if (checkedDays[i]) {
                        selectedDays.add(days[i])
                    }
                }

                if (selectedDays.isEmpty()) {
                    textRepeatDays.text = "Days: Not selected"
                } else {
                    textRepeatDays.text =
                        "Days: " + selectedDays.joinToString(", ")
                }
            }
            .setNegativeButton("Cancel", null)
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

    private fun renderTimes(textTimes: TextView) {

        if (times.isEmpty()) {
            textTimes.text = "No times added"
            return
        }

        val joined = times.joinToString(", ") { it.format(timeFormatter) }

        textTimes.text = joined
    }

    private fun openTimePicker(onPicked: (LocalTime) -> Unit) {

        val now = LocalTime.now()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onPicked(LocalTime.of(hourOfDay, minute))
            },
            now.hour,
            now.minute,
            true
        ).show()
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
}