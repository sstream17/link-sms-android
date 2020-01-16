package xyz.stream.messenger.utils.swipe_to_dismiss.actions

import android.graphics.Color
import xyz.stream.messenger.R
import xyz.stream.messenger.adapter.conversation.ConversationListAdapter

class SwipeNoAction : BaseSwipeAction() {

    override fun getIcon() = R.drawable.ic_back
    override fun getBackgroundColor() = Color.TRANSPARENT
    override fun onPerform(listener: ConversationListAdapter, index: Int) { }

}