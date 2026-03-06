package com.quietlogic.allisok.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "SETTINGS"

        binding.buttonMoreApps.setOnClickListener {

            val uri = Uri.parse(
                "https://play.google.com/store/apps/dev?id=QuietLogic"
            )

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}