package com.quietlogic.allisok.ui.home

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.ui.backup.ExportActivity
import com.quietlogic.allisok.ui.backup.ImportActivity
import com.quietlogic.allisok.ui.care.CareActivity
import com.quietlogic.allisok.ui.contacts.ContactsActivity
import com.quietlogic.allisok.ui.history.HistoryActivity
import com.quietlogic.allisok.ui.info.InfoActivity
import com.quietlogic.allisok.ui.language.LanguageActivity
import com.quietlogic.allisok.ui.pin.PinActivity
import com.quietlogic.allisok.ui.security.SecurityActivity
import com.quietlogic.allisok.ui.settings.DateFormatActivity
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private var skipUserUnlockOnce = false

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"

        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            Locale(parts[0], parts[1])
        } else {
            Locale(languageCode)
        }
        Locale.setDefault(locale)

        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.toolbarHome)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.overflowIcon?.setTint(Color.WHITE)

        val contactsButton = findViewById<MaterialButton>(R.id.buttonContacts)
        val careButton = findViewById<MaterialButton>(R.id.buttonCare)
        val infoButton = findViewById<MaterialButton>(R.id.buttonInfo)

        Button3D.apply(contactsButton, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(careButton, cornerDp = 16f, depthDp = 6f)
        Button3D.apply(infoButton, cornerDp = 16f, depthDp = 6f)

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
        invalidateOptionsMenu()

        if (skipUserUnlockOnce) {
            skipUserUnlockOnce = false
            return
        }

        LockGate.requireUserUnlock(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_USER_UNLOCK) {
            when (resultCode) {
                RESULT_OK -> {}

                PinActivity.RESULT_OPEN_EMERGENCY_INFO -> {
                    skipUserUnlockOnce = true
                    startActivity(Intent(this, InfoActivity::class.java))
                }

                else -> {
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)

        val adminItem = menu.findItem(R.id.menu_exit_admin)

        adminItem?.title =
            if (AdminSession.isActive()) getString(R.string.menu_exit_admin_mode)
            else getString(R.string.menu_enter_admin)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val adminItem = menu.findItem(R.id.menu_exit_admin)

        adminItem?.title =
            if (AdminSession.isActive()) getString(R.string.menu_exit_admin_mode)
            else getString(R.string.menu_enter_admin)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.menu_exit_admin -> {
                if (AdminSession.isActive()) {
                    AdminSession.stop()
                    updateAdminIndicator()
                    invalidateOptionsMenu()
                } else {
                    val intent = Intent(this, PinActivity::class.java)
                    intent.putExtra("mode", LockGate.MODE_ADMIN_UNLOCK)
                    startActivity(intent)
                }

                return true
            }

            R.id.menu_security -> {
                startActivity(Intent(this, SecurityActivity::class.java))
                return true
            }

            R.id.menu_language -> {
                startActivity(Intent(this, LanguageActivity::class.java))
                return true
            }

            R.id.menu_date_format -> {
                startActivity(Intent(this, DateFormatActivity::class.java))
                return true
            }

            R.id.menu_export -> {
                startActivity(Intent(this, ExportActivity::class.java))
                return true
            }

            R.id.menu_import -> {
                startActivity(Intent(this, ImportActivity::class.java))
                return true
            }

            R.id.menu_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                return true
            }

            R.id.menu_more_apps -> {
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/developer?id=QuietLogic"))
                startActivity(intent)
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