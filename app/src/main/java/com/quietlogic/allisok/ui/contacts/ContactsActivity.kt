package com.quietlogic.allisok.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminGate

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        title = "CONTACTS"

        val relative = findViewById<Button>(R.id.buttonRelative)
        val doctor = findViewById<Button>(R.id.buttonDoctor)
        val contact3 = findViewById<Button>(R.id.buttonContact3)
        val emergency = findViewById<Button>(R.id.buttonEmergency)

        val handler = { Toast.makeText(this, "Contact not configured", Toast.LENGTH_SHORT).show() }

        relative.setOnClickListener { handler() }
        doctor.setOnClickListener { handler() }
        contact3.setOnClickListener { handler() }
        emergency.setOnClickListener { handler() }

        relative.setOnLongClickListener { openEditAdmin(); true }
        doctor.setOnLongClickListener { openEditAdmin(); true }
        contact3.setOnLongClickListener { openEditAdmin(); true }
        emergency.setOnLongClickListener { openEditAdmin(); true }
    }

    private fun openEditAdmin() {
        AdminGate.requireAdmin(this) {
            startActivity(Intent(this, ContactEditActivity::class.java))
        }
    }
}