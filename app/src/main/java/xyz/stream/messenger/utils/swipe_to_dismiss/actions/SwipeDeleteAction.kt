package xyz.stream.messenger.utils.swipe_to_dismiss.actions

import xyz.stream.messenger.R
import xyz.stream.messenger.adapter.conversation.ConversationListAdapter
import xyz.stream.messenger.shared.data.Settings

class SwipeDeleteAction : BaseSwipeAction() {

    override fun getIcon() = R.drawable.ic_delete_sweep
    override fun getBackgroundColor() = Settings.mainColorSet.colorAccent
    override fun onPerform(listener: ConversationListAdapter, index: Int) {
        listener.deleteItem(index)
    }

}