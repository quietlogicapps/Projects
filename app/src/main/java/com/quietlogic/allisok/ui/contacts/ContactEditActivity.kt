package com.quietlogic.allisok.ui.contacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.ui.home.Button3D
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminSession

class ContactEditActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PICK_CONTACT = 1001
    }

    private var buttonId: Int = View.NO_ID

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE)
        val languageCode = prefs.getString("app_language", "en") ?: "en"
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            java.util.Locale(parts[0], parts[1])
        } else {
            java.util.Locale(languageCode)
        }
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AdminSession.isActive()) {
            finish()
            return
        }

        setContentView(R.layout.activity_contact_edit)
        title = getString(R.string.contact_edit_title)

        buttonId = intent.getIntExtra("buttonId", View.NO_ID)

        val buttonPick = findViewById<MaterialButton>(R.id.buttonPickContact)
        Button3D.apply(buttonPick, cornerDp = 16f, depthDp = 6f)

        buttonPick.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            )

            startActivityForResult(intent, REQUEST_PICK_CONTACT)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return

            val cursor = contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val result = Intent()
                    result.putExtra("buttonId", buttonId)
                    result.putExtra("name", it.getString(0))
                    result.putExtra("number", it.getString(1))
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            }
        }
    }
}