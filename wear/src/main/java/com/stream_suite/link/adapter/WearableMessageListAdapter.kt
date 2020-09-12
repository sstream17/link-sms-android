package com.stream_suite.link.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.stream_suite.link.R
import com.stream_suite.link.adapter.view_holder.WearableMessageViewHolder
import com.stream_suite.link.shared.data.*
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.data.pojo.BubbleTheme
import com.stream_suite.link.shared.util.*

class WearableMessageListAdapter(context: Context, private val manager: LinearLayoutManager,
                                 var messages: Cursor?, private val receivedColor: Int,
                                 private val accentColor: Int, private val isGroup: Boolean)
    : RecyclerView.Adapter<WearableMessageViewHolder>() {

    private val ignoreSendingStatus = true
    private val stylingHelper = MessageListStylingHelper(context)
    private val timestampHeight: Int by lazy { DensityUtil.spToPx(context, Settings.mediumFont + 2) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WearableMessageViewHolder {
        if (viewType == -1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_footer, parent, false)
            return WearableMessageViewHolder(view, -1, -1, -1)
        }

        val layoutId: Int
        val color: Int

        if (viewType == Message.TYPE_RECEIVED) {
            layoutId = when (Settings.bubbleTheme) {
                BubbleTheme.ROUNDED -> R.layout.message_round_received
                BubbleTheme.CIRCLE -> R.layout.message_circle_received
                BubbleTheme.SQUARE -> R.layout.message_square_received
            }
            color = receivedColor
        } else {
            color = -1
            layoutId = when (Settings.bubbleTheme) {
                BubbleTheme.ROUNDED -> when (viewType) {
                    Message.TYPE_SENDING -> R.layout.message_round_sending
                    Message.TYPE_ERROR -> R.layout.message_round_error
                    Message.TYPE_DELIVERED -> R.layout.message_round_delivered
                    Message.TYPE_IMAGE_SENDING -> R.layout.message_round_image_sending
                    Message.TYPE_IMAGE_SENT -> R.layout.message_round_image_sent
                    Message.TYPE_IMAGE_RECEIVED -> R.layout.message_round_image_received
                    Message.TYPE_INFO -> R.layout.message_info
                    Message.TYPE_MEDIA -> R.layout.message_media
                    else -> R.layout.message_round_sent
                }
                BubbleTheme.CIRCLE -> when (viewType) {
                    Message.TYPE_SENDING -> R.layout.message_circle_sending
                    Message.TYPE_ERROR -> R.layout.message_circle_error
                    Message.TYPE_DELIVERED -> R.layout.message_circle_delivered
                    Message.TYPE_IMAGE_SENDING -> R.layout.message_circle_image_sending
                    Message.TYPE_IMAGE_SENT -> R.layout.message_circle_image_sent
                    Message.TYPE_IMAGE_RECEIVED -> R.layout.message_circle_image_received
                    Message.TYPE_INFO -> R.layout.message_info
                    Message.TYPE_MEDIA -> R.layout.message_media
                    else -> R.layout.message_circle_sent
                }
                BubbleTheme.SQUARE -> when (viewType) {
                    Message.TYPE_SENDING -> R.layout.message_square_sending
                    Message.TYPE_ERROR -> R.layout.message_square_error
                    Message.TYPE_DELIVERED -> R.layout.message_square_delivered
                    Message.TYPE_IMAGE_SENDING -> R.layout.message_square_image_sending
                    Message.TYPE_IMAGE_SENT -> R.layout.message_square_image_sent
                    Message.TYPE_IMAGE_RECEIVED -> R.layout.message_square_image_received
                    Message.TYPE_INFO -> R.layout.message_info
                    Message.TYPE_MEDIA -> R.layout.message_media
                    else -> R.layout.message_square_sent
                }
            }
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        val holder = WearableMessageViewHolder(view, color, viewType, timestampHeight)
        holder.setColors(receivedColor, accentColor)

        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: WearableMessageViewHolder, position: Int) {
        if (position == itemCount - 1) {
            return
        }

        messages!!.moveToPosition(position)
        val message = Message()
        message.fillFromCursor(messages!!)

        holder.messageId = message.id
        holder.mimeType = message.mimeType
        holder.data = message.data

        if (message.mimeType == MimeType.TEXT_PLAIN) {
            holder.message?.text = message.data

            setGone(holder.image)
            setVisible(holder.message)
        } else {
            setVisible(holder.image)
            if (!MimeType.isExpandedMedia(message.mimeType)) {
                holder.image?.setImageDrawable(null)
                setGone(holder.message)
            }

            applyMedia(holder, message, position)
        }

        if (message.simPhoneNumber != null) {
            holder.timestamp?.text = TimeUtils.formatTimestamp(holder.itemView.context,
                    message.timestamp) + " (SIM " + message.simPhoneNumber + ")"
        } else {
            holder.timestamp?.text = TimeUtils.formatTimestamp(holder.itemView.context, message.timestamp)
        }

        if (holder.timestamp != null) {
            stylingHelper.calculateAdjacentItems(messages!!, position)
                    .setMargins(holder.itemView)
                    .setBackground(holder.messageHolder, message.mimeType!!)
                    .applyTimestampHeight(holder.timestamp!!, timestampHeight)
        }

        if (isGroup) {
            if (holder.contact != null && !MimeType.isExpandedMedia(message.mimeType!!)) {
                if (stylingHelper.hideContact) {
                    holder.contact!!.layoutParams.height = 0
                } else {
                    holder.contact!!.layoutParams.height = timestampHeight
                }
            }

            val label = if (holder.timestamp!!.layoutParams.height > 0)
                R.string.message_from_bullet else R.string.message_from

            if (!MimeType.isExpandedMedia(message.mimeType)) {
                holder.contact?.text = if (stylingHelper.hideContact) "" else holder.itemView.resources.getString(label, message.from)
                holder.contact?.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = try {
            messages!!.count + 1
        } catch (e: Exception) {
            0
        }

    override fun getItemViewType(position: Int): Int {
        if (messages == null || messages!!.count == 0 || position == itemCount - 1) {
            return -1
        }

        messages!!.moveToPosition(position)
        var type = messages!!.getInt(messages!!.getColumnIndex(Message.COLUMN_TYPE))
        if (ignoreSendingStatus && type == Message.TYPE_SENDING) {
            type = Message.TYPE_SENT
        }

        return type
    }

    override fun getItemId(position: Int): Long {
        if (messages == null || messages!!.count == 0) {
            return -1
        }

        messages!!.moveToPosition(position)
        return messages!!.getLong(messages!!.getColumnIndex(Message.COLUMN_ID))
    }

    fun addMessage(newMessages: Cursor?) {
        val initialCount = itemCount

        CursorUtil.closeSilent(messages)
        messages = newMessages

        if (newMessages == null) {
            return
        }

        val finalCount = itemCount

        if (initialCount == finalCount) {
            notifyItemChanged(finalCount - 1)
        } else if (initialCount > finalCount) {
            notifyDataSetChanged()
        } else {
            if (finalCount - 2 >= 0) {
                // with the new paddings, we need to notify the second to last item too
                notifyItemChanged(finalCount - 2)
            }

            notifyItemInserted(finalCount - 1)

            if (Math.abs(manager.findLastVisibleItemPosition() - initialCount) < 4) {
                // near the bottom, scroll to the new item
                manager.scrollToPosition(finalCount - 1)
            }
        }
    }

    fun setCursor(messages: Cursor?) {
        CursorUtil.closeSilent(this.messages)
        this.messages = messages
        notifyDataSetChanged()
    }

    private fun setVisible(v: View?) {
        if (v != null && v.visibility != View.VISIBLE) {
            v.visibility = View.VISIBLE
        }
    }

    private fun setGone(v: View?) {
        if (v != null && v.visibility != View.GONE) {
            v.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun applyMedia(holder: WearableMessageViewHolder, message: Message, position: Int) {
        when {
            MimeType.isStaticImage(message.mimeType) || message.mimeType == MimeType.IMAGE_GIF -> {
                holder.image?.setImageResource(if (getItemViewType(position) != Message.TYPE_RECEIVED)
                    R.drawable.ic_image_sending else R.drawable.ic_image)
            }
            MimeType.isVideo(message.mimeType!!) -> {
                val placeholder = if (getItemViewType(position) != Message.TYPE_RECEIVED) {
                    holder.itemView.context.getDrawable(R.drawable.ic_play_sent)
                } else {
                    holder.itemView.context.getDrawable(R.drawable.ic_play)
                }

                Glide.with(holder.itemView.context)
                        .asBitmap()
                        .load(Uri.parse(message.data))
                        .apply(RequestOptions()
                                .error(placeholder)
                                .placeholder(placeholder)
                                .override(holder.image!!.maxHeight, holder.image!!.maxHeight)
                                .fitCenter())
                        .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                ImageUtils.overlayBitmap(holder.itemView.context, bitmap, R.drawable.ic_play)
                                holder.image?.setImageBitmap(bitmap)
                            }
                        })
            }
            MimeType.isAudio(message.mimeType!!) -> {
                holder.image?.setImageResource(if (getItemViewType(position) != Message.TYPE_RECEIVED)
                    R.drawable.ic_audio_sent else R.drawable.ic_audio)
            }
            MimeType.isVcard(message.mimeType!!) -> {
                holder.message?.text = message.data
                holder.image?.setImageResource(if (getItemViewType(position) != Message.TYPE_RECEIVED)
                    R.drawable.ic_contacts_sent else R.drawable.ic_contacts)
            }
            message.mimeType == MimeType.MEDIA_YOUTUBE_V2 -> {
                val preview = YouTubePreview.build(message.data!!)
                if (preview != null) {
                    Glide.with(holder.itemView.context)
                            .asBitmap().load(Uri.parse(preview.thumbnail))
                            .apply(RequestOptions()
                                    .override(holder.clippedImage!!.maxHeight, holder.clippedImage!!.maxHeight)
                                    .fitCenter())
                            .into<SimpleTarget<Bitmap>>(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                                    ImageUtils.overlayBitmap(holder.itemView.context, bitmap, R.drawable.ic_play)
                                    holder.clippedImage?.setImageBitmap(bitmap)
                                }
                            })

                    holder.contact?.text = preview.title
                    holder.title?.text = "YouTube"

                    setGone(holder.image)
                    setGone(holder.message)
                    setVisible(holder.clippedImage)
                    setVisible(holder.contact)
                    setVisible(holder.title)
                } else {
                    setGone(holder.clippedImage)
                    setGone(holder.image)
                    setGone(holder.message)
                    setGone(holder.timestamp)
                    setGone(holder.title)
                }
            }
            message.mimeType == MimeType.MEDIA_TWITTER -> { }
            message.mimeType == MimeType.MEDIA_MAP -> {
                val preview = MapPreview.build(holder.data!!)
                if (preview != null) {
                    Glide.with(holder.itemView.context)
                            .load(Uri.parse(preview.generateMap()))
                            .apply(RequestOptions()
                                    .override(holder.image!!.maxHeight, holder.image!!.maxHeight)
                                    .fitCenter())
                            .into<SimpleTarget<Drawable>>(object : SimpleTarget<Drawable>() {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    holder.image?.background = holder.itemView.resources
                                            .getDrawable(R.drawable.rounded_rect)
                                    holder.image?.setImageDrawable(resource)
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    holder.image?.background = holder.itemView.resources
                                            .getDrawable(R.drawable.rounded_rect_drawer_color)
                                }
                            })
                }
            }
            message.mimeType == MimeType.MEDIA_ARTICLE -> {
                val preview = ArticlePreview.build(message.data!!)
                if (preview != null) {
                    Glide.with(holder.itemView.context)
                            .asBitmap().load(Uri.parse(message.data))
                            .apply(RequestOptions()
                                    .override(holder.image!!.maxHeight, holder.image!!.maxHeight)
                                    .fitCenter())
                            .into(holder.clippedImage!!)

                    holder.contact?.text = preview.title
                    holder.message?.text = preview.description
                    holder.title?.text = preview.domain

                    setGone(holder.image)
                    setVisible(holder.clippedImage)
                    setVisible(holder.contact)
                    setVisible(holder.message)
                    setVisible(holder.title)
                } else {
                    setGone(holder.clippedImage)
                    setGone(holder.image)
                    setGone(holder.message)
                    setGone(holder.timestamp)
                    setGone(holder.title)
                }
            }
            else -> Log.v("MessageListAdapter", "unused mime type: " + message.mimeType!!)
        }
    }
}