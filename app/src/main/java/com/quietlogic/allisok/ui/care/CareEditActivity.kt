package com.quietlogic.allisok.ui.care

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.AppDatabase

class CareEditActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

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
        val btnSave = findViewById<Button>(R.id.btnSaveCare)

        val instructions = listOf("None", "Before food", "After food")
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            instructions
        )
        instructionSpinner.adapter = spinnerAdapter

        btnAddTime.setOnClickListener {
            Toast.makeText(this, "ADD TIME (next step)", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            val name = nameInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val instruction = instructionSpinner.selectedItem?.toString() ?: "None"
            Toast.makeText(this, "SAVE (demo): $name / $instruction", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}