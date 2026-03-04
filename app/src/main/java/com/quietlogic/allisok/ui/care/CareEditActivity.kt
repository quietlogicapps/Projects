package com.quietlogic.allisok.ui.care

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.entity.CareItemEntity
import com.quietlogic.allisok.data.local.entity.CareTimeEntity
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

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_care_edit)

        title = "Add Care Item"

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "allisok-db"
        ).build()

        val nameInput = findViewById<EditText>(R.id.inputName)
        val instructionSpinner = findViewById<Spinner>(R.id.spinnerInstruction)

        val btnPickStart = findViewById<Button>(R.id.btnPickStart)
        val btnPickEnd = findViewById<Button>(R.id.btnPickEnd)

        val textStart = findViewById<TextView>(R.id.textStart)
        val textEnd = findViewById<TextView>(R.id.textEnd)

        val btnAddTime = findViewById<Button>(R.id.btnAddTime)
        val textTimes = findViewById<TextView>(R.id.textTimes)

        val btnSave = findViewById<Button>(R.id.btnSaveCare)

        val instructions = listOf("None", "Before food", "After food")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            instructions
        )
        instructionSpinner.adapter = spinnerAdapter

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

            val item = CareItemEntity(
                name = name,
                instruction = instruction,
                startDate = start,
                endDate = end,
                repeatType = "DAILY"
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

                Toast.makeText(
                    this@CareEditActivity,
                    "Saved",
                    Toast.LENGTH_SHORT
                ).show()

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

        val dialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                onPicked(LocalTime.of(hourOfDay, minute))
            },
            now.hour,
            now.minute,
            true
        )

        dialog.show()
    }

    private fun openDatePicker(onPicked: (LocalDate) -> Unit) {

        val now = LocalDate.now()

        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                onPicked(LocalDate.of(year, month + 1, day))
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )

        dialog.show()
    }
}