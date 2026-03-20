package com.quietlogic.allisok.ui.history

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
        val context = parent.context

        val root = LinearLayout(context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        val content = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 12))
        }

        val textContainer = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            orientation = LinearLayout.VERTICAL
        }

        val name = TextView(context).apply {
            textSize = 22f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
        }

        val subtitle = TextView(context).apply {
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        val delete = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = context.getString(R.string.care_delete_time)
            textSize = 22f
            setTextColor(Color.RED)
            setPadding(dp(context, 16), dp(context, 8), dp(context, 8), dp(context, 8))
        }

        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 1)
            )
            setBackgroundColor(Color.LTGRAY)
        }

        textContainer.addView(name)
        textContainer.addView(subtitle)

        content.addView(textContainer)
        content.addView(delete)

        root.addView(content)
        root.addView(divider)

        return VH(root, name, subtitle, delete)
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

    companion object {
        private fun dp(context: Context, value: Int): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value.toFloat(),
                context.resources.displayMetrics
            ).toInt()
        }
    }
}