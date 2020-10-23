package com.stream_suite.link.adapter.message

import android.content.res.ColorStateList
import com.stream_suite.link.R
import com.stream_suite.link.adapter.view_holder.MessageViewHolder
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Contact
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.ColorUtils

@Suppress("DEPRECATION")
class MessageColorHelper {

    private val fromColorMapper = mutableMapOf<String, Contact>()
    private val fromColorMapperByName = mutableMapOf<String, Contact>()

    fun setMappers(from: Map<String, Contact>, fromByName: Map<String, Contact>) {
        fromColorMapper.clear()
        fromColorMapper.putAll(from)

        fromColorMapperByName.clear()
        fromColorMapperByName.putAll(fromByName)
    }

    fun getColor(holder: MessageViewHolder?, message: Message?): Int {
        if (Settings.useGlobalThemeColor || holder == null || message == null || message.from == null) {
            return Integer.MIN_VALUE
        }

        try {
            if (message.type == Message.TYPE_RECEIVED && (fromColorMapper.isNotEmpty() || fromColorMapperByName.isNotEmpty())) {
                when {
                    fromColorMapper.containsKey(message.from!!) -> {
                        val color = fromColorMapper[message.from!!]!!.colors.color
                        holder.messageHolder?.backgroundTintList = ColorStateList.valueOf(color)
                        holder.color = color

                        if (!ColorUtils.isColorDark(color)) {
                            holder.message?.setTextColor(holder.itemView.context.resources.getColor(R.color.darkText))
                        } else {
                            holder.message?.setTextColor(holder.itemView.context.resources.getColor(R.color.lightText))
                        }

                        return color
                    }
                    fromColorMapperByName.containsKey(message.from!!) -> {
                        val color = fromColorMapperByName[message.from!!]!!.colors.color
                        holder.messageHolder?.backgroundTintList = ColorStateList.valueOf(color)
                        holder.color = color

                        if (!ColorUtils.isColorDark(color)) {
                            holder.message?.setTextColor(holder.itemView.context.resources.getColor(R.color.darkText))
                        } else {
                            holder.message?.setTextColor(holder.itemView.context.resources.getColor(R.color.lightText))
                        }

                        return color
                    }
                    else -> {
                        val contact = Contact()
                        contact.name = message.from
                        contact.phoneNumber = message.from
                        contact.colors = ColorUtils.getRandomMaterialColor(holder.itemView.context)

                        fromColorMapper[message.from!!] = contact
                        fromColorMapperByName[message.from!!] = contact

                        // then write it to the database for later
                        Thread {
                            val context = holder.itemView.context
                            val source = DataSource

                            if (contact.phoneNumber != null) {
//                                val originalLength = contact.phoneNumber!!.length
//                                val newLength = contact.phoneNumber!!.replace("[0-9]".toRegex(), "").length
//                                if (originalLength == newLength) {
//                                    // all letters, so we should use the contact name to find the phone number
//                                    val contacts = source.getContactsByNames(context, contact.name)
//                                    if (contacts.isNotEmpty()) {
//                                        contact.phoneNumber = contacts[0].phoneNumber
//                                    }
//                                }

                                source.insertContact(context, contact)
                            }
                        }.start()

                        val color = contact.colors.color
                        holder.messageHolder?.backgroundTintList = ColorStateList.valueOf(color)
                        holder.color = color

                        if (!ColorUtils.isColorDark(color)) {
                            holder.message?.setTextColor(holder.itemView.context.resources.getColor(R.color.darkText))
                        } else {
                            holder.message?.setTextColor(holder.itemView.context.resources.getColor(R.color.lightText))
                        }

                        return contact.colors.color
                    }
                }
            }
        } catch (e: Exception) {

        }

        return Integer.MIN_VALUE
    }
}