package com.quietlogic.allisok.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.history.HistoryActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        title = getString(R.string.settings_title)

        val buttonExport = findViewById<Button>(R.id.buttonExport)
        val buttonImport = findViewById<Button>(R.id.buttonImport)
        val buttonMoreApps = findViewById<Button>(R.id.buttonMoreApps)
        val buttonHistory = findViewById<Button>(R.id.buttonHistory)

        buttonHistory.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.settings_history_opening),
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(this, HistoryActivity::class.java)
            )
        }

        buttonMoreApps.setOnClickListener {
            val uri = Uri.parse(
                "https://play.google.com/store/apps/dev?id=QuietLogic"
            )

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        buttonExport.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.settings_export_pressed),
                Toast.LENGTH_SHORT
            ).show()
        }

        buttonImport.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.settings_import_pressed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}