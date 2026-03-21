package com.quietlogic.allisok.ui.trial

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class TrialEndedActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_trial_ended)

        findViewById<Button>(R.id.btnBuy).setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.trial_purchase_soon),
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<Button>(R.id.btnRestore).setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.trial_restore_soon),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onBackPressed() {
        // Block back button
    }
}