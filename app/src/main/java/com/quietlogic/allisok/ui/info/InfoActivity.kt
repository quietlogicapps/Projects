package com.quietlogic.allisok.ui.info

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.CareLogRepository
import com.quietlogic.allisok.data.repository.InfoRepository
import com.quietlogic.allisok.databinding.ActivityInfoBinding
import com.quietlogic.allisok.security.AdminGate
import com.quietlogic.allisok.security.AdminSession
import kotlinx.coroutines.launch

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    private lateinit var adapter: RecentTakenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "INFO"

        updateAdminIndicator()

        adapter = RecentTakenAdapter()

        binding.recyclerRecentTaken.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentTaken.adapter = adapter

        val database = DatabaseProvider.getDatabase(this)
        val careLogRepository = CareLogRepository(database.careLogDao())
        val infoRepository = InfoRepository(database.emergencyInfoDao())

        binding.buttonEditEmergencyInfo.setOnClickListener {
            if (AdminSession.isActive()) {
                startActivity(Intent(this, EmergencyInfoEditActivity::class.java))
            } else {
                AdminGate.requireAdmin(this) {
                    startActivity(Intent(this, EmergencyInfoEditActivity::class.java))
                }
            }
        }

        lifecycleScope.launch {
            careLogRepository.getRecentLast72Hours().collect { list ->
                adapter.submitList(list)
            }
        }

        lifecycleScope.launch {
            infoRepository.getInfo().collect { info ->
                binding.textBloodType.text = "Blood type: ${info?.bloodType?.takeIf { it.isNotBlank() } ?: "-"}"
                binding.textAllergies.text = "Allergies: ${info?.allergies?.takeIf { it.isNotBlank() } ?: "-"}"
                binding.textConditions.text = "Conditions: ${info?.conditions?.takeIf { it.isNotBlank() } ?: "-"}"
                binding.textNotes.text = "Notes: ${info?.notes?.takeIf { it.isNotBlank() } ?: "-"}"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAdminIndicator()
    }

    private fun updateAdminIndicator() {
        binding.root.findViewById<View?>(com.quietlogic.allisok.R.id.viewAdminIndicator)?.visibility =
            if (AdminSession.isActive()) View.VISIBLE else View.GONE
    }
}