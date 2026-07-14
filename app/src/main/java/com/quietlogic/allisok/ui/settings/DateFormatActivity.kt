package com.quietlogic.allisok.ui.settings

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.AppSettingsEntity
import com.quietlogic.allisok.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DateFormatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_format)

        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = SettingsRepository(database.appSettingsDao())

        val btnEU = findViewById<MaterialButton>(R.id.btnDateFormatEU)
        val btnUS = findViewById<MaterialButton>(R.id.btnDateFormatUS)

        btnEU.text = buildStyledLabel("EU", "DD/MM/YYYY")
        btnUS.text = buildStyledLabel("US", "MM/DD/YYYY")

        lifecycleScope.launch {
            val currentSettings = repository.getSettings().first()
            val activeFormat = currentSettings?.dateFormat ?: "EU"

            applyActiveStyle(
                btnEU = btnEU,
                btnUS = btnUS,
                active = activeFormat
            )
        }

        btnEU.setOnClickListener {
            lifecycleScope.launch {
                val currentSettings = repository.getSettings().first()

                val updatedSettings = currentSettings?.copy(
                    dateFormat = "EU"
                ) ?: AppSettingsEntity(
                    appPinHash = null,
                    adminPinHash = null,
                    trialStartTimestamp = null,
                    dateFormat = "EU"
                )

                repository.saveSettings(updatedSettings)

                applyActiveStyle(
                    btnEU = btnEU,
                    btnUS = btnUS,
                    active = "EU"
                )
            }
        }

        btnUS.setOnClickListener {
            lifecycleScope.launch {
                val currentSettings = repository.getSettings().first()

                val updatedSettings = currentSettings?.copy(
                    dateFormat = "US"
                ) ?: AppSettingsEntity(
                    appPinHash = null,
                    adminPinHash = null,
                    trialStartTimestamp = null,
                    dateFormat = "US"
                )

                repository.saveSettings(updatedSettings)

                applyActiveStyle(
                    btnEU = btnEU,
                    btnUS = btnUS,
                    active = "US"
                )
            }
        }
    }

    private fun buildStyledLabel(
        code: String,
        example: String
    ): SpannableString {
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

    private fun applyActiveStyle(
        btnEU: MaterialButton,
        btnUS: MaterialButton,
        active: String
    ) {
        val activeAlpha = 1.0f
        val inactiveAlpha = 0.70f
        val activeStrokeColor = Color.parseColor("#22C55E")

        val activeStrokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            2f,
            resources.displayMetrics
        ).toInt()

        btnEU.strokeWidth = 0
        btnUS.strokeWidth = 0

        if (active == "US") {
            btnUS.alpha = activeAlpha
            btnEU.alpha = inactiveAlpha
            btnUS.strokeColor = ColorStateList.valueOf(activeStrokeColor)
            btnUS.strokeWidth = activeStrokeWidth
        } else {
            btnEU.alpha = activeAlpha
            btnUS.alpha = inactiveAlpha
            btnEU.strokeColor = ColorStateList.valueOf(activeStrokeColor)
            btnEU.strokeWidth = activeStrokeWidth
        }
    }
}