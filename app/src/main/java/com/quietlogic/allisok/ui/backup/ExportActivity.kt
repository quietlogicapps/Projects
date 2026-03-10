package com.quietlogic.allisok.ui.backup

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.backup.BackupRepository
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_export)

        title = "EXPORT DATA"

        val buttonGenerateExport = findViewById<Button>(R.id.buttonGenerateExport)
        val textExportJson = findViewById<TextView>(R.id.textExportJson)

        buttonGenerateExport.setOnClickListener {

            lifecycleScope.launch {
                textExportJson.text = "Generating..."

                val json = withContext(Dispatchers.IO) {
                    val db = DatabaseProvider.getDatabase(applicationContext)
                    val repository = BackupRepository(db)
                    repository.buildExportJson()
                }

                textExportJson.text = json
            }
        }
    }
}