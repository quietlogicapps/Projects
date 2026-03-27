package com.quietlogic.allisok.ui.trial

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.databinding.ActivityTrialEndedBinding
import com.quietlogic.allisok.ui.home.Button3D

class TrialEndedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrialEndedBinding

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

        binding = ActivityTrialEndedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Button3D.apply(binding.btnBuy, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(binding.btnRestore, cornerDp = 16f, depthDp = 6f)

        binding.btnBuy.setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.trial_purchase_soon),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnRestore.setOnClickListener {
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
