package com.quietlogic.allisok.ui.info

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity
import com.quietlogic.allisok.data.repository.InfoRepository
import com.quietlogic.allisok.databinding.ActivityEmergencyInfoEditBinding
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.ui.home.Button3D
import kotlinx.coroutines.launch

class EmergencyInfoEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyInfoEditBinding
    private lateinit var repository: InfoRepository

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
            LockGate.requireAdminUnlock(this)
        }

        binding = ActivityEmergencyInfoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Button3D.apply(binding.buttonSaveEmergencyInfo, 16f)
        Button3D.apply(binding.buttonClearEmergencyInfo, 16f)

        title = getString(R.string.edit_emergency_title)

        val database = DatabaseProvider.getDatabase(this)
        repository = InfoRepository(database.emergencyInfoDao())

        lifecycleScope.launch {
            repository.getInfo().collect { info ->
                if (binding.editBloodType.text.toString() != (info?.bloodType ?: "")) {
                    binding.editBloodType.setText(info?.bloodType ?: "")
                }
                if (binding.editAllergies.text.toString() != (info?.allergies ?: "")) {
                    binding.editAllergies.setText(info?.allergies ?: "")
                }
                if (binding.editConditions.text.toString() != (info?.conditions ?: "")) {
                    binding.editConditions.setText(info?.conditions ?: "")
                }
                if (binding.editNotes.text.toString() != (info?.notes ?: "")) {
                    binding.editNotes.setText(info?.notes ?: "")
                }
            }
        }

        binding.buttonSaveEmergencyInfo.setOnClickListener {
            lifecycleScope.launch {
                repository.saveInfo(
                    EmergencyInfoEntity(
                        id = 1,
                        bloodType = binding.editBloodType.text.toString().trim(),
                        allergies = binding.editAllergies.text.toString().trim(),
                        conditions = binding.editConditions.text.toString().trim(),
                        notes = binding.editNotes.text.toString().trim()
                    )
                )

                Toast.makeText(
                    this@EmergencyInfoEditActivity,
                    getString(R.string.emergency_info_saved),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }

        binding.buttonClearEmergencyInfo.setOnClickListener {
            lifecycleScope.launch {
                repository.clear()
                Toast.makeText(
                    this@EmergencyInfoEditActivity,
                    getString(R.string.emergency_info_cleared),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LockGate.REQUEST_ADMIN_UNLOCK) {
            if (resultCode != RESULT_OK) {
                finish()
            }
        }
    }
}