package com.stream_suite.link.shared.util.autoreply

import android.content.Context
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.AutoReply
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message

abstract class AutoReplyParser(protected var context: Context?, protected val reply: AutoReply) {

    abstract fun canParse(conversation: Conversation, message: Message): Boolean

    fun parse(forMessage: Message): Message? {
        val message = Message()
        message.conversationId = forMessage.conversationId
        message.timestamp = forMessage.timestamp + 1
        message.type = Message.TYPE_SENDING
        message.read = false
        message.seen = false
        message.mimeType = MimeType.TEXT_PLAIN
        message.data = reply.response
        message.sentDeviceId =  if (Account.exists()) Account.deviceId!!.toLong() else -1L

        return if (message.data == null) null else message
    }
}
