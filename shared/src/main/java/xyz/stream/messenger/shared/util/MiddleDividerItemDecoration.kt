package xyz.stream.messenger.shared.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class MiddleDividerItemDecoration(context: Context, orientation: Int) : DividerItemDecoration(context, orientation) {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == state.itemCount - 1)
            return

        super.getItemOffsets(outRect, view, parent, state)
    }
}