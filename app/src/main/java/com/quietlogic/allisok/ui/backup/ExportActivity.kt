package com.quietlogic.allisok.ui.backup

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.backup.BackupRepository
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExportActivity : AppCompatActivity() {

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

        setContentView(R.layout.activity_export)

        title = getString(R.string.export_title)

        val buttonGenerateExport = findViewById<Button>(R.id.buttonGenerateExport)
        val textExportJson = findViewById<TextView>(R.id.textExportJson)

        buttonGenerateExport.setOnClickListener {

            lifecycleScope.launch {

                textExportJson.text = getString(R.string.export_generating)

                val json = withContext(Dispatchers.IO) {

                    val db = DatabaseProvider.getDatabase(applicationContext)
                    val repository = BackupRepository(db)
                    repository.buildExportJson()
                }

                textExportJson.text = json

                withContext(Dispatchers.IO) {

                    val file = File(filesDir, "allisok_backup.json")
                    file.writeText(json)

                    file.absolutePath
                }.also { path ->

                    Toast.makeText(
                        this@ExportActivity,
                        getString(R.string.export_saved, path),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}