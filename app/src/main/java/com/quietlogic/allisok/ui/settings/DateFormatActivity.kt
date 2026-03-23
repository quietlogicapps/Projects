package com.quietlogic.allisok.ui.settings

import android.content.Context
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
        setContentView(R.layout.activity_date_format)

        title = getString(R.string.date_format_title)

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