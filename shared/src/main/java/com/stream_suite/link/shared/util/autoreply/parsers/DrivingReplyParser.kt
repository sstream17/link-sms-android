package com.stream_suite.link.shared.util.autoreply.parsers

import android.content.Context
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.AutoReply
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.autoreply.AutoReplyParser

class DrivingReplyParser(context: Context?, reply: AutoReply) : AutoReplyParser(context, reply) {

    override fun canParse(conversation: Conversation, message: Message) = Settings.drivingMode

}
