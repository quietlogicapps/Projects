package com.quietlogic.allisok.ui.backup

import android.content.Context
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

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_import)

        title = getString(R.string.import_title)

        val buttonLoadBackup = findViewById<Button>(R.id.buttonLoadBackup)
        val textImportPreview = findViewById<TextView>(R.id.textImportPreview)

        buttonLoadBackup.setOnClickListener {

            val file = File(filesDir, "allisok_backup.json")

            if (!file.exists()) {

                Toast.makeText(
                    this,
                    getString(R.string.import_file_not_found),
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
                    getString(R.string.import_restored),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}