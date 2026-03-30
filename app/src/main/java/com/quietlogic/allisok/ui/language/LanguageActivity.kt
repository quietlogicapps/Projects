package com.quietlogic.allisok.ui.language

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.PermissionSetupActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.home.Button3D
import java.util.Locale

class LanguageActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"

        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            Locale(parts[0], parts[1])
        } else {
            Locale(languageCode)
        }
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.language_screen_title)

        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

        val buttonEnglish = findViewById<MaterialButton>(R.id.buttonLanguageEnglish)
        val buttonSpanish = findViewById<MaterialButton>(R.id.buttonLanguageSpanish)
        val buttonPortuguese = findViewById<MaterialButton>(R.id.buttonLanguagePortuguese)
        val buttonGerman = findViewById<MaterialButton>(R.id.buttonLanguageGerman)
        val buttonFrench = findViewById<MaterialButton>(R.id.buttonLanguageFrench)
        val buttonItalian = findViewById<MaterialButton>(R.id.buttonLanguageItalian)
        val buttonTurkish = findViewById<MaterialButton>(R.id.buttonLanguageTurkish)
        val buttonPolish = findViewById<MaterialButton>(R.id.buttonLanguagePolish)
        val buttonRussian = findViewById<MaterialButton>(R.id.buttonLanguageRussian)

        Button3D.apply(buttonEnglish, 16f, 6f)
        Button3D.apply(buttonSpanish, 16f, 6f)
        Button3D.apply(buttonPortuguese, 16f, 6f)
        Button3D.apply(buttonGerman, 16f, 6f)
        Button3D.apply(buttonFrench, 16f, 6f)
        Button3D.apply(buttonItalian, 16f, 6f)
        Button3D.apply(buttonTurkish, 16f, 6f)
        Button3D.apply(buttonPolish, 16f, 6f)
        Button3D.apply(buttonRussian, 16f, 6f)

        buttonEnglish.setOnClickListener { setLanguage("en") }
        buttonSpanish.setOnClickListener { setLanguage("es") }
        buttonPortuguese.setOnClickListener { setLanguage("pt-BR") }
        buttonGerman.setOnClickListener { setLanguage("de") }
        buttonFrench.setOnClickListener { setLanguage("fr") }
        buttonItalian.setOnClickListener { setLanguage("it") }
        buttonTurkish.setOnClickListener { setLanguage("tr") }
        buttonPolish.setOnClickListener { setLanguage("pl") }
        buttonRussian.setOnClickListener { setLanguage("ru") }

        updateCheckmarks()
    }

    private fun updateCheckmarks() {
        val currentLang = prefs.getString("app_language", "en") ?: "en"

        val buttons = mapOf(
            "en" to R.id.buttonLanguageEnglish,
            "es" to R.id.buttonLanguageSpanish,
            "pt-BR" to R.id.buttonLanguagePortuguese,
            "de" to R.id.buttonLanguageGerman,
            "fr" to R.id.buttonLanguageFrench,
            "it" to R.id.buttonLanguageItalian,
            "tr" to R.id.buttonLanguageTurkish,
            "pl" to R.id.buttonLanguagePolish,
            "ru" to R.id.buttonLanguageRussian
        )

        val labels = mapOf(
            "en" to getString(R.string.lang_english),
            "es" to getString(R.string.lang_spanish),
            "pt-BR" to getString(R.string.lang_portuguese),
            "de" to getString(R.string.lang_german),
            "fr" to getString(R.string.lang_french),
            "it" to getString(R.string.lang_italian),
            "tr" to getString(R.string.lang_turkish),
            "pl" to getString(R.string.lang_polish),
            "ru" to getString(R.string.lang_russian)
        )

        for ((code, btnId) in buttons) {
            val btn = findViewById<MaterialButton>(btnId)
            val label = labels[code] ?: ""
            btn.text = if (code == currentLang) "$label  ✓" else label
        }
    }

    private fun setLanguage(langCode: String) {
        prefs.edit().putString("app_language", langCode).commit()

        val intent = Intent(this, PermissionSetupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("skip_pin", true)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}