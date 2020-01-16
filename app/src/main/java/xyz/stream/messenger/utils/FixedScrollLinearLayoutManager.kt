package xyz.stream.messenger.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FixedScrollLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {

    private var canScroll = true

    fun setCanScroll(canScroll: Boolean) {
        this.canScroll = canScroll
    }

    override fun canScrollVertically(): Boolean {
        return canScroll && super.canScrollVertically()
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
        }
    }
}
