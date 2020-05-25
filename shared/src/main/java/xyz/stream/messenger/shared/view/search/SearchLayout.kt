package xyz.stream.messenger.shared.view.search

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import xyz.stream.messenger.shared.R
import kotlin.math.max

class SearchLayout : LinearLayout, CoordinatorLayout.AttachedBehavior {

    private var behavior: Behavior? = null

    private var totalScrollRange = INVALID_SCROLL_RANGE
    private var downPreScrollRange = INVALID_SCROLL_RANGE
    private var downScrollRange = INVALID_SCROLL_RANGE

    private val searchView: PersistentSearchView by lazy { findViewById<View>(R.id.search_view) as PersistentSearchView }
    private val searchContainer: LinearLayout by lazy { findViewById<View>(R.id.search_container) as LinearLayout }

    private var _isSearchOpen: Boolean = false
    private var pastText: String = ""
    private var onQueryTextListener: OnQueryTextListener? = null
    private var searchViewListener: SearchViewListener? = null

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        behavior = Behavior(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        behavior = Behavior(context, attrs)
    }

    constructor(context: Context) : super(context) {
        behavior = Behavior()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initSearchText()
        initBackButton()
    }

    private fun initSearchText() {
        searchView.text.setOnFocusChangeListener { _, focused ->
            if (focused) {
                openSearch()
            }
        }

        searchView.text.setOnEditorActionListener { _, _, _ ->
            onSubmitQuery()
            true
        }

        searchView.text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    onTextChanged(s)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun initBackButton() {
        searchView.backButton.setOnClickListener {
            closeSearch()
        }
    }

    fun setOnQueryTextListener(listener: OnQueryTextListener) {
        onQueryTextListener = listener
    }

    fun setSearchViewListener(listener: SearchViewListener) {
        searchViewListener = listener
    }

    val isSearchOpen: Boolean
        get() = _isSearchOpen

    fun openSearch() {
        if (isSearchOpen) return

        setBackgroundColor(getColor(context, R.color.background))
        searchView.backButtonLayout.visibility = View.VISIBLE
        searchContainer.visibility = View.VISIBLE
        searchView.accountPictureLayout.visibility = View.GONE
        searchView.swapLeftRightPadding()
        searchViewListener?.onSearchOpened()
        _isSearchOpen = true
    }

    fun closeSearch() {
        if (!isSearchOpen) return

        setBackgroundColor(getColor(context, android.R.color.transparent))
        searchView.backButtonLayout.visibility = View.GONE
        searchContainer.visibility = View.GONE
        searchView.accountPictureLayout.visibility = View.VISIBLE
        searchView.swapLeftRightPadding()
        searchView.text.clearFocus()
        searchView.text.text = null
        searchViewListener?.onSearchClosed()
        _isSearchOpen = false

        invalidateScrollRanges()
    }

    fun invalidateScrollRanges() {
        searchView.currentOffset = 0
        totalScrollRange = INVALID_SCROLL_RANGE
        downPreScrollRange = INVALID_SCROLL_RANGE
        downScrollRange = INVALID_SCROLL_RANGE
    }

    private fun onTextChanged(newText: CharSequence) {
        if (onQueryTextListener != null && !TextUtils.equals(newText, pastText)) {
            onQueryTextListener!!.onQueryTextChange(newText.toString())
        }
        pastText = newText.toString()
    }

    private fun onSubmitQuery() {
        val query = searchView.text.text
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (onQueryTextListener == null || onQueryTextListener?.onQueryTextSubmit(query.toString()) == false) {
                closeSearch()
            }
        }
    }

    interface OnQueryTextListener {
        fun onQueryTextSubmit(query: String): Boolean
        fun onQueryTextChange(newText: String): Boolean
    }

    interface SearchViewListener {
        fun onSearchOpened()
        fun onSearchClosed()
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return behavior!!
    }

    internal fun setTopBottomOffset(newOffset: Int, minOffset: Int, maxOffset: Int): Int {
        var newOffset = newOffset
        val curOffset: Int = searchView.currentOffset
        var consumed = 0
        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset)
            if (curOffset != newOffset) {
                searchView.currentOffset = newOffset
                consumed = curOffset - newOffset
            }
        }
        return consumed
    }

    internal fun getDownNestedScrollRange(): Int {
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

    internal fun getDownNestedPreScrollRange(): Int {
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

    internal fun getTotalScrollRange(): Int {
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

    class Behavior : CoordinatorLayout.Behavior<SearchLayout> {

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        constructor() : super()

        override fun layoutDependsOn(parent: CoordinatorLayout, child: SearchLayout, dependency: View): Boolean {
            return dependency is FragmentContainerView
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: SearchLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                    super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: SearchLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
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

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: SearchLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                consumed[1] = scroll(child, dyUnconsumed, -child.getDownNestedScrollRange(), 0)
            }
        }

        private fun scroll(child: SearchLayout, dy: Int, minOffset: Int, maxOffset: Int): Int {
            val consumed = child.setTopBottomOffset(
                    child.searchView.currentOffset - dy,
                    minOffset,
                    maxOffset)


            ViewCompat.offsetTopAndBottom(child.searchView, -consumed)
            return consumed
        }
    }

    companion object {
        private const val INVALID_SCROLL_RANGE = -1
    }
}