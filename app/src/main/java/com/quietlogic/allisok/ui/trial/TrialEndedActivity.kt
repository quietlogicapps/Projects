package com.quietlogic.allisok.ui.trial

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class TrialEndedActivity : AppCompatActivity() {

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