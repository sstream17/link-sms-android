package com.stream_suite.link.shared.service.notification.conversation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.stream_suite.link.shared.R
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.pojo.NotificationConversation
import com.stream_suite.link.shared.receiver.notification_action.NotificationMarkReadReceiver
import com.stream_suite.link.shared.service.ReplyService

class NotificationCarHelper(private val service: Context) {

    fun buildExtender(conversation: NotificationConversation, remoteInput: RemoteInput): NotificationCompat.CarExtender {
        val carReply = Intent().addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction("com.stream_suite.link.CAR_REPLY")
                .putExtra(ReplyService.EXTRA_CONVERSATION_ID, conversation.id)
                .setPackage("com.stream_suite.link")
        val pendingCarReply = PendingIntent.getBroadcast(service, conversation.id.toInt(),
                carReply, PendingIntent.FLAG_UPDATE_CURRENT)

        val carRead = Intent().addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction("com.stream_suite.link.CAR_READ")
                .putExtra(NotificationMarkReadReceiver.EXTRA_CONVERSATION_ID, conversation.id)
                .setPackage("com.stream_suite.link")
        val pendingCarRead = PendingIntent.getBroadcast(service, conversation.id.toInt(),
                carRead, PendingIntent.FLAG_UPDATE_CURRENT)

        // Android Auto extender
        val car = NotificationCompat.CarExtender.UnreadConversation.Builder(if (conversation.privateNotification) service.getString(R.string.new_message) else conversation.title)
                .setReadPendingIntent(pendingCarRead)
                .setReplyAction(pendingCarReply, remoteInput)
                .setLatestTimestamp(conversation.timestamp)

        if (!conversation.privateNotification) {
            for ((_, data, mimeType) in conversation.messages) {
                if (mimeType == MimeType.TEXT_PLAIN) {
                    car.addMessage(data)
                } else {
                    car.addMessage(service.getString(R.string.new_mms_message))
                }
            }
        }

        return NotificationCompat.CarExtender().setUnreadConversation(car.build())
    }
}