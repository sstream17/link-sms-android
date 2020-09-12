package com.stream_suite.link.adapter.message

import com.stream_suite.link.adapter.view_holder.MessageViewHolder
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.Regex

class MessageEmojiEnlarger {

    fun enlarge(holder: MessageViewHolder, message: Message) {
        if (isNotEmpty(message) && onlyHasEmojis(message)) {
            holder.message!!.textSize = 35f
        } else {
            holder.message?.textSize = Settings.largeFont.toFloat()
        }
    }

    private fun isNotEmpty(message: Message) = message.data!!.isNotEmpty()
    private fun onlyHasEmojis(message: Message) = message.data!!.replace(Regex.EMOJI.toRegex(), "").isEmpty()
}