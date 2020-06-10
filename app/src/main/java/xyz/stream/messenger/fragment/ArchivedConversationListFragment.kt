package xyz.stream.messenger.fragment

import androidx.navigation.fragment.findNavController
import xyz.stream.messenger.R
import xyz.stream.messenger.fragment.conversation.ConversationListFragment

class ArchivedConversationListFragment : ConversationListFragment() {

    override fun noConversationsText() = getString(R.string.no_archived_messages_description)

    // always consume the back event and send us to the conversation list
    override fun onBackPressed(): Boolean {
        if (!super.onBackPressed()) {
            findNavController().setGraph(R.navigation.navigation_conversations)
        }

        return true
    }
}
