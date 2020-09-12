package com.stream_suite.link.utils.swipe_to_dismiss.actions

import com.stream_suite.link.adapter.conversation.ConversationListAdapter

abstract class BaseSwipeAction {

    abstract fun getBackgroundColor(): Int
    abstract fun getIcon(): Int
    abstract fun onPerform(listener: ConversationListAdapter, index: Int)

}