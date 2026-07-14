package com.quietlogic.allisok.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R
import com.quietlogic.allisok.data.local.entity.CareItemEntity

class HistoryAdapter(
    private val onDeleteClick: (Long) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val items = mutableListOf<CareItemEntity>()
    var dateFormatter: java.time.format.DateTimeFormatter? = null

    fun submitList(newItems: List<CareItemEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return VH(
            itemView = view,
            name = view.findViewById(R.id.textHistoryName),
            subtitle = view.findViewById(R.id.textHistoryDate),
            delete = view.findViewById(R.id.textDelete)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.subtitle.text = dateFormatter?.format(item.endDate) ?: item.endDate.toString()

        holder.delete.setOnClickListener {
            onDeleteClick(item.id)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        val name: TextView,
        val subtitle: TextView,
        val delete: TextView
    ) : RecyclerView.ViewHolder(itemView)
}
