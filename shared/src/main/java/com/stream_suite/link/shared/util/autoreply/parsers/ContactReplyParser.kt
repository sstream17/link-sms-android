package com.stream_suite.link.shared.util.autoreply.parsers

import android.content.Context
import com.stream_suite.link.shared.data.model.AutoReply
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.SmsMmsUtils
import com.stream_suite.link.shared.util.autoreply.AutoReplyParser

class ContactReplyParser(context: Context?, reply: AutoReply) : AutoReplyParser(context, reply) {

    override fun canParse(conversation: Conversation, message: Message): Boolean {
        if (reply.pattern == null) {
            return false
        }

        val autoReplyIdMatcher = SmsMmsUtils.createIdMatcher(reply.pattern!!)
        val receivedMessageMatcher = SmsMmsUtils.createIdMatcher(conversation.phoneNumbers!!)

        return autoReplyIdMatcher.default == receivedMessageMatcher.default
    }

}
