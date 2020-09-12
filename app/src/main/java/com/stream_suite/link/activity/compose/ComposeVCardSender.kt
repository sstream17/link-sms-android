package com.stream_suite.link.activity.compose

import android.net.Uri
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.util.CursorUtil
import com.stream_suite.link.shared.util.SendUtils

class ComposeVCardSender(private val activity: ComposeActivity) {

    fun send(mimeType: String, data: String) {
        val phoneNumbers = activity.contactsProvider.getPhoneNumberFromContactEntry()
        send(mimeType, data, phoneNumbers)
    }

    fun send(mimeType: String, data: String, conversationId: Long) {
        val conversation = DataSource.getConversation(activity, conversationId)

        val uri = SendUtils(conversation!!.simSubscriptionId)
                .send(activity, "", conversation.phoneNumbers!!, Uri.parse(data), mimeType)
        val cursor = DataSource.searchMessages(activity, data)

        if (cursor != null && cursor.moveToFirst()) {
            DataSource.updateMessageData(activity, cursor.getLong(0), uri!!.toString())
        }

        CursorUtil.closeSilent(cursor)
        activity.finish()
    }

    private fun send(mimeType: String, data: String, phoneNumbers: String) {
        val conversationId = DataSource.insertSentMessage(phoneNumbers, data, mimeType, activity)
        send(mimeType, data, conversationId)
    }

}