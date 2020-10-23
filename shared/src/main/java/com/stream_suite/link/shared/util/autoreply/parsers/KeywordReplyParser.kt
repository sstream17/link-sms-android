package com.stream_suite.link.shared.util.autoreply.parsers

import android.content.Context
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.AutoReply
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.autoreply.AutoReplyParser

class KeywordReplyParser(context: Context?, reply: AutoReply) : AutoReplyParser(context, reply) {

    override fun canParse(conversation: Conversation, message: Message): Boolean {
        if (reply.pattern == null || message.mimeType != MimeType.TEXT_PLAIN) {
            return false
        }

        return message.data!!.toLowerCase().contains(reply.pattern!!.toLowerCase())
    }

}