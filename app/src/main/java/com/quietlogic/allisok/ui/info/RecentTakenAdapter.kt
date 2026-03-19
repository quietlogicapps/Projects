package com.quietlogic.allisok.ui.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quietlogic.allisok.data.local.entity.RecentTakenItem
import com.quietlogic.allisok.databinding.ItemRecentTakenBinding

class RecentTakenAdapter : RecyclerView.Adapter<RecentTakenAdapter.ViewHolder>() {

    private val items = mutableListOf<RecentTakenItem>()

    fun submitList(list: List<RecentTakenItem>) {

        items.clear()

        items.addAll(list)

        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemRecentTakenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecentTakenItem) {

            binding.textDate.text = item.date

            // комбинираме TIME + NAME на един ред
            binding.textTime.text = "${item.scheduledTime}  ${item.careItemName}"

            // махаме отделния ред за име
            binding.textCareName.text = ""

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