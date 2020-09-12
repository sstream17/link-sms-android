package com.stream_suite.link.shared.service

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.util.DualSimUtils
import com.stream_suite.link.shared.util.SendUtils

class ResendFailedMessage : IntentService("ResendFailedMessage") {

    override fun onHandleIntent(intent: Intent?) {
        try {
            var messageId = intent?.getLongExtra(EXTRA_MESSAGE_ID, -1) ?: return
            if (messageId == -1L) {
                return
            }

            NotificationManagerCompat.from(this).cancel(6666 + messageId.toInt())

            val original = DataSource.getMessage(this, messageId) ?: return
            val conversation = DataSource.getConversation(this, original.conversationId)

            val m = Message()
            m.conversationId = original.conversationId
            m.type = Message.TYPE_SENDING
            m.data = original.data
            m.timestamp = original.timestamp
            m.mimeType = original.mimeType
            m.read = true
            m.seen = true
            m.from = null
            m.color = null
            m.sentDeviceId = if (Account.exists()) java.lang.Long.parseLong(Account.deviceId!!) else -1L
            m.simPhoneNumber = if (conversation!!.simSubscriptionId != null)
                DualSimUtils.getPhoneNumberFromSimSubscription(conversation.simSubscriptionId!!)
            else null

            DataSource.deleteMessage(this, messageId)
            messageId = DataSource.insertMessage(this, m, m.conversationId, true)

            SendUtils(conversation.simSubscriptionId).setForceSplitMessage(true)
                    .setRetryFailedMessages(false)
                    .send(this, m.data!!, conversation.phoneNumbers!!)
        } catch (e: Exception) {
        }
    }

    companion object {
        const val EXTRA_MESSAGE_ID = "arg_message_id"
    }
}
