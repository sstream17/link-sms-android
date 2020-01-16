package xyz.stream.messenger.shared.receiver.notification_action

import android.content.Context
import android.content.Intent
import xyz.stream.messenger.shared.MessengerActivityExtras
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.Settings

open class NotificationArchiveReceiver : xyz.stream.messenger.shared.receiver.notification_action.NotificationMarkReadReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        // mark as read functionality

        if (context == null || intent == null) {
            return
        }

        Thread {
            val conversationId = intent.getLongExtra(xyz.stream.messenger.shared.receiver.notification_action.NotificationMarkReadReceiver.EXTRA_CONVERSATION_ID, -1L)
            if (conversationId != -1L) {
                DataSource.archiveConversation(context, conversationId)
                Settings.setValue(context, MessengerActivityExtras.EXTRA_SHOULD_REFRESH_LIST, true)
            }
        }.start()

    }

}
