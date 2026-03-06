package com.quietlogic.allisok.ui.info

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.quietlogic.allisok.data.local.db.DatabaseProvider
import com.quietlogic.allisok.data.repository.CareLogRepository
import com.quietlogic.allisok.databinding.ActivityInfoBinding
import kotlinx.coroutines.launch

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding
    private lateinit var adapter: RecentTakenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "INFO"

        adapter = RecentTakenAdapter()

        binding.recyclerRecentTaken.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentTaken.adapter = adapter

        val database = DatabaseProvider.getDatabase(this)
        val repository = CareLogRepository(database.careLogDao())

        lifecycleScope.launch {
            repository.getRecentLast72Hours().collect { list ->
                adapter.submitList(list)
            }
        }
    }
}