package com.stream_suite.link.utils.swipe_to_dismiss.actions

import android.graphics.Color
import com.stream_suite.link.R
import com.stream_suite.link.adapter.conversation.ConversationListAdapter
import com.stream_suite.link.shared.data.Settings

open class SwipeArchiveAction : BaseSwipeAction() {

    override fun getIcon() = R.drawable.ic_archive

    override fun getBackgroundColor(): Int {
        val set = Settings.mainColorSet

        return if (set.colorLight == Color.WHITE) {
            set.colorDark
        } else {
            set.colorLight
        }
    }

    override fun onPerform(listener: ConversationListAdapter, index: Int) {
        listener.archiveItem(index)
    }

}