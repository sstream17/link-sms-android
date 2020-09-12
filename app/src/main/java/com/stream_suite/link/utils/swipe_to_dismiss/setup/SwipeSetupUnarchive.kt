package com.stream_suite.link.utils.swipe_to_dismiss.setup

import com.stream_suite.link.adapter.conversation.ConversationListAdapter
import com.stream_suite.link.utils.swipe_to_dismiss.actions.BaseSwipeAction
import com.stream_suite.link.utils.swipe_to_dismiss.actions.SwipeDeleteAction
import com.stream_suite.link.utils.swipe_to_dismiss.actions.SwipeUnarchiveAction

@Suppress("DEPRECATION")
class SwipeSetupUnarchive(adapter: ConversationListAdapter) : SwipeSetupBase(adapter) {

    override fun getLeftToRightAction(): BaseSwipeAction {
        return SwipeUnarchiveAction()
    }

    override fun getRightToLeftAction(): BaseSwipeAction {
        return SwipeDeleteAction()
    }

}
