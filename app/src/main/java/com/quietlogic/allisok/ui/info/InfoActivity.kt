package com.quietlogic.allisok.ui.info

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        title = "INFO"
    }

}