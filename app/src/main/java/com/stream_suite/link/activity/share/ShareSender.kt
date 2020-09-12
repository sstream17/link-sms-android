package com.stream_suite.link.activity.share

import android.net.Uri
import com.stream_suite.link.R
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.receiver.ConversationListUpdatedReceiver
import com.stream_suite.link.shared.receiver.MessageListUpdatedReceiver
import com.stream_suite.link.shared.util.DualSimUtils
import com.stream_suite.link.shared.util.PhoneNumberUtils
import com.stream_suite.link.shared.util.SendUtils
import com.stream_suite.link.shared.util.TimeUtils
import com.stream_suite.link.shared.widget.MessengerAppWidgetProvider

class ShareSender(private val page: QuickSharePage) {

    fun sendMessage(): Boolean {
        try {
            if (page.messageEntry.text.isEmpty() && page.mediaData == null) {
                return false
            }

            var conversationId: Long? = null

            val messageText = page.messageEntry.text.toString().trim { it <= ' ' }
            val phoneNumbers = page.contactEntry.recipients
                    .joinToString(", ") { PhoneNumberUtils.clearFormatting(it.entry.destination) }

            if (messageText.isNotEmpty()) {
                val textMessage = createMessage(messageText)
                conversationId = DataSource.insertMessage(textMessage, phoneNumbers, page.context)
            }

            if (page.mediaData != null) {
                val imageMessage = createMessage(page.mediaData!!)
                imageMessage.mimeType = page.mimeType!!
                conversationId = DataSource.insertMessage(imageMessage, phoneNumbers, page.context)
            }

            DataSource.readConversation(page.context, conversationId!!)
            val conversation = DataSource.getConversation(page.activity, conversationId)
                    ?: return false

            Thread {
                if (page.mediaData != null) {
                    SendUtils(conversation.simSubscriptionId).send(page.context, messageText, phoneNumbers, Uri.parse(page.mediaData!!), page.mimeType!!)
                } else {
                    SendUtils(conversation.simSubscriptionId).send(page.context, messageText, phoneNumbers)
                }

                //MarkAsSentJob.scheduleNextRun(page.context, messageId)
            }.start()

            ConversationListUpdatedReceiver.sendBroadcast(page.context, conversation.id, page.context.getString(R.string.you) + ": " + messageText, true)
            MessageListUpdatedReceiver.sendBroadcast(page.context, conversation.id)
            MessengerAppWidgetProvider.refreshWidget(page.context)

            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun createMessage(text: String, mimeType: String = MimeType.TEXT_PLAIN): Message {
        val message = Message()
        message.type = Message.TYPE_SENDING
        message.data = text
        message.timestamp = TimeUtils.now
        message.mimeType = mimeType
        message.read = true
        message.seen = true
        message.simPhoneNumber = if (DualSimUtils.availableSims.size > 1) DualSimUtils.defaultPhoneNumber else null
        message.sentDeviceId = if (Account.exists()) Account.deviceId!!.toLong() else -1L

        return message
    }
}