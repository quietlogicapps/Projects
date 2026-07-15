package com.quietlogic.allisok.ui.history

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
        recycler.clipToPadding = true

        val bottomAirPx = (8 * resources.displayMetrics.density).toInt()
        fun applyHistoryBottomInset(insets: WindowInsetsCompat) {
            val navigationBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val bottomInset = navigationBottom + bottomAirPx
            val params = recycler.layoutParams as ConstraintLayout.LayoutParams
            if (params.bottomMargin != bottomInset) {
                params.bottomMargin = bottomInset
                recycler.layoutParams = params
            }
            if (recycler.paddingBottom != 0) {
                recycler.updatePadding(bottom = 0)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            applyHistoryBottomInset(insets)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(recycler) { _, insets ->
            applyHistoryBottomInset(insets)
            insets
        }
        ViewCompat.requestApplyInsets(window.decorView)

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