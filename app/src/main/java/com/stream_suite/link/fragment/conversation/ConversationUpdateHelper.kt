package com.stream_suite.link.fragment.conversation

import androidx.fragment.app.FragmentActivity
import com.stream_suite.link.R
import com.stream_suite.link.shared.MessengerActivityExtras
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.data.pojo.ConversationUpdateInfo
import com.stream_suite.link.shared.receiver.ConversationListUpdatedReceiver
import java.lang.IllegalStateException

class ConversationUpdateHelper(private val fragment: ConversationListFragment) {

    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val updatedReceiver: ConversationListUpdatedReceiver = ConversationListUpdatedReceiver(fragment)

    var newConversationTitle: String? = null
    var updateInfo: ConversationUpdateInfo? = null

    fun notifyOfSentMessage(m: Message?) {
        if (m == null) {
            return
        }

        fragment.messageListManager.expandedConversation?.conversation?.timestamp = m.timestamp
        fragment.messageListManager.expandedConversation?.conversation?.read = m.read

        val snippet = if (m.mimeType != null && m.mimeType == MimeType.TEXT_PLAIN) {
            fragment.messageListManager.expandedConversation?.conversation?.snippet = m.data
            fragment.messageListManager.expandedConversation?.summary?.text = m.data
            m.data
        } else if (fragment.activity != null) {
            val text = MimeType.getTextDescription(fragment.activity!!, m.mimeType)
            fragment.messageListManager.expandedConversation?.conversation?.snippet = text
            fragment.messageListManager.expandedConversation?.summary?.text = text

            text
        } else {
            ""
        }

        if (fragment.messageListManager.expandedConversation != null && fragment.messageListManager.expandedConversation!!.conversation != null) {
            updateInfo = try {
                ConversationUpdateInfo(
                        fragment.messageListManager.expandedConversation!!.conversation!!.id,
                        fragment.getString(R.string.you) + ": " + snippet, true)
            } catch (e: IllegalStateException) {
                ConversationUpdateInfo(
                        fragment.messageListManager.expandedConversation!!.conversation!!.id, "" + snippet, true)
            }
        }
    }

    fun createReceiver() { activity?.registerReceiver(updatedReceiver, ConversationListUpdatedReceiver.intentFilter) }
    fun destroyReceiver() { activity?.unregisterReceiver(updatedReceiver) }

    fun broadcastUpdateInfo() {
        if (updateInfo != null) {
            ConversationListUpdatedReceiver.sendBroadcast(activity, updateInfo!!)
            updateInfo = null
        }
    }

    fun broadcastTitleChange(contractedId: Long) {
        if (newConversationTitle != null && activity != null) {
            Settings.setValue(activity!!, MessengerActivityExtras.EXTRA_SHOULD_REFRESH_LIST, true)
            newConversationTitle = null
        }
    }
}