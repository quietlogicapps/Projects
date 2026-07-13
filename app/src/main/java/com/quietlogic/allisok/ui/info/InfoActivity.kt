package com.quietlogic.allisok.ui.info

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.entity.RecentTakenItem
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.CareLogRepository
import com.quietlogic.allisok.data.repository.InfoRepository
import com.quietlogic.allisok.data.repository.SettingsRepository
import com.quietlogic.allisok.databinding.ActivityInfoBinding
import com.quietlogic.allisok.security.AdminGate
import com.quietlogic.allisok.security.AdminSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    private lateinit var adapter: RecentTakenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        title = getString(R.string.home_info)

        updateAdminIndicator()

        adapter = RecentTakenAdapter()

        binding.recyclerRecentTaken.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentTaken.adapter = adapter

        val bottomAirPx = (8 * resources.displayMetrics.density).toInt()
        fun applyRecyclerBottomInset(insets: WindowInsetsCompat) {
            val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            binding.recyclerRecentTaken.updatePadding(
                left = binding.recyclerRecentTaken.paddingLeft,
                top = binding.recyclerRecentTaken.paddingTop,
                right = binding.recyclerRecentTaken.paddingRight,
                bottom = navigationBottom + bottomAirPx
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            applyRecyclerBottomInset(insets)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.recyclerRecentTaken) { _, insets ->
            applyRecyclerBottomInset(insets)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

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
            val settings = SettingsRepository(DatabaseProvider.getDatabase(applicationContext).appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            val formatter = DateTimeFormatter.ofPattern(pattern)

            careLogRepository.getRecentLast72Hours().collect { list ->
                val formatted = list.map { item ->
                    val parsedDate = try {
                        LocalDate.parse(item.date).format(formatter)
                    } catch (e: Exception) {
                        item.date
                    }
                    RecentTakenItem(
                        date = parsedDate,
                        scheduledTime = item.scheduledTime,
                        careItemName = item.careItemName
                    )
                }
                adapter.submitList(formatted)
            }
        }

        lifecycleScope.launch {
            infoRepository.getInfo().collect { info ->
                binding.textBloodType.text =
                    info?.bloodType?.takeIf { it.isNotBlank() } ?: "-"
                binding.textAllergies.text =
                    info?.allergies?.takeIf { it.isNotBlank() } ?: "-"
                binding.textConditions.text =
                    info?.conditions?.takeIf { it.isNotBlank() } ?: "-"
                binding.textNotes.text =
                    info?.notes?.takeIf { it.isNotBlank() } ?: "-"
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