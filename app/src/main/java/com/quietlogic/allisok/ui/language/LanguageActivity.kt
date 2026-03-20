package com.quietlogic.allisok.ui.language

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.PermissionSetupActivity
import com.quietlogic.allisok.R
import java.util.Locale

class LanguageActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"

        val locale = Locale(languageCode)
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

        findViewById<Button>(R.id.buttonLanguageEnglish).setOnClickListener {
            setLanguage("en")
        }

        findViewById<Button>(R.id.buttonLanguageSpanish).setOnClickListener {
            setLanguage("es")
        }

        findViewById<Button>(R.id.buttonLanguageGerman).setOnClickListener {
            setLanguage("de")
        }
    }

    private fun setLanguage(langCode: String) {
        prefs.edit().putString("app_language", langCode).commit()

        val intent = Intent(this, PermissionSetupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}