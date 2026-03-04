package com.quietlogic.allisok.ui.care.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R

class CareAdapter : RecyclerView.Adapter<CareAdapter.VH>() {

    data class Row(
        val name: String,
        val subtitle: String
    )

    private val items = mutableListOf<Row>()

    fun submitList(rows: List<Row>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_care, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.subtitle.text = item.subtitle
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textName)
        val subtitle: TextView = itemView.findViewById(R.id.textSubtitle)
    }
}