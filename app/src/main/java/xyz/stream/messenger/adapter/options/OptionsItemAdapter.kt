package xyz.stream.messenger.adapter.options

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import xyz.stream.messenger.R
import xyz.stream.messenger.shared.data.pojo.OptionsItem

class OptionsItemAdapter(private val items: List<OptionsItem>) : RecyclerView.Adapter<OptionsItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val color = getColor(holder.itemView.context, R.color.secondaryText)
        holder.image.setImageResource(item.image)
        holder.image.imageTintList = ColorStateList.valueOf(color)
        holder.text.setText(item.label)
        holder.text.setTextColor(color)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text) as TextView
        val image: ImageView = itemView.findViewById(R.id.image) as ImageView
    }
}