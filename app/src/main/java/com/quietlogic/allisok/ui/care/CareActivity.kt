package com.quietlogic.allisok.ui.care

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.CareRepository
import com.quietlogic.allisok.data.repository.SettingsRepository
import com.quietlogic.allisok.security.AdminGate
import com.quietlogic.allisok.security.AdminSession
import com.quietlogic.allisok.security.LockGate
import com.quietlogic.allisok.ui.care.adapter.CareAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CareActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: CareRepository
    private lateinit var adapter: CareAdapter

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onStart() {
        super.onStart()
        LockGate.requireUserUnlock(this)
    }

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
        setContentView(R.layout.activity_care)
        title = getString(R.string.home_care)

        updateAdminIndicator()

        db = DatabaseProvider.getDatabase(applicationContext)

        repository = CareRepository(
            context = applicationContext,
            careItemDao = db.careItemDao(),
            careTimeDao = db.careTimeDao()
        )

        val recycler = findViewById<RecyclerView>(R.id.recyclerCare)
        val empty = findViewById<TextView>(R.id.textEmpty)
        val btnAdd = findViewById<Button>(R.id.btnAddCare)

        adapter = CareAdapter { itemId ->
            if (!AdminSession.isActive()) return@CareAdapter

            lifecycleScope.launch {
                val item = db.careItemDao().getAllActive().first().firstOrNull { it.id == itemId }
                if (item != null) {
                    repository.deleteCareItem(item)
                }
            }
        }

        adapter.setAdminMode(AdminSession.isActive())

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnAdd.setOnClickListener {
            if (AdminSession.isActive()) {
                startActivity(Intent(this, CareEditActivity::class.java))
            } else {
                AdminGate.requireAdmin(this) {
                    startActivity(Intent(this, CareEditActivity::class.java))
                }
            }
        }

        lifecycleScope.launch {
            val settings = SettingsRepository(db.appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            dateFormatter = DateTimeFormatter.ofPattern(pattern)

            repository.archiveExpiredItems()

            db.careItemDao().getAllActive().collect { items ->

                val finalRows = mutableListOf<CareAdapter.Row>()

                for (item in items) {
                    val times = db.careTimeDao()
                        .getByItemId(item.id)
                        .first()

                    val timesText = if (times.isEmpty()) {
                        getString(R.string.care_times_dash)
                    } else {
                        times
                            .map { it.time.format(timeFormatter) }
                            .sorted()
                            .joinToString(", ")
                    }

                    val currentDateFormatter = dateFormatter
                    val dateRange =
                        "${item.startDate.format(currentDateFormatter)} → ${item.endDate.format(currentDateFormatter)}"

                    val repeatText = when {
                        item.repeatType == "DAILY" -> getString(R.string.care_repeat_daily)
                        item.repeatType.startsWith("DAYS:") -> item.repeatType.removePrefix("DAYS:")
                            .split(",")
                            .joinToString(", ") { mapDayCodeToLabel(it) }

                        else -> item.repeatType
                    }

                    val instructionText = when(item.instruction) {
                        "None" -> getString(R.string.care_instruction_none)
                        "Before food" -> getString(R.string.care_instruction_before_food)
                        "After food" -> getString(R.string.care_instruction_after_food)
                        else -> item.instruction
                    }

                    val subtitle = buildSubtitle(
                        dateRange = dateRange,
                        timesText = timesText,
                        instruction = instructionText,
                        repeatText = repeatText
                    )

                    finalRows.add(
                        CareAdapter.Row(
                            id = item.id,
                            name = item.name,
                            subtitle = subtitle
                        )
                    )
                }

                adapter.submitList(finalRows)
                empty.visibility = if (finalRows.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAdminIndicator()
        adapter.setAdminMode(AdminSession.isActive())

        lifecycleScope.launch {
            kotlinx.coroutines.delay(500)

            val settings = SettingsRepository(db.appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            dateFormatter = DateTimeFormatter.ofPattern(pattern)

            repository.archiveExpiredItems()

            val items = db.careItemDao().getAllActive().first()
            val finalRows = mutableListOf<CareAdapter.Row>()

            for (item in items) {
                val times = db.careTimeDao().getByItemId(item.id).first()

                val timesText = if (times.isEmpty()) {
                    getString(R.string.care_times_dash)
                } else {
                    times.map { it.time.format(timeFormatter) }.sorted().joinToString(", ")
                }

                val dateRange = "${item.startDate.format(dateFormatter)} → ${item.endDate.format(dateFormatter)}"

                val repeatText = when {
                    item.repeatType == "DAILY" -> getString(R.string.care_repeat_daily)
                    item.repeatType.startsWith("DAYS:") -> item.repeatType.removePrefix("DAYS:")
                        .split(",")
                        .joinToString(", ") { mapDayCodeToLabel(it) }

                    else -> item.repeatType
                }

                val instructionText = when(item.instruction) {
                    "None" -> getString(R.string.care_instruction_none)
                    "Before food" -> getString(R.string.care_instruction_before_food)
                    "After food" -> getString(R.string.care_instruction_after_food)
                    else -> item.instruction
                }

                val subtitle = buildSubtitle(
                    dateRange = dateRange,
                    timesText = timesText,
                    instruction = instructionText,
                    repeatText = repeatText
                )

                finalRows.add(
                    CareAdapter.Row(
                        id = item.id,
                        name = item.name,
                        subtitle = subtitle
                    )
                )
            }

            val empty = findViewById<TextView>(R.id.textEmpty)
            adapter.submitList(finalRows)
            empty.visibility = if (finalRows.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun buildSubtitle(
        dateRange: String,
        timesText: String,
        instruction: String,
        repeatText: String
    ): String {
        val lines = mutableListOf<String>()

        lines.add(dateRange)
        lines.add(timesText)

        if (instruction != getString(R.string.care_instruction_none)) {
            lines.add(instruction)
        }

        lines.add(repeatText)

        return lines.joinToString("\n")
    }

    private fun mapDayCodeToLabel(code: String): String {
        return when (code.trim()) {
            "MON" -> getString(R.string.care_day_mon)
            "TUE" -> getString(R.string.care_day_tue)
            "WED" -> getString(R.string.care_day_wed)
            "THU" -> getString(R.string.care_day_thu)
            "FRI" -> getString(R.string.care_day_fri)
            "SAT" -> getString(R.string.care_day_sat)
            "SUN" -> getString(R.string.care_day_sun)
            else -> code
        }
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