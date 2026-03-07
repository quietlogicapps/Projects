package com.quietlogic.allisok.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R
import com.quietlogic.allisok.security.AdminGate
import com.quietlogic.allisok.security.AdminSession

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_contacts)

        val buttonRelative = findViewById<Button>(R.id.buttonRelative)
        val buttonDoctor = findViewById<Button>(R.id.buttonDoctor)
        val buttonContact3 = findViewById<Button>(R.id.buttonContact3)
        val buttonEmergency = findViewById<Button>(R.id.buttonEmergency)

        buttonRelative.setOnLongClickListener {
            AdminGate.requireAdmin(this) {
                startActivity(Intent(this, ContactEditActivity::class.java))
            }
            true
        }

        buttonDoctor.setOnLongClickListener {
            AdminGate.requireAdmin(this) {
                startActivity(Intent(this, ContactEditActivity::class.java))
            }
            true
        }

        buttonContact3.setOnLongClickListener {
            AdminGate.requireAdmin(this) {
                startActivity(Intent(this, ContactEditActivity::class.java))
            }
            true
        }

        buttonEmergency.setOnLongClickListener {
            AdminGate.requireAdmin(this) {
                startActivity(Intent(this, ContactEditActivity::class.java))
            }
            true
        }

        updateAdminIndicator()
    }

    override fun onResume() {
        super.onResume()
        updateAdminIndicator()
    }

    private fun updateAdminIndicator() {

        val indicatorId = resources.getIdentifier(
            "viewAdminIndicator",
            "id",
            packageName
        )

        if (indicatorId == 0) return

        val indicator = findViewById<View>(indicatorId)

        indicator.visibility =
            if (AdminSession.isActive()) View.VISIBLE else View.GONE
    }
}