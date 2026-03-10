package com.quietlogic.allisok.ui.backup

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.alarm.engine.AlarmRescheduler
import com.quietlogic.allisok.data.backup.RestoreRepository
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

            lifecycleScope.launch {

                withContext(Dispatchers.IO) {
                    val db = DatabaseProvider.getDatabase(applicationContext)
                    val repository = RestoreRepository(db)
                    repository.restoreFromJson(json)

                    val rescheduler = AlarmRescheduler(applicationContext)
                    rescheduler.rescheduleAll()
                }

                Toast.makeText(
                    this@ImportActivity,
                    "Backup restored",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}