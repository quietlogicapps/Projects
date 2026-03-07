package com.quietlogic.allisok.ui.care

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import java.time.LocalTime
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
        val instructionSpinner = findViewById<Spinner>(R.id.spinnerInstruction)
        val repeatSpinner = findViewById<Spinner>(R.id.spinnerRepeat)

        val btnPickDays = findViewById<Button>(R.id.btnPickDays)
        val textRepeatDays = findViewById<TextView>(R.id.textRepeatDays)

        val btnPickStart = findViewById<Button>(R.id.btnPickStart)
        val btnPickEnd = findViewById<Button>(R.id.btnPickEnd)

        val textStart = findViewById<TextView>(R.id.textStart)
        val textEnd = findViewById<TextView>(R.id.textEnd)

        val btnAddTime = findViewById<Button>(R.id.btnAddTime)
        val textTimes = findViewById<TextView>(R.id.textTimes)

        val btnSave = findViewById<Button>(R.id.btnSaveCare)

        val instructions = listOf("None", "Before food", "After food")

        instructionSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, instructions)

        val repeatOptions = listOf("Daily", "Specific days")

        repeatSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, repeatOptions)

        repeatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (position == 0) {
                    textRepeatDays.text = "Days: Daily"
                } else {
                    textRepeatDays.text = "Days: Not selected"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnPickDays.setOnClickListener {

            if (repeatSpinner.selectedItemPosition == 0) {
                Toast.makeText(this, "Repeat is Daily", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            val instruction = instructionSpinner.selectedItem.toString()

            val repeatType = if (repeatSpinner.selectedItemPosition == 0) {
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

                    times.forEach { t ->

                        db.careTimeDao().insert(
                            CareTimeEntity(
                                careItemId = itemId,
                                time = t
                            )
                        )
                    }
                }

                AlarmPlanner(this@CareEditActivity).scheduleSimpleTestAlarm()

                Toast.makeText(this@CareEditActivity, "Saved", Toast.LENGTH_SHORT).show()

                finish()
            }
        }
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
}