package com.stream_suite.link.activity.notification

import androidx.core.app.NotificationManagerCompat
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.api.implementation.ApiUtils
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.service.ReplyService
import com.stream_suite.link.shared.util.CursorUtil

class ReplyDataProvider(private val activity: MarshmallowReplyActivity) {

    val conversationId: Long by lazy { activity.intent.getLongExtra(ReplyService.EXTRA_CONVERSATION_ID, -1L) }
    val conversation: Conversation? by lazy { DataSource.getConversation(activity, conversationId) }
    val messages = mutableListOf<Message>()

    fun queryMessageHistory() {
        DataSource.seenConversation(activity, conversationId)

        val cursor = DataSource.getMessages(activity, conversationId)

        if (cursor.moveToLast()) {
            do {
                val message = Message()
                message.fillFromCursor(cursor)

                if (!MimeType.isExpandedMedia(message.mimeType)) {
                    messages.add(message)
                }
            } while (cursor.moveToPrevious() && messages.size < PREV_MESSAGES_TOTAL)
        }

        CursorUtil.closeSilent(cursor)
    }

    fun dismissNotification() {
        NotificationManagerCompat.from(activity).cancel(conversationId.toInt())
        ApiUtils.dismissNotification(Account.accountId,
                Account.deviceId, conversationId)
    }

    companion object {
        val PREV_MESSAGES_TOTAL = 10
        val PREV_MESSAGES_DISPLAYED = 3
    }
}