package com.stream_suite.link.utils

import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.shared.data.SectionType

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