package com.quietlogic.allisok.ui.backup

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import java.io.File

class ImportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_import)

        title = "IMPORT DATA"

        val buttonLoadBackup = findViewById<Button>(R.id.buttonLoadBackup)
        val textImportPreview = findViewById<TextView>(R.id.textImportPreview)

        buttonLoadBackup.setOnClickListener {

            val file = File(filesDir, "allisok_backup.json")

            if (!file.exists()) {

                Toast.makeText(
                    this,
                    "Backup file not found",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val json = file.readText()

            textImportPreview.text = json

            Toast.makeText(
                this,
                "Backup loaded",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}