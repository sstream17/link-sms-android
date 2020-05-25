package xyz.stream.messenger.shared.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.view.emoji.EmojiableEditText
import kotlin.math.max

class PersistentSearchView : MaterialCardView, CoordinatorLayout.AttachedBehavior {

    private var behavior: Behavior? = null

    private var currentOffset = 0
    private var totalScrollRange = INVALID_SCROLL_RANGE
    private var downPreScrollRange = INVALID_SCROLL_RANGE
    private var downScrollRange = INVALID_SCROLL_RANGE

    var isSearchOpen: Boolean = false

    val backButtonLayout: FrameLayout by lazy { findViewById<View>(R.id.search_back_holder) as FrameLayout }
    val backButton: ImageView by lazy { findViewById<View>(R.id.search_back_button) as ImageView }
    val text: EmojiableEditText by lazy { findViewById<View>(R.id.search_text) as EmojiableEditText }
    val accountPictureLayout: FrameLayout by lazy { findViewById<View>(R.id.account_image_holder) as FrameLayout }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        behavior = Behavior(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        behavior = Behavior(context, attrs)
    }

    constructor(context: Context) : super(context) {
        behavior = Behavior()
    }

    fun swapLeftRightPadding() {
        text.setPadding(text.paddingRight, text.paddingTop, text.paddingLeft, text.paddingBottom)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return behavior!!
    }

    fun invalidateScrollRanges() {
        currentOffset = 0
        totalScrollRange = INVALID_SCROLL_RANGE
        downPreScrollRange = INVALID_SCROLL_RANGE
        downScrollRange = INVALID_SCROLL_RANGE
    }

    fun setTopBottomOffset(newOffset: Int, minOffset: Int, maxOffset: Int): Int {
        var newOffset = newOffset
        val curOffset: Int = currentOffset
        var consumed = 0
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset)
            if (curOffset != newOffset) {
                currentOffset = newOffset
                consumed = curOffset - newOffset
            }
        }
        return consumed
    }

    fun getDownNestedScrollRange(): Int {
        if (downScrollRange != INVALID_SCROLL_RANGE) {
            return downScrollRange
        }
        var range = 0
        var i = 0
        val z = childCount
        while (i < z) {
            val child = getChildAt(i)
            if (child.isVisible) {
                val lp = child.layoutParams as LinearLayout.LayoutParams
                var childHeight = child.measuredHeight
                childHeight += lp.topMargin + lp.bottomMargin
                range += childHeight
            }
            i++
        }
        return max(0, range).also { downScrollRange = it }
    }

    fun getDownNestedPreScrollRange(): Int {
        if (downPreScrollRange != INVALID_SCROLL_RANGE) {
            return downPreScrollRange
        }
        var range = 0
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            val childHeight: Int = child.measuredHeight
            if (range <= 0) {
                val child = getChildAt(i)
                if (child.isVisible) {
                    val lp = child.layoutParams as LinearLayout.LayoutParams
                    var childRange = lp.topMargin + lp.bottomMargin
                    childRange += childHeight
                    range += childRange
                }
            } else {
                break
            }
        }
        return max(0, range).also { downPreScrollRange = it }
    }

    fun getTotalScrollRange(): Int {
        if (isSearchOpen) return 0

        if (totalScrollRange != INVALID_SCROLL_RANGE) {
            return totalScrollRange
        }
        var range = 0
        var i = 0
        val z = childCount
        while (i < z) {
            val child = getChildAt(i)
            if (child.isVisible) {
                val lp = child.layoutParams as LinearLayout.LayoutParams
                val childHeight = child.measuredHeight
                range += childHeight + lp.topMargin + lp.bottomMargin
            }
            i++
        }
        return max(0, range).also { totalScrollRange = it }
    }

    class Behavior : CoordinatorLayout.Behavior<PersistentSearchView> {

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        constructor() : super()

        override fun layoutDependsOn(parent: CoordinatorLayout, child: PersistentSearchView, dependency: View): Boolean {
            return dependency is FragmentContainerView
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: PersistentSearchView, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                    super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: PersistentSearchView, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
            if (target is RecyclerView) {
                if (!target.canScrollVertically(dy)) {
                    return
                }
            }

            if (dy != 0) {
                val min: Int
                val max: Int
                if (dy < 0) { // We're scrolling down
                    min = -child.getTotalScrollRange()
                    max = min + child.getDownNestedPreScrollRange()
                } else { // We're scrolling up
                    min = -child.getTotalScrollRange()
                    max = 0
                }
                if (min != max) {
                    scroll(child, dy, min, max)
                }
            }
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: PersistentSearchView, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                consumed[1] = scroll(child, dyUnconsumed, -child.getDownNestedScrollRange(), 0)
            }
        }

        private fun scroll(header: PersistentSearchView, dy: Int, minOffset: Int, maxOffset: Int): Int {
            val consumed = header.setTopBottomOffset(
                    header.currentOffset - dy,
                    minOffset,
                    maxOffset)

            ViewCompat.offsetTopAndBottom(header, -consumed)
            return consumed
        }
    }

    companion object {
        private const val INVALID_SCROLL_RANGE = -1
    }
}