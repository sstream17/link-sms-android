package xyz.stream.messenger.fragment

import android.view.View
import com.google.android.material.navigation.NavigationView

import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.adapter.view_holder.ConversationViewHolder
import xyz.stream.messenger.fragment.conversation.ConversationListFragment

class ArchivedConversationListFragment : ConversationListFragment() {

    override fun noConversationsText() = getString(R.string.no_archived_messages_description)

    // always consume the back event and send us to the conversation list
    override fun onBackPressed(): Boolean {
        if (!super.onBackPressed()) {
            activity?.title = getString(R.string.app_title)
            (activity as MessengerActivity).displayConversations()
        }

        return true
    }
}
