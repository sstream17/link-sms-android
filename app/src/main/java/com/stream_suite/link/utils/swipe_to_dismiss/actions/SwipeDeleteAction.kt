package com.stream_suite.link.utils.swipe_to_dismiss.actions

import com.stream_suite.link.R
import com.stream_suite.link.adapter.conversation.ConversationListAdapter
import com.stream_suite.link.shared.data.Settings

class SwipeDeleteAction : BaseSwipeAction() {

    override fun getIcon() = R.drawable.ic_delete_sweep
    override fun getBackgroundColor() = Settings.mainColorSet.colorAccent
    override fun onPerform(listener: ConversationListAdapter, index: Int) {
        listener.deleteItem(index)
    }

}