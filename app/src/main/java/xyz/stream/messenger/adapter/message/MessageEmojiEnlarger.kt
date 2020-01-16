package xyz.stream.messenger.adapter.message

import xyz.stream.messenger.adapter.view_holder.MessageViewHolder
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Message
import xyz.stream.messenger.shared.util.Regex

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