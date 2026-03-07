package com.quietlogic.allisok.ui.home

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.care.CareActivity
import com.quietlogic.allisok.ui.contacts.ContactsActivity
import com.quietlogic.allisok.ui.info.InfoActivity
import com.quietlogic.allisok.ui.security.SecurityActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.overflowIcon?.setTint(Color.WHITE)

        val contactsButton = findViewById<Button>(R.id.buttonContacts)
        val careButton = findViewById<Button>(R.id.buttonCare)
        val infoButton = findViewById<Button>(R.id.buttonInfo)

        contactsButton.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        careButton.setOnClickListener {
            startActivity(Intent(this, CareActivity::class.java))
        }

        infoButton.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_security -> {
                startActivity(Intent(this, SecurityActivity::class.java))
                return true
            }

            R.id.menu_language -> return true
            R.id.menu_export -> return true
            R.id.menu_import -> return true
            R.id.menu_history -> return true

            R.id.menu_more_apps -> {
                val uri = Uri.parse("https://play.google.com/store/apps/dev?id=QuietLogic")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}