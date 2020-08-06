package xyz.stream.messenger.activity.main

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
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
import xyz.stream.messenger.shared.util.doOnApplyWindowInsets

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
        val newSystemUiFlags = oldSystemUiFlags or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        activity.window.decorView.systemUiVisibility = newSystemUiFlags
    }

    @SuppressLint("RestrictedApi")
    fun overrideDrawerInsets() {
        if (!useEdgeToEdge()) {
            return
        }
    }

    fun modifyConversationListElements(fragment: ConversationListFragment?) {
        if (!useEdgeToEdge() || fragment == null) {
            return
        }

        val appBar = activity.findViewById<AppBarLayout>(R.id.app_bar_layout)
        appBar.doOnApplyWindowInsets { view, insets, padding, _ ->
            view.setPadding(padding.left, insets.systemWindowInsetTop, padding.right, padding.bottom)
        }

        val recycler = fragment.recyclerView
        recycler.clipToPadding = false
        recycler.setPadding(recycler.paddingLeft, recycler.paddingTop, recycler.paddingRight, bottomInsetValue)

        val snackbar = activity.snackbarContainer
        val layoutParams = snackbar.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin = bottomInsetValue
        snackbar.layoutParams = layoutParams
    }

    fun modifyScheduledMessageElements(fragment: ScheduledMessagesFragment) {
        if (!useEdgeToEdge()) {
            return
        }

        val appBar = activity.findViewById<AppBarLayout>(R.id.app_bar_layout)
        appBar.doOnApplyWindowInsets { view, insets, padding, _ ->
            view.setPadding(padding.left, insets.systemWindowInsetTop, padding.right, padding.bottom)
        }

        val recycler = fragment.list
        recycler.clipToPadding = false
        recycler.setPadding(recycler.paddingLeft, recycler.paddingTop, recycler.paddingRight, bottomInsetValue)

        // move fab above the nav bar
        val params = fragment.fab.layoutParams as FrameLayout.LayoutParams
        params.bottomMargin = sixteenDp + bottomInsetValue
    }

    fun modifyBlacklistElements(fragment: BlacklistFragment) {
        if (!useEdgeToEdge()) {
            return
        }

        val recycler = fragment.list
        recycler.clipToPadding = false
        recycler.setPadding(recycler.paddingLeft, recycler.paddingTop, recycler.paddingRight, bottomInsetValue)

        // move fab above the nav bar
        val params = fragment.fab.layoutParams as FrameLayout.LayoutParams
        params.bottomMargin = sixteenDp + bottomInsetValue
    }

    fun modifySearchListElements(fragment: SearchFragment?) {
        val recycler = fragment?.list
        if (!useEdgeToEdge() || recycler == null) {
            return
        }

        recycler.clipToPadding = false
        recycler.setPadding(recycler.paddingLeft, recycler.paddingTop, recycler.paddingRight, bottomInsetValue)
    }

    fun modifyMessageListElements(fragment: MessageListFragment) {
        if (!useEdgeToEdge()) {
            return
        }

        val appBar = fragment.rootView!!.findViewById<AppBarLayout>(R.id.app_bar_layout)
        appBar.doOnApplyWindowInsets { view, insets, padding, _ ->
            view.setPadding(padding.left, insets.systemWindowInsetTop, padding.right, padding.bottom)
        }

        val sendbar = fragment.nonDeferredInitializer.replyBarCard.getChildAt(0)
        sendbar.setPadding(sendbar.paddingLeft, sendbar.paddingTop, sendbar.paddingRight, DensityUtil.toDp(activity, 24) + bottomInsetValue) // 24 dp from initial layout...
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
        val layoutParams = view.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.bottomMargin = bottomInsetValue
        view.layoutParams = layoutParams

        return snackbar
    }

    private fun modifyMessengerActivityElements() {

        // put padding at the bottom of the navigation view's recycler view
        val navView = activity.navController.navigationView
        val navRecycler = navView.getChildAt(0) as RecyclerView
        navRecycler.clipToPadding = false
        navRecycler.setPadding(navView.paddingLeft, navView.paddingTop, navView.paddingRight, bottomInsetValue)
    }

    private fun useEdgeToEdge(): Boolean {
        return ActivityUtils.useEdgeToEdge()
    }
}