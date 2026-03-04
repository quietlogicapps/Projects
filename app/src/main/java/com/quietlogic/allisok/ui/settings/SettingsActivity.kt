package com.quietlogic.allisok.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = "SETTINGS"
    }

}