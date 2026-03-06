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
import com.quietlogic.allisok.ui.contacts.ContactsActivity
import com.quietlogic.allisok.ui.care.CareActivity
import com.quietlogic.allisok.ui.info.InfoActivity

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

        menuInflater.inflate(R.menu.menu_home, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.actionSecurity -> {
                return true
            }

            R.id.actionLanguage -> {
                return true
            }

            R.id.actionExport -> {
                return true
            }

            R.id.actionImport -> {
                return true
            }

            R.id.actionHistory -> {
                return true
            }

            R.id.actionPrivacy -> {
                return true
            }

            R.id.actionMoreApps -> {

                val uri = Uri.parse("https://play.google.com/store/apps/dev?id=QuietLogic")
                startActivity(Intent(Intent.ACTION_VIEW, uri))

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}