package com.stream_suite.link.shared.util

import android.content.Context
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.service.message_parser.AutoReplyParserService
import com.stream_suite.link.shared.service.message_parser.MediaParserService
import com.stream_suite.link.shared.service.message_parser.VcardParserService
import com.stream_suite.link.shared.util.media.parsers.ArticleParser

@Suppress("RedundantIf")
class MessageInsertionMetadataHelper(private val context: Context) {

    private fun shouldProcessOnThisDevice(): Boolean {
        return (!Account.exists() || Account.primary) && !WearableCheck.isAndroidWear(context)
    }

    fun process(message: Message) {
        if (!shouldProcessOnThisDevice()) {
            return
        }

        val conversation = try {
            DataSource.getConversation(context, message.conversationId)
        } catch (e: Exception) {
            null
        }

        if (conversation != null) {
            process(message, conversation)
        }
    }

    private fun process(message: Message, conversation: Conversation) {
        if (message.mimeType == MimeType.TEXT_PLAIN && canProcessMedia(message)) {
            MediaParserService.start(context, message)
        }

        if (message.type == Message.TYPE_RECEIVED && canProcessAutoReply(message, conversation)) {
            AutoReplyParserService.start(context, message)
        }

        if (MimeType.isVcard(message.mimeType!!) && canProcessVcard(message)) {
            VcardParserService.start(context, message)
        }
    }

    private fun canProcessMedia(message: Message): Boolean {
        val parser = MediaParserService.createParser(context, message)
        return if (parser == null || (!Settings.internalBrowser && parser is ArticleParser)) {
            false
        } else true
    }

    private fun canProcessAutoReply(message: Message, conversation: Conversation) =
            AutoReplyParserService.createParsers(context, conversation, message).isNotEmpty()

    private fun canProcessVcard(message: Message) =
            VcardParserService.createParsers(context, message).isNotEmpty()

}