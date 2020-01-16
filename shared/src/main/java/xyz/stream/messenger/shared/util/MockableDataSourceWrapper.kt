package xyz.stream.messenger.shared.util

import android.content.Context
import android.database.Cursor

import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.model.Conversation
import xyz.stream.messenger.shared.data.model.Message

class MockableDataSourceWrapper(private val source: DataSource) {

    fun getMessages(context: Context, conversationId: Long, numberOfMessages: Int): List<Message> {
        return source.getMessages(context, conversationId, numberOfMessages)
    }

    fun getUnseenMessages(context: Context): Cursor {
        return source.getUnseenMessages(context)
    }

    fun getConversation(context: Context, conversationId: Long): Conversation? {
        return source.getConversation(context, conversationId)
    }
}
