package xyz.stream.messenger.utils

import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.shared.data.SectionType

class TextAnywhereConversationCardApplier(private val conversationList: ConversationListFragment) {

    fun shouldAddCardToList(): Boolean {
        val adapter = conversationList.adapter
        return if (adapter == null || adapter.sectionCounts.size == 0) {
            false
        } else {
            adapter.sectionCounts[0].type != SectionType.CARD_ABOUT_ONLINE &&
                    adapter.showHeaderAboutTextingOnline()
        }
    }

    fun addCardToConversationList() {
        val adapter = conversationList.adapter
        if (adapter != null) {
            adapter.sectionCounts.add(0, SectionType(SectionType.CARD_ABOUT_ONLINE, 0))
            adapter.shouldShowHeadersForEmptySections(true)
            adapter.notifyItemInserted(0)
        }
    }
}