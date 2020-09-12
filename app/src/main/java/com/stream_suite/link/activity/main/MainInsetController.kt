package com.stream_suite.link.activity.main

import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.fragment.BlacklistFragment
import com.stream_suite.link.fragment.ScheduledMessagesFragment
import com.stream_suite.link.fragment.SearchFragment
import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.fragment.message.EdgeToEdgeKeyboardWorkaround
import com.stream_suite.link.fragment.message.MessageListFragment
import com.stream_suite.link.fragment.settings.MaterialPreferenceFragmentCompat
import com.stream_suite.link.shared.util.ActivityUtils
import com.stream_suite.link.shared.util.DensityUtil
import com.stream_suite.link.shared.util.doOnApplyWindowInsets

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

    fun overrideInsetsForStatusBar() {
        if (!useEdgeToEdge()) {
            return
        }

        val contentContainer = activity.findViewById<FrameLayout>(R.id.content_container)
        contentContainer.doOnApplyWindowInsets { view, insets, padding, _ ->
            view.setPadding(padding.left, insets.systemWindowInsetTop, padding.right, padding.bottom)
        }
    }

    fun modifyConversationListElements(fragment: ConversationListFragment?) {
        if (!useEdgeToEdge() || fragment == null) {
            return
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

    private fun useEdgeToEdge(): Boolean {
        return ActivityUtils.useEdgeToEdge()
    }
}