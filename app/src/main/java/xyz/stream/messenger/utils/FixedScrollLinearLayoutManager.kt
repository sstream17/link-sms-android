package xyz.stream.messenger.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FixedScrollLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {

    private var canScroll = true
    private val childSizesMap = mutableMapOf<Int, Int>()

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != null) {
                childSizesMap[getPosition(child)] = child.height
            }
        }
    }

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }

        val firstChildPosition = findFirstVisibleItemPosition()
        val firstChild = findViewByPosition(firstChildPosition)
        var scrolledY: Int = -(firstChild?.y?.toInt() ?: 0)
        for (i in 0 until firstChildPosition) {
            scrolledY += childSizesMap[i] ?: 0
        }

        return scrolledY
    }

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
