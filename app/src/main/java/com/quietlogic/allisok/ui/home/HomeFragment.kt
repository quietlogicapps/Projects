package com.quietlogic.allisok.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.care.CareActivity
import com.quietlogic.allisok.ui.contacts.ContactsActivity
import com.quietlogic.allisok.ui.info.InfoActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactsButton = view.findViewById<MaterialButton>(R.id.buttonContacts)
        val careButton = view.findViewById<MaterialButton>(R.id.buttonCare)
        val infoButton = view.findViewById<MaterialButton>(R.id.buttonInfo)

        contactsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ContactsActivity::class.java))
        }

        careButton.setOnClickListener {
            startActivity(Intent(requireContext(), CareActivity::class.java))
        }

        infoButton.setOnClickListener {
            startActivity(Intent(requireContext(), InfoActivity::class.java))
        }
    }
}
