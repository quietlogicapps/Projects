package com.quietlogic.allisok.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.data.local.entity.CareLogEntity
import com.quietlogic.allisok.databinding.ItemRecentTakenBinding

class RecentTakenAdapter : RecyclerView.Adapter<RecentTakenAdapter.ViewHolder>() {

    private val items = mutableListOf<CareLogEntity>()

    fun submitList(list: List<CareLogEntity>) {

        items.clear()

        items.addAll(list)

        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemRecentTakenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CareLogEntity) {

            binding.textDate.text = item.date.toString()

            binding.textTime.text = item.scheduledTime.toString()

            binding.textCareName.text = item.careItemId.toString()

            binding.textStatus.text = "Taken"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        val binding = ItemRecentTakenBinding.inflate(
            inflater,
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = items[position]

        holder.bind(item)
    }

    override fun getItemCount(): Int {

        return items.size
    }
}