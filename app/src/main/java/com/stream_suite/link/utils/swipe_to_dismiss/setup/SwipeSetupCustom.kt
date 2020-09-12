package com.stream_suite.link.utils.swipe_to_dismiss.setup

import com.stream_suite.link.adapter.conversation.ConversationListAdapter
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.pojo.SwipeOption
import com.stream_suite.link.utils.swipe_to_dismiss.actions.BaseSwipeAction
import com.stream_suite.link.utils.swipe_to_dismiss.actions.SwipeArchiveAction
import com.stream_suite.link.utils.swipe_to_dismiss.actions.SwipeDeleteAction
import com.stream_suite.link.utils.swipe_to_dismiss.actions.SwipeNoAction

class SwipeSetupCustom(adapter: ConversationListAdapter) : SwipeSetupBase(adapter) {

    override fun getLeftToRightAction() = mapToAction(Settings.leftToRightSwipe)
    override fun getRightToLeftAction() = mapToAction(Settings.rightToLeftSwipe)

    private fun mapToAction(option: SwipeOption): BaseSwipeAction {
        return when (option) {
            SwipeOption.ARCHIVE -> SwipeArchiveAction()
            SwipeOption.DELETE -> SwipeDeleteAction()
            SwipeOption.NONE -> SwipeNoAction()
        }
    }

}