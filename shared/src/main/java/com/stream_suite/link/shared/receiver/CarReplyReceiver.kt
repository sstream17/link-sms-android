package com.stream_suite.link.shared.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import android.util.Log

import com.stream_suite.link.shared.R
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.api.implementation.ApiUtils
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.service.ReplyService
import com.stream_suite.link.shared.util.*

class CarReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        var reply: String? = null
        if (remoteInput != null) {
            reply = remoteInput.getCharSequence(ReplyService.EXTRA_REPLY)!!.toString()
        }

        if (reply == null) {
            Log.e(TAG, "could not find attached reply")
            return
        }

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
            DualSimUtils.getPhoneNumberFromSimSubscription(conversation.simSubscriptionId!!)
        else
            null
        m.sentDeviceId = if (Account.exists()) java.lang.Long.parseLong(Account.deviceId!!) else -1L

        DataSource.insertMessage(context, m, conversationId)
        DataSource.readConversation(context, conversationId)

        SendUtils(conversation.simSubscriptionId).send(context, reply, conversation.phoneNumbers!!)

        // cancel the notification we just replied to or
        // if there are no more notifications, cancel the summary as well
        val unseenMessages = DataSource.getUnseenMessages(context)
        if (unseenMessages.count <= 0) {
            NotificationUtils.cancelAll(context)
        } else {
            NotificationManagerCompat.from(context).cancel(conversationId.toInt())
        }

        ApiUtils.dismissNotification(Account.accountId,
                Account.deviceId,
                conversationId)

        unseenMessages.closeSilent()

        ConversationListUpdatedReceiver.sendBroadcast(context, conversationId, context.getString(R.string.you) + ": " + reply, true)
        MessageListUpdatedReceiver.sendBroadcast(context, conversationId)
    }

    companion object {
        private val TAG = "CarReplyReceiver"
    }
}
