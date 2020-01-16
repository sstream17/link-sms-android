package xyz.stream.messenger.shared.util.autoreply.parsers

import android.content.Context
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.AutoReply
import xyz.stream.messenger.shared.data.model.Conversation
import xyz.stream.messenger.shared.data.model.Message
import xyz.stream.messenger.shared.util.autoreply.AutoReplyParser

class DrivingReplyParser(context: Context?, reply: AutoReply) : AutoReplyParser(context, reply) {

    override fun canParse(conversation: Conversation, message: Message) = Settings.drivingMode

}
