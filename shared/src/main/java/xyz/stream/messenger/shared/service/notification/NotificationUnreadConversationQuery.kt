package xyz.stream.messenger.shared.service.notification

import android.content.Context
import android.graphics.Color
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Message
import xyz.stream.messenger.shared.data.pojo.NotificationConversation
import xyz.stream.messenger.shared.data.pojo.NotificationMessage
import xyz.stream.messenger.shared.util.MockableDataSourceWrapper
import xyz.stream.messenger.shared.util.closeSilent
import java.lang.Exception
import java.util.*

class NotificationUnreadConversationQuery(private val context: Context) {

    fun getUnseenConversations(source: MockableDataSourceWrapper): List<NotificationConversation> {
        // timestamps are ASC, so it will start with the oldest message, and move to the newest.
        val unseenMessages = source.getUnseenMessages(context)
        val conversations = mutableListOf<NotificationConversation>()
        val keys = mutableListOf<Long>()

        if (unseenMessages.moveToFirst()) {
            do {
                val conversationId = unseenMessages.getLong(unseenMessages.getColumnIndex(Message.COLUMN_CONVERSATION_ID))
                val id = unseenMessages.getLong(unseenMessages.getColumnIndex(Message.COLUMN_ID))
                val data = unseenMessages.getString(unseenMessages.getColumnIndex(Message.COLUMN_DATA))
                val mimeType = unseenMessages.getString(unseenMessages.getColumnIndex(Message.COLUMN_MIME_TYPE))
                val timestamp = unseenMessages.getLong(unseenMessages.getColumnIndex(Message.COLUMN_TIMESTAMP))
                val from = unseenMessages.getString(unseenMessages.getColumnIndex(Message.COLUMN_FROM))

                if (!MimeType.isExpandedMedia(mimeType)) {
                    val conversationIndex = keys.indexOf(conversationId)
                    var conversation: NotificationConversation? = null

                    if (conversationIndex == -1) {
                        val c = source.getConversation(context, conversationId)
                        if (c != null) {
                            conversation = NotificationConversation()
                            conversation.id = c.id
                            conversation.unseenMessageId = id
                            conversation.title = c.title
                            conversation.snippet = c.snippet
                            conversation.imageUri = c.imageUri
                            conversation.color = c.colors.color
                            conversation.ringtoneUri = c.ringtoneUri
                            conversation.ledColor = c.ledColor
                            conversation.timestamp = c.timestamp
                            conversation.mute = c.mute
                            conversation.phoneNumbers = c.phoneNumbers
                            conversation.groupConversation = c.phoneNumbers!!.contains(",")
                            try {
                                conversation.realMessages = DataSource.getMessages(context, conversation.id, 4)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (c.private) {
                                conversation.title = context.getString(R.string.new_message)
                                conversation.imageUri = null
                                conversation.ringtoneUri = null
                                conversation.color = Settings.mainColorSet.color
                                conversation.privateNotification = true
                                conversation.ledColor = Color.WHITE
                            } else {
                                conversation.privateNotification = false
                            }

                            conversations.add(conversation)
                            keys.add(conversationId)
                        }
                    } else {
                        conversation = conversations[conversationIndex]
                    }

                    conversation?.messages?.add(NotificationMessage(id, data, mimeType, timestamp, from))
                }
            } while (unseenMessages.moveToNext())
        }

        unseenMessages.closeSilent()
        conversations.sortWith(Comparator { result1, result2 -> Date(result2.timestamp).compareTo(Date(result1.timestamp)) })
        return conversations
    }
}