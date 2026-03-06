package com.quietlogic.allisok.ui.care

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
import com.quietlogic.allisok.ui.care.adapter.CareAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CareActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: CareRepository

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_care)
        title = "CARE"

        db = DatabaseProvider.getDatabase(applicationContext)

        repository = CareRepository(
            context = applicationContext,
            careItemDao = db.careItemDao(),
            careTimeDao = db.careTimeDao()
        )

        val recycler = findViewById<RecyclerView>(R.id.recyclerCare)
        val empty = findViewById<TextView>(R.id.textEmpty)
        val btnAdd = findViewById<Button>(R.id.btnAddCare)

        val adapter = CareAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, CareEditActivity::class.java))
        }

        lifecycleScope.launch {

            repository.archiveExpiredItems()

            db.careItemDao().getAllActive().collect { items ->

                val finalRows = mutableListOf<CareAdapter.Row>()

                for (item in items) {

                    val times = db.careTimeDao()
                        .getByItemId(item.id)
                        .first()

                    val timesText = if (times.isEmpty()) {
                        "—"
                    } else {
                        times
                            .map { it.time.format(timeFormatter) }
                            .sorted()
                            .joinToString(", ")
                    }

                    val dateRange =
                        "${item.startDate.format(dateFormatter)} → ${item.endDate.format(dateFormatter)}"

                    val repeatText = when {
                        item.repeatType == "DAILY" -> "Daily"
                        item.repeatType.startsWith("DAYS:") -> item.repeatType.removePrefix("DAYS:").replace(",", " ")
                        else -> item.repeatType
                    }

                    val line =
                        "$timesText • ${item.instruction} • $repeatText • $dateRange"

                    finalRows.add(
                        CareAdapter.Row(
                            item.name,
                            line
                        )
                    )
                }

                adapter.submitList(finalRows)
                empty.visibility = if (finalRows.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}