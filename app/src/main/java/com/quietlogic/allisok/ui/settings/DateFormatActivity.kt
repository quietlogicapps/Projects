package com.quietlogic.allisok.ui.settings

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import com.quietlogic.allisok.data.repository.SettingsRepository
import com.quietlogic.allisok.ui.home.Button3D
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

        val db = DatabaseProvider.getDatabase(applicationContext)
        val repository = SettingsRepository(db.appSettingsDao())

        val btnEU = findViewById<MaterialButton>(R.id.btnDateFormatEU)
        val btnUS = findViewById<MaterialButton>(R.id.btnDateFormatUS)

        btnEU.text = buildStyledLabel("EU", "DD/MM/YYYY")
        btnUS.text = buildStyledLabel("US", "MM/DD/YYYY")

        Button3D.apply(btnEU, 16f, 6f)
        Button3D.apply(btnUS, 16f, 6f)

        lifecycleScope.launch {
            val current = repository.getSettings().first()
            val active = current?.dateFormat ?: "EU"
            applyActiveStyle(btnEU, btnUS, active)
        }

        btnEU.setOnClickListener {
            lifecycleScope.launch {
                val current = repository.getSettings().first()
                val updated = current?.copy(dateFormat = "EU")
                    ?: AppSettingsEntity(
                        appPinHash = null,
                        adminPinHash = null,
                        trialStartTimestamp = null,
                        dateFormat = "EU"
                    )
                repository.saveSettings(updated)
                applyActiveStyle(btnEU, btnUS, "EU")
                kotlinx.coroutines.delay(300)
                finish()
            }
        }

        btnUS.setOnClickListener {
            lifecycleScope.launch {
                val current = repository.getSettings().first()
                val updated = current?.copy(dateFormat = "US")
                    ?: AppSettingsEntity(
                        appPinHash = null,
                        adminPinHash = null,
                        trialStartTimestamp = null,
                        dateFormat = "US"
                    )
                repository.saveSettings(updated)
                applyActiveStyle(btnEU, btnUS, "US")
                kotlinx.coroutines.delay(300)
                finish()
            }
        }
    }

    private fun buildStyledLabel(code: String, example: String): SpannableString {
        val fullText = "$code\n$example"
        val spannable = SpannableString(fullText)

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            code.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val exampleStart = code.length + 1
        spannable.setSpan(
            RelativeSizeSpan(0.85f),
            exampleStart,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

    private fun applyActiveStyle(btnEU: MaterialButton, btnUS: MaterialButton, active: String) {
        val activeAlpha = 1.0f
        val inactiveAlpha = 0.70f

        if (active == "US") {
            btnUS.alpha = activeAlpha
            btnEU.alpha = inactiveAlpha
        } else {
            btnEU.alpha = activeAlpha
            btnUS.alpha = inactiveAlpha
        }
    }
}