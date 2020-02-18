package xyz.stream.messenger.adapter.view_holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import xyz.stream.messenger.R

class AttachContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val picture: ImageView = itemView.findViewById<View>(R.id.picture) as ImageView
    val name: TextView = itemView.findViewById<View>(R.id.name) as TextView
}