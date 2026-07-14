package com.quietlogic.allisok.ui.history

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.CareRepository
import com.quietlogic.allisok.data.repository.SettingsRepository
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: CareRepository
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        title = getString(R.string.history_title)

        db = DatabaseProvider.getDatabase(applicationContext)

        repository = CareRepository(
            context = applicationContext,
            careItemDao = db.careItemDao(),
            careTimeDao = db.careTimeDao()
        )

        val recycler = findViewById<RecyclerView>(R.id.historyRecycler)
        val empty = findViewById<TextView>(R.id.textHistoryEmpty)

        adapter = HistoryAdapter { itemId ->
            lifecycleScope.launch {
                val item = db.careItemDao().getAllArchived().first().firstOrNull { it.id == itemId }
                if (item != null) {
                    repository.deleteCareItem(item)
                }
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val bottomAirPx = (8 * resources.displayMetrics.density).toInt()
        fun applyRecyclerBottomInset(insets: WindowInsetsCompat) {
            val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            recycler.updatePadding(
                left = recycler.paddingLeft,
                top = recycler.paddingTop,
                right = recycler.paddingRight,
                bottom = navigationBottom + bottomAirPx
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.historyRecycler)) { _, insets ->
            applyRecyclerBottomInset(insets)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            applyRecyclerBottomInset(insets)
            insets
        }
        ViewCompat.requestApplyInsets(findViewById(android.R.id.content))

        lifecycleScope.launch {
            val settings = SettingsRepository(db.appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            adapter.dateFormatter = DateTimeFormatter.ofPattern(pattern)

            repository.getAllArchivedCareItems().collect { items ->
                adapter.submitList(items)

                if (items.isEmpty()) {
                    empty.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    empty.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            kotlinx.coroutines.delay(500)
            val settings = SettingsRepository(db.appSettingsDao()).getSettings().first()
            val pattern = if (settings?.dateFormat == "US") "MM/dd/yyyy" else "dd/MM/yyyy"
            adapter.dateFormatter = DateTimeFormatter.ofPattern(pattern)
            adapter.notifyDataSetChanged()
        }
    }
}