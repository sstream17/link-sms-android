package xyz.stream.messenger.shared.receiver.notification_action

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.util.NotificationUtils
import xyz.stream.messenger.shared.util.UnreadBadger
import xyz.stream.messenger.shared.util.closeSilent
import xyz.stream.messenger.shared.widget.MessengerAppWidgetProvider

class NotificationMuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        Thread {
            val conversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1)
            DataSource.readConversation(context, conversationId)
            val conversation = DataSource.getConversation(context, conversationId)

            conversation!!.mute = true
            DataSource.updateConversationSettings(context, conversation, true)

            // cancel the notification we just replied to or
            // if there are no more notifications, cancel the summary as well
            val unseenMessages = DataSource.getUnseenMessages(context)
            if (unseenMessages.count <= 0) {
                NotificationUtils.cancelAll(context)
            } else {
                NotificationManagerCompat.from(context).cancel(conversationId.toInt())
            }

            unseenMessages.closeSilent()

            ApiUtils.dismissNotification(Account.accountId, Account.deviceId, conversationId)
            xyz.stream.messenger.shared.receiver.ConversationListUpdatedReceiver.Companion.sendBroadcast(context, conversationId, if (conversation == null) "" else conversation.snippet, true)

            UnreadBadger(context).clearCount()
            MessengerAppWidgetProvider.refreshWidget(context)
        }.start()
    }

    companion object {
        const val EXTRA_CONVERSATION_ID = "conversation_id"
    }
}
