package com.quietlogic.allisok.ui.home

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.ui.care.CareActivity
import com.quietlogic.allisok.ui.contacts.ContactsActivity
import com.quietlogic.allisok.ui.info.InfoActivity
import com.quietlogic.allisok.ui.pin.PinActivity
import com.quietlogic.allisok.ui.security.SecurityActivity

class HomeActivity : AppCompatActivity() {

    private var hasRequestedUserUnlock = false

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

        updateAdminIndicator()
    }

    override fun onResume() {
        super.onResume()

        updateAdminIndicator()

        if (!hasRequestedUserUnlock) {
            hasRequestedUserUnlock = true
            LockGate.requireUserUnlock(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_USER_UNLOCK) {
            when (resultCode) {
                RESULT_OK -> {}

                PinActivity.RESULT_OPEN_EMERGENCY_INFO -> {
                    startActivity(Intent(this, InfoActivity::class.java))
                    finish()
                }

                else -> {
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.home_menu, menu)

        val exitItem = menu.findItem(R.id.menu_exit_admin)

        exitItem?.isVisible = AdminSession.isActive()

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        val exitItem = menu.findItem(R.id.menu_exit_admin)

        exitItem?.isVisible = AdminSession.isActive()

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu_exit_admin -> {
                AdminSession.stop()
                invalidateOptionsMenu()
                updateAdminIndicator()
                return true
            }

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

    private fun updateAdminIndicator() {

        val indicator = findViewById<View>(R.id.viewAdminIndicator)

        indicator.visibility =
            if (AdminSession.isActive()) View.VISIBLE else View.GONE
    }
}