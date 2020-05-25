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
import xyz.stream.messenger.shared.R

class SearchLayout : CoordinatorLayout {

    private val searchView: PersistentSearchView by lazy { findViewById<View>(R.id.search_view) as PersistentSearchView }
    private val searchContainer: LinearLayout by lazy { findViewById<View>(R.id.search_container) as LinearLayout }

    private var _isSearchOpen: Boolean = false
    private var pastText: String = ""
    private var onQueryTextListener: OnQueryTextListener? = null
    private var searchViewListener: SearchViewListener? = null

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context) : super(context) {
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
        searchView.isSearchOpen = true
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
        searchView.isSearchOpen = false

        invalidateScrollRanges()
    }

    fun invalidateScrollRanges() {
        searchView.invalidateScrollRanges()
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
}