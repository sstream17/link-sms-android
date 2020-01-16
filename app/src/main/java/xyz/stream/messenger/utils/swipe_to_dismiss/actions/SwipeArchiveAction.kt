package xyz.stream.messenger.utils.swipe_to_dismiss.actions

import android.graphics.Color
import xyz.stream.messenger.R
import xyz.stream.messenger.adapter.conversation.ConversationListAdapter
import xyz.stream.messenger.shared.data.Settings

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