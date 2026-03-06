package com.quietlogic.allisok.ui.info

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.local.entity.EmergencyInfoEntity
import com.quietlogic.allisok.data.repository.InfoRepository
import com.quietlogic.allisok.databinding.ActivityEmergencyInfoEditBinding
import kotlinx.coroutines.launch

class EmergencyInfoEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmergencyInfoEditBinding
    private lateinit var repository: InfoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmergencyInfoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "EDIT EMERGENCY INFO"

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
                    "Emergency info saved",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }

        binding.buttonClearEmergencyInfo.setOnClickListener {
            lifecycleScope.launch {
                repository.clear()
                Toast.makeText(this@EmergencyInfoEditActivity, "Emergency info cleared", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}