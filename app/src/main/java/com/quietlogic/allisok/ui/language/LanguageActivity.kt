package com.quietlogic.allisok.ui.language

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class LanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Language"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}