package com.stream_suite.link.adapter.search

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.stream_suite.link.adapter.view_holder.ConversationViewHolder
import com.stream_suite.link.adapter.view_holder.MessageViewHolder
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.data.pojo.BubbleTheme
import com.stream_suite.link.shared.util.ContactUtils
import com.stream_suite.link.shared.util.DensityUtil
import com.stream_suite.link.shared.util.TimeUtils
import com.stream_suite.link.shared.util.listener.SearchListener

class SearchListItemBinder(private val listener: SearchListener) {

    fun bindConversation(holder: ConversationViewHolder, conversation: Conversation) {
        if (conversation.imageUri == null) {
            holder.image?.setImageDrawable(ColorDrawable(conversation.colors.color))

            if (ContactUtils.shouldDisplayContactLetter(conversation)) {
                holder.imageLetter?.text = conversation.title!!.substring(0, 1)
                holder.groupIcon?.visibility = View.GONE
            } else {
                holder.groupIcon?.visibility = View.VISIBLE
                holder.imageLetter?.text = null
            }
        } else {
            holder.groupIcon?.visibility = View.GONE
            holder.imageLetter?.text = null
            Glide.with(holder.itemView.context)
                    .load(Uri.parse(conversation.imageUri))
                    .into(holder.image!!)
        }

        holder.itemView.setOnClickListener { listener.onSearchSelected(conversation) }
        holder.name?.setOnClickListener { listener.onSearchSelected(conversation) }
    }

    @SuppressLint("SetTextI18n")
    fun bindMessage(holder: MessageViewHolder, message: Message) {

        val timestamp = TimeUtils.formatTimestamp(holder.itemView.context, message.timestamp)
        if (message.from != null && !message.from!!.isEmpty()) {
            holder.timestamp.text = timestamp + " - " + message.from + " (" + message.nullableConvoTitle + ")"
        } else {
            holder.timestamp.text = timestamp + " - " + message.nullableConvoTitle
        }

        holder.timestamp.setSingleLine(true)
        if (holder.timestamp.visibility != View.VISIBLE) {
            holder.timestamp.visibility = View.VISIBLE
        }

        holder.messageHolder?.setOnClickListener { listener.onSearchSelected(message) }
        holder.message?.setOnClickListener { listener.onSearchSelected(message) }

        val topMargin = DensityUtil.toDp(holder.itemView.context, when (Settings.bubbleTheme) {
            BubbleTheme.ROUNDED -> 1
            BubbleTheme.SQUARE -> 3
            BubbleTheme.CIRCLE -> 3
        })

        (holder.message!!.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 0
        (holder.message!!.layoutParams as ViewGroup.MarginLayoutParams).topMargin = -1 * topMargin
        (holder.image!!.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = 0
        (holder.image!!.layoutParams as ViewGroup.MarginLayoutParams).topMargin = -1 * topMargin
    }
}