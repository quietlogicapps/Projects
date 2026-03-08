package com.quietlogic.allisok.ui.contacts

import android.app.Activity
import android.app.AlertDialog
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
            openAdminContactActions(R.id.buttonRelative)
            true
        }

        buttonDoctor.setOnLongClickListener {
            openAdminContactActions(R.id.buttonDoctor)
            true
        }

        buttonContact3.setOnLongClickListener {
            openAdminContactActions(R.id.buttonContact3)
            true
        }

        buttonEmergency.setOnLongClickListener {
            openAdminContactActions(R.id.buttonEmergency)
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
                showContactActionsDialog(pendingButtonId)
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

    private fun openAdminContactActions(buttonId: Int) {
        pendingButtonId = buttonId

        AdminGate.requireAdmin(this) {
            if (AdminSession.isActive()) {
                showContactActionsDialog(buttonId)
            } else {
                LockGate.requireAdminUnlock(this)
            }
        }
    }

    private fun showContactActionsDialog(buttonId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Contact options")
            .setItems(arrayOf("Select from phone contacts", "Delete contact")) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, ContactEditActivity::class.java)
                        intent.putExtra("buttonId", buttonId)
                        startActivityForResult(intent, REQUEST_EDIT_CONTACT)
                    }

                    1 -> {
                        deleteContact(buttonId)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteContact(buttonId: Int) {
        val slotId = when (buttonId) {
            R.id.buttonRelative -> 1
            R.id.buttonDoctor -> 2
            R.id.buttonContact3 -> 3
            R.id.buttonEmergency -> 4
            else -> 0
        }

        if (slotId == 0) return

        lifecycleScope.launch {
            repository.saveContact(
                ContactSlotEntity(
                    slotId = slotId,
                    label = "",
                    phoneNumber = null,
                    iconType = "default"
                )
            )
        }
    }

    private fun applyContacts(contacts: List<ContactSlotEntity>) {
        buttonRelative.text =
            contacts.firstOrNull { it.slotId == 1 }?.label?.takeIf { it.isNotBlank() } ?: "RELATIVE"

        buttonDoctor.text =
            contacts.firstOrNull { it.slotId == 2 }?.label?.takeIf { it.isNotBlank() } ?: "DOCTOR"

        buttonContact3.text =
            contacts.firstOrNull { it.slotId == 3 }?.label?.takeIf { it.isNotBlank() } ?: "CONTACT 3"

        buttonEmergency.text =
            contacts.firstOrNull { it.slotId == 4 }?.label?.takeIf { it.isNotBlank() } ?: "EMERGENCY"
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