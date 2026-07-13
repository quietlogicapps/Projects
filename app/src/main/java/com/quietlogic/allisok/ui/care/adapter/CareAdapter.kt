package com.quietlogic.allisok.ui.care.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.R

class CareAdapter(
    private val onDeleteClick: (Long) -> Unit,
    private val onEditClick: (Long) -> Unit
) : RecyclerView.Adapter<CareAdapter.VH>() {

    data class Row(
        val id: Long,
        val name: String,
        val subtitle: String
    )

    private val items = mutableListOf<Row>()

    private var adminMode: Boolean = false

    fun submitList(rows: List<Row>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    fun setAdminMode(enabled: Boolean) {
        adminMode = enabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_care, parent, false)
        return VH(
            itemView = view,
            name = view.findViewById(R.id.textName),
            subtitle = view.findViewById(R.id.textSubtitle),
            edit = view.findViewById(R.id.iconEdit),
            delete = view.findViewById(R.id.textDelete)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.subtitle.text = item.subtitle

        if (adminMode) {
            holder.delete.visibility = View.VISIBLE
            holder.delete.setOnClickListener {
                onDeleteClick(item.id)
            }
            holder.edit.visibility = View.VISIBLE
            holder.edit.setOnClickListener {
                onEditClick(item.id)
            }
        } else {
            holder.delete.visibility = View.GONE
            holder.delete.setOnClickListener(null)
            holder.edit.visibility = View.GONE
            holder.edit.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        val name: TextView,
        val subtitle: TextView,
        val edit: ImageView,
        val delete: TextView
    ) : RecyclerView.ViewHolder(itemView)
}
