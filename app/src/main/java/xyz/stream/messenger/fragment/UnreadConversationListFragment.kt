package xyz.stream.messenger.fragment

import androidx.navigation.fragment.findNavController
import xyz.stream.messenger.R
import xyz.stream.messenger.fragment.conversation.ConversationListFragment

class UnreadConversationListFragment : ConversationListFragment() {

    // always consume the back event and send us to the conversation list
    override fun onBackPressed(): Boolean {
        if (!super.onBackPressed()) {
            findNavController().setGraph(R.navigation.navigation_conversations)
        }

        return true
    }
}
