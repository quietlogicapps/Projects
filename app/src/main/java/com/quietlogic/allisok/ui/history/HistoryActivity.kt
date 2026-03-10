package com.quietlogic.allisok.ui.history

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.db.AppDatabase
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.CareRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: CareRepository
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        title = "History"

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

        lifecycleScope.launch {
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
}