package com.quietlogic.allisok.ui.care

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class CareEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_care_edit)

        title = "Add Care Item"

        val nameInput = findViewById<EditText>(R.id.inputName)
        val instructionSpinner = findViewById<Spinner>(R.id.spinnerInstruction)
        val saveButton = findViewById<Button>(R.id.btnSaveCare)

        val instructions = listOf(
            "None",
            "Before food",
            "After food"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            instructions
        )

        instructionSpinner.adapter = adapter

        saveButton.setOnClickListener {

            val name = nameInput.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Care item saved (demo)", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}