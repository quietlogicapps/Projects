package com.quietlogic.allisok.ui.care

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

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

            val instruction = instructionSpinner.selectedItem.toString()

            val item = CareItemEntity(
                name = name,
                instruction = instruction,
                startDate = LocalDate.now(),
                endDate = LocalDate.now().plusDays(30),
                repeatType = "DAILY"
            )

            lifecycleScope.launch {

                withContext(Dispatchers.IO) {

                    // 1) insert item
                    val itemId = db.careItemDao().insert(item)

                    // 2) insert times
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

        val joined = times.joinToString(separator = ", ") { it.format(timeFormatter) }
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
}