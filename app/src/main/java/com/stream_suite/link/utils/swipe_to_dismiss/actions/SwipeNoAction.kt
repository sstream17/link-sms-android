package com.stream_suite.link.utils.swipe_to_dismiss.actions

import android.graphics.Color
import com.stream_suite.link.R
import com.stream_suite.link.adapter.conversation.ConversationListAdapter

class SwipeNoAction : BaseSwipeAction() {

    override fun getIcon() = R.drawable.ic_back
    override fun getBackgroundColor() = Color.TRANSPARENT
    override fun onPerform(listener: ConversationListAdapter, index: Int) { }

}