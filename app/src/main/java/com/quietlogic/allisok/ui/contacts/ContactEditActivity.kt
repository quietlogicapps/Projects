package com.quietlogic.allisok.ui.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quietlogic.allisok.R

class ContactEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_edit)
        title = "EDIT CONTACT"
    }

}