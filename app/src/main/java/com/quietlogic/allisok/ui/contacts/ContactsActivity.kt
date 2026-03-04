package com.quietlogic.allisok.ui.contacts

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        title = "CONTACTS"

        val relative = findViewById<Button>(R.id.buttonRelative)
        val doctor = findViewById<Button>(R.id.buttonDoctor)
        val contact3 = findViewById<Button>(R.id.buttonContact3)
        val emergency = findViewById<Button>(R.id.buttonEmergency)

        val handler = {
            Toast.makeText(this, "Contact not configured", Toast.LENGTH_SHORT).show()
        }

        relative.setOnClickListener { handler() }
        doctor.setOnClickListener { handler() }
        contact3.setOnClickListener { handler() }
        emergency.setOnClickListener { handler() }
    }
}