package com.stream_suite.link.fragment.message

import android.os.Handler
import androidx.appcompat.widget.Toolbar
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.stream_suite.link.R
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.pojo.ConversationUpdateInfo
import com.stream_suite.link.shared.util.ContactUtils
import com.stream_suite.link.shared.util.PhoneNumberUtils

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
        if (/*!argManager.title.matches(".*[a-zA-Z].*".toRegex()) && */(!Account.exists() || Account.primary)) {
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