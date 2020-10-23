package com.stream_suite.link.fragment.message

import android.app.Notification
import android.net.Uri
import android.view.View
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import com.stream_suite.link.R
import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.MimeType
import com.stream_suite.link.shared.data.model.Draft
import com.stream_suite.link.shared.data.model.ScheduledMessage
import com.stream_suite.link.shared.data.pojo.ConversationUpdateInfo
import com.stream_suite.link.shared.util.TimeUtils

class DraftManager(private val fragment: MessageListFragment) {

    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val attachManager
        get() = fragment.attachManager
    private val argManager
        get() = fragment.argManager

    private val messageEntry: EditText by lazy { fragment.rootView!!.findViewById<View>(R.id.message_entry) as EditText }
    private val sendProgress: View by lazy { fragment.rootView!!.findViewById<View>(R.id.send_progress) }

    var pullDrafts = true
    private var drafts = emptyList<Draft>()

    fun applyDrafts() { if (pullDrafts) setDrafts(drafts) else pullDrafts = true }
    fun loadDrafts() {
        if (activity != null) drafts = DataSource.getDrafts(activity!!, argManager.conversationId)
        if (drafts.isNotEmpty()) {
            val activity = this.activity!!
            Thread {
                val updatedSnippet = DataSource.deleteDrafts(activity, argManager.conversationId)
                if (updatedSnippet != null) {
                    activity.runOnUiThread {
                        val fragment = activity.supportFragmentManager.findFragmentById(R.id.conversation_list_container)
                        if (fragment != null && fragment is ConversationListFragment) {
                            fragment.setConversationUpdateInfo(ConversationUpdateInfo(argManager.conversationId, updatedSnippet, true))
                        }
                    }
                }
            }.start()
        }
    }

    fun createDrafts(scheduledMessage: ScheduledMessage = ScheduledMessage()) {
        if (sendProgress.visibility != View.VISIBLE && messageEntry.text != null && messageEntry.text.isNotEmpty()) {
            if (drafts.isNotEmpty() && activity != null) {
                DataSource.deleteDrafts(activity!!, argManager.conversationId)
            }

            DataSource.insertDraft(activity, argManager.conversationId,
                    messageEntry.text.toString(), MimeType.TEXT_PLAIN, scheduledMessage = scheduledMessage)
        }

        attachManager.writeDraftOfAttachment()
    }

    private fun setDrafts(drafts: List<Draft>) {
        val notificationDraft = fragment.argManager.notificationInputDraft
        if (!notificationDraft.isNullOrBlank()) {
            messageEntry.setText(notificationDraft)

            val extra: String? = null
            fragment.activity?.intent?.putExtra(Notification.EXTRA_REMOTE_INPUT_DRAFT, extra)
        } else {
            for (draft in drafts) {
                when {
                    draft.mimeType == MimeType.TEXT_PLAIN -> {
                        messageEntry.setText(draft.data)
                        messageEntry.setSelection(messageEntry.text.length)
                        val message = ScheduledMessage()
                        message.timestamp = draft.scheduledTimestamp
                        if (message.timestamp > TimeUtils.now) {
                            message.repeat = draft.scheduledRepeat
                            fragment.showScheduledTime(message)
                            fragment.sendManager.enableMessageScheduling(message)
                        }
                    } else -> attachManager.attachMedia(Uri.parse(draft.data), draft.mimeType!!)
                }
            }
        }
    }
}