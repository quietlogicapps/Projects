package com.quietlogic.allisok.ui.contacts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import com.quietlogic.allisok.data.repository.ContactsRepository
import com.quietlogic.allisok.security.AdminGate
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_EDIT_CONTACT = 2001
    }

    private var pendingButtonId: Int = View.NO_ID

    private lateinit var buttonRelative: Button
    private lateinit var buttonDoctor: Button
    private lateinit var buttonContact3: Button
    private lateinit var buttonEmergency: Button

    private lateinit var repository: ContactsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_contacts)

        val db = DatabaseProvider.getDatabase(this)
        repository = ContactsRepository(db.contactSlotDao())

        buttonRelative = findViewById(R.id.buttonRelative)
        buttonDoctor = findViewById(R.id.buttonDoctor)
        buttonContact3 = findViewById(R.id.buttonContact3)
        buttonEmergency = findViewById(R.id.buttonEmergency)

        buttonRelative.setOnLongClickListener {
            openContactEdit(R.id.buttonRelative)
            true
        }

        buttonDoctor.setOnLongClickListener {
            openContactEdit(R.id.buttonDoctor)
            true
        }

        buttonContact3.setOnLongClickListener {
            openContactEdit(R.id.buttonContact3)
            true
        }

        buttonEmergency.setOnLongClickListener {
            openContactEdit(R.id.buttonEmergency)
            true
        }

        lifecycleScope.launch {
            repository.getAllContacts().collectLatest { contacts ->
                applyContacts(contacts)
            }
        }

        updateAdminIndicator()
    }

    override fun onResume() {
        super.onResume()
        updateAdminIndicator()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_ADMIN_UNLOCK) {
            if (resultCode == RESULT_OK && pendingButtonId != View.NO_ID) {
                val intent = Intent(this, ContactEditActivity::class.java)
                intent.putExtra("buttonId", pendingButtonId)
                startActivityForResult(intent, REQUEST_EDIT_CONTACT)
            } else {
                pendingButtonId = View.NO_ID
            }
            return
        }

        if (requestCode == REQUEST_EDIT_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                val name = data?.getStringExtra("name").orEmpty()
                val phone = data?.getStringExtra("number")
                val buttonId = data?.getIntExtra("buttonId", View.NO_ID) ?: View.NO_ID

                val slotId = when (buttonId) {
                    R.id.buttonRelative -> 1
                    R.id.buttonDoctor -> 2
                    R.id.buttonContact3 -> 3
                    R.id.buttonEmergency -> 4
                    else -> 0
                }

                if (slotId != 0 && name.isNotBlank()) {
                    lifecycleScope.launch {
                        repository.saveContact(
                            ContactSlotEntity(
                                slotId = slotId,
                                label = name,
                                phoneNumber = phone,
                                iconType = "default"
                            )
                        )
                    }
                }
            }

            pendingButtonId = View.NO_ID
        }
    }

    private fun openContactEdit(buttonId: Int) {
        pendingButtonId = buttonId

        AdminGate.requireAdmin(this) {
            if (AdminSession.isActive()) {
                val intent = Intent(this, ContactEditActivity::class.java)
                intent.putExtra("buttonId", buttonId)
                startActivityForResult(intent, REQUEST_EDIT_CONTACT)
            } else {
                LockGate.requireAdminUnlock(this)
            }
        }
    }

    private fun applyContacts(contacts: List<ContactSlotEntity>) {
        buttonRelative.text = contacts.firstOrNull { it.slotId == 1 }?.label ?: "RELATIVE"
        buttonDoctor.text = contacts.firstOrNull { it.slotId == 2 }?.label ?: "DOCTOR"
        buttonContact3.text = contacts.firstOrNull { it.slotId == 3 }?.label ?: "CONTACT 3"
        buttonEmergency.text = contacts.firstOrNull { it.slotId == 4 }?.label ?: "EMERGENCY"
    }

    private fun updateAdminIndicator() {
        val indicatorId = resources.getIdentifier(
            "viewAdminIndicator",
            "id",
            packageName
        )

        if (indicatorId == 0) return

        val indicator = findViewById<View>(indicatorId)
        indicator.visibility = if (AdminSession.isActive()) View.VISIBLE else View.GONE
    }
}