package xyz.stream.messenger.activity.compose

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.shared.MessengerActivityExtras
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Message
import xyz.stream.messenger.shared.util.TimeUtils

class ComposeSendHelper(private val activity: ComposeActivity) {

    internal val fab: FloatingActionButton by lazy { activity.findViewById<View>(R.id.fab) as FloatingActionButton }

    fun setupViews() {
        fab.backgroundTintList = ColorStateList.valueOf(Settings.mainColorSet.colorAccent)
        fab.setOnClickListener {
            dismissKeyboard()

            Handler().postDelayed({
                if (activity.contactsProvider.hasContacts()) {
                    showConversation()
                }
            }, 100)
        }
    }

    internal fun resetViews(data: ShareData, isvCard: Boolean = false) {
        resetViews(listOf(data), isvCard)
    }

    internal fun resetViews(data: List<ShareData>, isvCard: Boolean = false) {
        fab.setOnClickListener {
            if (activity.contactsProvider.getRecipients().isNotEmpty() && isvCard && data.isNotEmpty()) {
                activity.vCardSender.send(data[0].mimeType, data[0].data)
            } else if (activity.contactsProvider.hasContacts()) {
                activity.shareHandler.apply(data)
            }
        }
    }

    internal fun resetViewsForMultipleImages(data: List<ShareData>) {
        fab.setOnClickListener { activity.shareHandler.apply(data) }
    }

    private fun dismissKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(fab.windowToken, 0)
    }

    private fun showConversation() {
        val phoneNumbers = activity.contactsProvider.getPhoneNumberFromContactEntry()
        showConversation(phoneNumbers)
    }

    internal fun showConversation(phoneNumbers: String, data: String? = null) {
        val phoneNumbers = phoneNumbers.replace(";", ", ")
        var conversationId = DataSource.findConversationId(activity, phoneNumbers)

        // we only want to match on phone number, not by name. This was probably silly?
//        if (conversationId == null && activity.contactsProvider.getRecipients().size == 1) {
//            conversationId = DataSource.findConversationIdByTitle(activity,
//                    activity.contactsProvider.getRecipients()[0].entry.displayName)
//        }

        if (conversationId == null) {
            val message = Message()
            message.type = Message.TYPE_INFO
            message.data = activity.getString(R.string.no_messages_with_contact)
            message.timestamp = TimeUtils.now
            message.mimeType = MimeType.TEXT_PLAIN
            message.read = true
            message.seen = true
            message.sentDeviceId = -1

            conversationId = DataSource.insertMessage(message, phoneNumbers, activity)
        } else {
            DataSource.unarchiveConversation(activity, conversationId)
        }

        if (data != null) {
            DataSource.insertDraft(activity, conversationId, data, MimeType.TEXT_PLAIN)
        }

        val conversation = DataSource.getConversation(activity, conversationId)

        val open = Intent(activity, MessengerActivity::class.java)

        if (conversation?.private == true) {
            Toast.makeText(activity, R.string.private_conversation_disclaimer, Toast.LENGTH_LONG).show()
        } else {
            open.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, conversationId)
            open.putExtra(MessengerActivityExtras.EXTRA_SHOULD_OPEN_KEYBOARD, true)
        }

        open.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        if (activity.contactsProvider.getRecipients().size == 1) {
            val name = activity.contactsProvider.getRecipients()[0].entry.displayName
            open.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_NAME, name)
        }

        activity.startActivity(open)
        activity.finish()
    }

    internal fun showConversation(conversationId: Long) {
        val open = Intent(activity, MessengerActivity::class.java)
        open.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, conversationId)
        open.putExtra(MessengerActivityExtras.EXTRA_SHOULD_OPEN_KEYBOARD, true)
        open.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val conversation = DataSource.getConversation(activity, conversationId)
        if (conversation != null && conversation.archive) {
            DataSource.unarchiveConversation(activity, conversationId)
        }

        activity.startActivity(open)
        activity.finish()
    }
}