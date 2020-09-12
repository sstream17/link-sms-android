package com.stream_suite.link.adapter

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.IDragSelectAdapter

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.stream_suite.link.R
import com.stream_suite.link.adapter.view_holder.ImageViewHolder
import com.stream_suite.link.shared.data.MediaMessage
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.listener.MediaSelectedListener

/**
 * An adapter for displaying images in a grid for the user to select to attach to a message.
 */
class MediaGridAdapter(mediaMessages: List<Message>, private val callback: MediaSelectedListener?)
    : RecyclerView.Adapter<ImageViewHolder>(), IDragSelectAdapter {

    var messages = mediaMessages.map { MediaMessage(it) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_attach_image, parent, false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = Uri.parse(messages[position].message.data)

        holder.image.setOnClickListener {
            callback?.onSelected(messages, holder.adapterPosition)
        }

        holder.image.setOnLongClickListener {
            callback?.onStartDrag(position)
            true
        }

        if (messages[position].selected) {
            holder.selectedCheckmarkLayout.setBackgroundColor(Color.BLACK)
            holder.selectedCheckmarkLayout.alpha = .3f
            holder.selectedCheckmarkLayout.visibility = View.VISIBLE
        } else {
            holder.selectedCheckmarkLayout.visibility = View.GONE
        }

        holder.image.setBackgroundColor(Color.TRANSPARENT)
        Glide.with(holder.image.context)
                .load(image)
                .apply(RequestOptions().centerCrop())
                .into(holder.image)
    }

    override fun getItemCount() = messages.size
    override fun isIndexSelectable(index: Int) = true

    override fun setSelected(index: Int, selected: Boolean) {
        messages[index].selected = selected
        notifyItemChanged(index)
    }
}
