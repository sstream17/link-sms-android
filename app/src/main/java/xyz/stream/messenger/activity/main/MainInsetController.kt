package xyz.stream.messenger.activity.main

import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.fragment.BlacklistFragment
import xyz.stream.messenger.fragment.ScheduledMessagesFragment
import xyz.stream.messenger.fragment.SearchFragment
import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.fragment.message.EdgeToEdgeKeyboardWorkaround
import xyz.stream.messenger.fragment.message.MessageListFragment
import xyz.stream.messenger.fragment.settings.MaterialPreferenceFragmentCompat
import xyz.stream.messenger.shared.util.ActivityUtils
import xyz.stream.messenger.shared.util.DensityUtil
import xyz.stream.messenger.shared.util.applySystemWindowInsetsMargin
import xyz.stream.messenger.shared.util.applySystemWindowInsetsPadding
import xyz.stream.messenger.shared.view.WhitableToolbar

class MainInsetController(private val activity: MessengerActivity) {

    private val keyboardWorkaround: EdgeToEdgeKeyboardWorkaround by lazy { EdgeToEdgeKeyboardWorkaround(activity) }
    private val sixteenDp: Int by lazy { DensityUtil.toDp(activity, 16) }
    var bottomInsetValue: Int = 0

    fun onResume() {
        if (!useEdgeToEdge()) {
            return
        }

        keyboardWorkaround.addListener()
    }

    fun onPause() {
        if (!useEdgeToEdge()) {
            return
        }

        keyboardWorkaround.removeListener()
    }

    fun applyWindowStatusFlags() {
        if (!useEdgeToEdge()) {
            return
        }

        val oldSystemUiFlags = activity.window.decorView.systemUiVisibility
        val newSystemUiFlags = oldSystemUiFlags or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        activity.window.decorView.systemUiVisibility = newSystemUiFlags
    }

    fun modifyConversationListElements(fragment: ConversationListFragment?) {
        if (!useEdgeToEdge() || fragment == null) {
            return
        }

        val recycler = fragment.recyclerView
        recycler.clipToPadding = false
        recycler.doOnPreDraw {
            val searchBar = activity.searchBar
            val searchBarHeight = searchBar.measuredHeight + sixteenDp
            recycler.setPadding(recycler.paddingLeft, searchBarHeight, recycler.paddingRight, recycler.paddingBottom)
            recycler.applySystemWindowInsetsPadding(applyTop = true)
            val navBar = activity.findViewById<BottomNavigationView>(R.id.nav_view)
            val navBarHeight = navBar.measuredHeight
            val layoutParams = recycler.layoutParams as FrameLayout.LayoutParams
            layoutParams.bottomMargin = navBarHeight
            recycler.layoutParams = layoutParams
        }

        val snackbar = activity.snackbarContainer
        val layoutParams = snackbar.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin = bottomInsetValue
        snackbar.layoutParams = layoutParams
    }

    fun modifyScheduledMessageElements(fragment: ScheduledMessagesFragment) {
        if (!useEdgeToEdge()) {
            return
        }

        val recycler = fragment.list
        recycler.clipToPadding = false
        recycler.doOnPreDraw {
            val searchBar = activity.searchBar
            val searchBarHeight = searchBar.measuredHeight + sixteenDp
            recycler.setPadding(recycler.paddingLeft, searchBarHeight, recycler.paddingRight, recycler.paddingBottom)
            recycler.applySystemWindowInsetsPadding(applyTop = true)
            val navBar = activity.findViewById<BottomNavigationView>(R.id.nav_view)
            val navBarHeight = navBar.measuredHeight
            val layoutParams = recycler.layoutParams as FrameLayout.LayoutParams
            layoutParams.bottomMargin = navBarHeight
            recycler.layoutParams = layoutParams
        }

        val fab = fragment.fab
        fab.doOnPreDraw {
            val navBar = activity.findViewById<BottomNavigationView>(R.id.nav_view)
            val navBarHeight = navBar.measuredHeight
            val layoutParams = fab.layoutParams as FrameLayout.LayoutParams
            layoutParams.bottomMargin = navBarHeight + sixteenDp
            fab.layoutParams = layoutParams
        }
    }

    fun modifyBlacklistElements(fragment: BlacklistFragment) {
        if (!useEdgeToEdge()) {
            return
        }

        val recycler = fragment.list
        recycler.clipToPadding = false
        recycler.applySystemWindowInsetsPadding(applyTop = true, applyBottom = true)

        val fab = fragment.fab
        val layoutParams = fab.layoutParams as FrameLayout.LayoutParams
        layoutParams.bottomMargin = sixteenDp
        fab.layoutParams = layoutParams
        fab.applySystemWindowInsetsMargin(applyBottom = true)
    }

    fun modifySearchListElements(fragment: SearchFragment?) {
        val recycler = fragment?.list
        if (!useEdgeToEdge() || recycler == null) {
            return
        }

        recycler.clipToPadding = false
        recycler.applySystemWindowInsetsPadding(applyBottom = true)
    }

    fun modifyMessageListElements(fragment: MessageListFragment) {
        if (!useEdgeToEdge()) {
            return
        }

        val sendbar = fragment.nonDeferredInitializer.replyBarCard.getChildAt(0)
        sendbar.applySystemWindowInsetsPadding(applyBottom = true)

        val toolbar = fragment.rootView!!.findViewById<WhitableToolbar>(R.id.toolbar)
        toolbar.applySystemWindowInsetsPadding(applyTop = true)
    }

    fun modifyPreferenceFragmentElements(fragment: MaterialPreferenceFragmentCompat) {
        if (!useEdgeToEdge()) {
            return
        }

        val recycler = fragment.listView
        recycler.clipToPadding = false
        recycler.setPadding(recycler.paddingLeft, recycler.paddingTop, recycler.paddingRight, bottomInsetValue)
    }

    fun adjustSnackbar(snackbar: Snackbar): Snackbar {
        if (!useEdgeToEdge()) {
            return snackbar
        }

        val view = snackbar.view
        view.doOnPreDraw {
            val navbar = activity.findViewById<BottomNavigationView>(R.id.nav_view)
            val navbarHeight = navbar.measuredHeight
            val layoutParams = view.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.bottomMargin = navbarHeight
            view.layoutParams = layoutParams
        }

        return snackbar
    }

    private fun useEdgeToEdge(): Boolean {
        return ActivityUtils.useEdgeToEdge()
    }
}