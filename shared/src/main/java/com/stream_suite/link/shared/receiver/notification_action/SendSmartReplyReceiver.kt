package com.stream_suite.link.shared.receiver.notification_action

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import android.util.Log

import com.stream_suite.link.shared.R
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.api.implementation.ApiUtils
import com.stream_suite.link.api.implementation.firebase.AnalyticsHelper
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.receiver.ConversationListUpdatedReceiver
import com.stream_suite.link.shared.receiver.MessageListUpdatedReceiver
import com.stream_suite.link.shared.service.ReplyService
import com.stream_suite.link.shared.util.*
import com.stream_suite.link.shared.widget.MessengerAppWidgetProvider

class SendSmartReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reply = intent.getStringExtra(ReplyService.EXTRA_REPLY) ?: return
        val conversationId = intent.getLongExtra(ReplyService.EXTRA_CONVERSATION_ID, -1)

        if (conversationId == -1L) {
            Log.e(TAG, "could not find attached conversation id")
            return
        }

        val conversation = DataSource.getConversation(context, conversationId) ?: return

        val m = Message()
        m.conversationId = conversationId
        m.type = Message.TYPE_SENDING
        m.data = reply
        m.timestamp = TimeUtils.now
        m.mimeType = MimeType.TEXT_PLAIN
        m.read = true
        m.seen = true
        m.from = null
        m.color = null
        m.simPhoneNumber = if (conversation.simSubscriptionId != null)
            DualSimUtils
                    .getPhoneNumberFromSimSubscription(conversation.simSubscriptionId!!)
        else
            null
        m.sentDeviceId = if (Account.exists()) java.lang.Long.parseLong(Account.deviceId!!) else -1L

        DataSource.insertMessage(context, m, conversationId)
        DataSource.readConversation(context, conversationId)

        Log.v(TAG, "sending message $reply to ${conversation.phoneNumbers}")

        SendUtils(conversation.simSubscriptionId).send(context, reply, conversation.phoneNumbers!!)

        // cancel the notification we just replied to or
        // if there are no more notifications, cancel the summary as well
        val unseenMessages = DataSource.getUnseenMessages(context)
        if (unseenMessages.count <= 0) {
            try {
                NotificationUtils.cancelAll(context)
            } catch (e: SecurityException) {
            }
        } else {
            NotificationManagerCompat.from(context).cancel(conversationId.toInt())
        }

        ApiUtils.dismissNotification(Account.accountId, Account.deviceId, conversationId)

        unseenMessages.closeSilent()

        ConversationListUpdatedReceiver.sendBroadcast(context, conversationId, context.getString(R.string.you) + ": " + reply, true)
        MessageListUpdatedReceiver.sendBroadcast(context, conversationId)
        MessengerAppWidgetProvider.refreshWidget(context)

        AnalyticsHelper.sendSmartReply(context)
    }

    companion object {
        private const val TAG = "SmartReplySender"
    }
}
