package xyz.stream.messenger.activity.share

import android.net.Uri
import xyz.stream.messenger.R
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.model.Message
import xyz.stream.messenger.shared.receiver.ConversationListUpdatedReceiver
import xyz.stream.messenger.shared.receiver.MessageListUpdatedReceiver
import xyz.stream.messenger.shared.util.DualSimUtils
import xyz.stream.messenger.shared.util.PhoneNumberUtils
import xyz.stream.messenger.shared.util.SendUtils
import xyz.stream.messenger.shared.util.TimeUtils
import xyz.stream.messenger.shared.widget.MessengerAppWidgetProvider

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