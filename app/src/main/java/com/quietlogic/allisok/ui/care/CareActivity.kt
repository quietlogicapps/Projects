package com.quietlogic.allisok.ui.care

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R
import com.quietlogic.allisok.ui.care.adapter.CareAdapter

class CareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_care)
        title = "CARE"

        val recycler = findViewById<RecyclerView>(R.id.recyclerCare)
        val empty = findViewById<TextView>(R.id.textEmpty)

        val adapter = CareAdapter()

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val demo = listOf(
            CareAdapter.Row("Medicine A", "08:00"),
            CareAdapter.Row("Medicine B", "12:00, 20:00")
        )

        adapter.submitList(demo)

        empty.visibility = if (demo.isEmpty()) View.VISIBLE else View.GONE
    }
}