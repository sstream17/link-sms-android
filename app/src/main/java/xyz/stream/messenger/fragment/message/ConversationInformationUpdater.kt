package xyz.stream.messenger.fragment.message

import android.os.Handler
import androidx.appcompat.widget.Toolbar
import android.view.View
import androidx.fragment.app.FragmentActivity
import xyz.stream.messenger.R
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.pojo.ConversationUpdateInfo
import xyz.stream.messenger.shared.util.ContactUtils
import xyz.stream.messenger.shared.util.PhoneNumberUtils

class ConversationInformationUpdater(private val fragment: MessageListFragment) {

    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val argManager
        get() = fragment.argManager

    private val handler = Handler()
    private val toolbar: Toolbar by lazy { fragment.rootView!!.findViewById<View>(R.id.toolbar) as Toolbar }

    fun update() {
        if (activity == null || argManager.isGroup) {
            return
        }

        val number = argManager.phoneNumbers

        // if the title has a letter in it, don't update it
        // the user can update conversation titles, from settings
        if (!argManager.title.matches(".*[a-zA-Z].*".toRegex()) && (!Account.exists() || Account.primary)) {
            val name = ContactUtils.findContactNames(number, activity)
            if (name != argManager.title && !PhoneNumberUtils.checkEquality(name, number)) {
                DataSource.updateConversationTitle(activity!!, argManager.conversationId, name)

                val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.conversation_list_container) as ConversationListFragment?
                fragment?.setNewConversationTitle(name)

                handler.post { toolbar.title = name }
            }
        }

        if (argManager.imageUri.isNullOrEmpty()) {
            val photoUri = ContactUtils.findImageUri(number, activity)
            if (!photoUri.isNullOrEmpty()) {
                DataSource.updateConversationImage(activity!!, argManager.conversationId, photoUri)
            }
        }
    }

    fun setConversationUpdateInfo(newMessage: String) {
        val fragment = activity?.supportFragmentManager?.findFragmentById(R.id.conversation_list_container)
        if (fragment != null && fragment is ConversationListFragment) {
            fragment.setConversationUpdateInfo(ConversationUpdateInfo(argManager.conversationId, newMessage, true))
        }
    }
}