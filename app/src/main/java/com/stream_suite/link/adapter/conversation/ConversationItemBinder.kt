package com.stream_suite.link.adapter.conversation

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.stream_suite.link.R
import com.stream_suite.link.adapter.view_holder.ConversationViewHolder
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.util.ColorUtils
import com.stream_suite.link.shared.util.TimeUtils

@Suppress("DEPRECATION")
class ConversationItemBinder(private val activity: Activity) {

    private val lightToolbarTextColor: Int by lazy { activity.resources.getColor(R.color.lightToolbarTextColor) }

    fun showText(holder: ConversationViewHolder, conversation: Conversation) {
        holder.name?.text = conversation.title

        if (conversation.snippet == null || conversation.snippet!!.contains("file://") || conversation.snippet!!.contains("content://")) {
            holder.summary!!.text = ""
        } else {
            holder.summary!!.text = conversation.snippet
        }
    }

    // read not muted
    // not read, not muted
    // muted not read
    // read and muted
    fun showTextStyle(holder: ConversationViewHolder, conversation: Conversation) {
        if (conversation.read && conversation.mute) {
            holder.setTypeface(false, true)
        } else if (conversation.mute && !conversation.read) {
            holder.setTypeface(true, true)
        } else if (!conversation.mute && conversation.read) {
            holder.setTypeface(false, false)
        } else if (!conversation.mute && !conversation.read) {
            holder.setTypeface(true, false)
        }
    }

    fun indicatePinned(holder: ConversationViewHolder, conversation: Conversation) {
        if (!Settings.showConversationCategories && conversation.pinned) {
            holder.pinnedIcon?.visibility = View.VISIBLE
        } else if (holder.pinnedIcon?.visibility != View.GONE) {
            holder.pinnedIcon?.visibility = View.GONE
        }
    }

    fun showDate(holder: ConversationViewHolder, conversation: Conversation) {
        if (Settings.showConversationCategories) {
            return
        }

        holder.date?.text = TimeUtils.formatConversationTimestamp(holder.itemView.context, conversation.timestamp)
    }

    fun showImage(holder: ConversationViewHolder, conversation: Conversation) {
        Glide.with(holder.itemView.context)
                .load(Uri.parse(conversation.imageUri))
                .into(holder.image!!)
    }

    fun showImageColor(holder: ConversationViewHolder, conversation: Conversation) {
        if (Settings.useGlobalThemeColor) {
            if (Settings.mainColorSet.colorLight == Color.WHITE) {
                holder.color?.setImageDrawable(ColorDrawable(Settings.mainColorSet.colorDark))
            } else {
                holder.color?.setImageDrawable(ColorDrawable(Settings.mainColorSet.colorLight))
            }
        } else if (conversation.colors.color == Color.WHITE) {
            holder.color?.setImageDrawable(ColorDrawable(conversation.colors.colorDark))
        } else {
            holder.color?.setImageDrawable(ColorDrawable(conversation.colors.color))
        }
    }

    fun showContactLetter(holder: ConversationViewHolder, conversation: Conversation) {
        holder.imageLetter?.text = conversation.title?.substring(0, 1)?.toUpperCase()

        val colorToInspect = if (Settings.useGlobalThemeColor) Settings.mainColorSet.color else conversation.colors.color
        holder.imageLetter?.setTextColor(if (ColorUtils.isColorDark(colorToInspect)) Color.WHITE else lightToolbarTextColor)

        if (holder.groupIcon?.visibility != View.GONE) {
            holder.groupIcon?.visibility = View.GONE
        }
    }

    fun showContactPlaceholderIcon(holder: ConversationViewHolder, conversation: Conversation) {
        holder.imageLetter?.text = null

        val colorToInspect = if (Settings.useGlobalThemeColor) Settings.mainColorSet.color else conversation.colors.color
        holder.groupIcon?.imageTintList = ColorStateList.valueOf(if (ColorUtils.isColorDark(colorToInspect)) Color.WHITE else lightToolbarTextColor)
        holder.groupIcon?.setImageResource(if (conversation.isGroup) R.drawable.ic_group else R.drawable.ic_person)

        if (holder.groupIcon?.visibility != View.VISIBLE) {
            holder.groupIcon?.visibility = View.VISIBLE
        }
    }

//    fun nullItem(holder: ConversationViewHolder) {
//        holder.conversation = null
//        holder.name?.text = null
//        holder.summary?.text = null
//        holder.imageLetter?.text = null
//        Glide.with(holder.itemView.context).clear(holder.image)
//    }
}
