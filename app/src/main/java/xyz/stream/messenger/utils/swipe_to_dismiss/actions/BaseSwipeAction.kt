package xyz.stream.messenger.utils.swipe_to_dismiss.actions

import xyz.stream.messenger.adapter.conversation.ConversationListAdapter

abstract class BaseSwipeAction {

    abstract fun getBackgroundColor(): Int
    abstract fun getIcon(): Int
    abstract fun onPerform(listener: ConversationListAdapter, index: Int)

}