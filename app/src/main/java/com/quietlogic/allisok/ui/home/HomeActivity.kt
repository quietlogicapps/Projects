package com.quietlogic.allisok.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.contacts.ContactsActivity
import com.quietlogic.allisok.ui.care.CareActivity
import com.quietlogic.allisok.ui.info.InfoActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val contactsButton = findViewById<Button>(R.id.buttonContacts)
        val careButton = findViewById<Button>(R.id.buttonCare)
        val infoButton = findViewById<Button>(R.id.buttonInfo)

        contactsButton.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        careButton.setOnClickListener {
            val intent = Intent(this, CareActivity::class.java)
            startActivity(intent)
        }

        infoButton.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
    }
}