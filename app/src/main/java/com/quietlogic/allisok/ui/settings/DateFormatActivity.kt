package com.quietlogic.allisok.ui.settings

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DateFormatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_format)

        title = "Date Format"

        val db = DatabaseProvider.getDatabase(applicationContext)
        val repository = SettingsRepository(db.appSettingsDao())

        val btnEU = findViewById<Button>(R.id.btnDateFormatEU)
        val btnUS = findViewById<Button>(R.id.btnDateFormatUS)

        btnEU.setOnClickListener {
            lifecycleScope.launch {
                val current = repository.getSettings().first()
                val updated = current?.copy(dateFormat = "EU")
                    ?: com.quietlogic.allisok.data.local.entity.AppSettingsEntity(
                        appPinHash = null,
                        adminPinHash = null,
                        trialStartTimestamp = null,
                        dateFormat = "EU"
                    )
                repository.saveSettings(updated)
                kotlinx.coroutines.delay(300)
                finish()
            }
        }

        btnUS.setOnClickListener {
            lifecycleScope.launch {
                val current = repository.getSettings().first()
                val updated = current?.copy(dateFormat = "US")
                    ?: com.quietlogic.allisok.data.local.entity.AppSettingsEntity(
                        appPinHash = null,
                        adminPinHash = null,
                        trialStartTimestamp = null,
                        dateFormat = "US"
                    )
                repository.saveSettings(updated)
                kotlinx.coroutines.delay(300)
                finish()
            }
        }
    }
}
