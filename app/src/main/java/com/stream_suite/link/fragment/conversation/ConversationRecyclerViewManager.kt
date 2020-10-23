package com.stream_suite.link.fragment.conversation

import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.stream_suite.link.MessengerApplication
import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.adapter.conversation.ConversationListAdapter
import com.stream_suite.link.fragment.ArchivedConversationListFragment
import com.stream_suite.link.fragment.FolderConversationListFragment
import com.stream_suite.link.fragment.PrivateConversationListFragment
import com.stream_suite.link.fragment.UnreadConversationListFragment
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.util.ColorUtils
import com.stream_suite.link.shared.util.TimeUtils
import com.stream_suite.link.utils.FixedScrollLinearLayoutManager
import com.stream_suite.link.utils.swipe_to_dismiss.SwipeItemDecoration

class ConversationRecyclerViewManager(private val fragment: ConversationListFragment) {

    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val layoutManager: FixedScrollLinearLayoutManager by lazy { FixedScrollLinearLayoutManager(activity) }
    var adapter: ConversationListAdapter? = null

    val recyclerView: RecyclerView by lazy { fragment.rootView!!.findViewById<View>(R.id.recycler_view) as RecyclerView }
    private val empty: View by lazy { fragment.rootView!!.findViewById<View>(R.id.empty_view) }

    fun setupViews() {
        if (activity == null) {
            return
        }

        empty.setBackgroundColor(Settings.mainColorSet.colorLight)
        ColorUtils.changeRecyclerOverscrollColors(recyclerView, Settings.mainColorSet.color)
    }

    fun loadConversations() {
        fragment.swipeHelper.clearPending()

        val handler = Handler()
        Thread {
            val startTime = TimeUtils.now

            if (activity == null) {
                return@Thread
            }

            val conversations = getCursorSafely()

            Log.v("conversation_load", "load took ${TimeUtils.now - startTime} ms")

            if (activity == null) {
                return@Thread
            }

            handler.post {
                setConversations(conversations.toMutableList())
                fragment.lastRefreshTime = TimeUtils.now

                try {
                    (activity!!.application as MessengerApplication).refreshDynamicShortcuts()
                } catch (e: Exception) {
                }
            }
        }.start()
    }

    fun canScroll(scrollable: Boolean) { layoutManager.setCanScroll(scrollable) }
    fun scrollToPosition(position: Int) { layoutManager.scrollToPosition(position) }
    fun getViewAtPosition(position: Int): View = recyclerView.findViewHolderForAdapterPosition(position)!!.itemView

    private fun getCursorSafely() = when {
        fragment is ArchivedConversationListFragment && activity != null -> DataSource.getArchivedConversationsAsList(activity!!)
        fragment is PrivateConversationListFragment && activity != null -> DataSource.getPrivateConversationsAsList(activity!!)
        fragment is UnreadConversationListFragment && activity != null -> DataSource.getUnreadNonPrivateConversationsAsList(activity!!)
        fragment is FolderConversationListFragment && activity != null -> fragment.queryConversations(activity!!)
        activity != null -> DataSource.getUnarchivedConversationsAsList(activity!!)
        else -> emptyList()
    }

    private fun setConversations(conversations: MutableList<Conversation>) {
        if (activity == null) {
            return
        }

        if (adapter != null) {
            adapter!!.conversations = conversations
            adapter!!.notifyDataSetChanged()
        } else {
            adapter = ConversationListAdapter(activity as MessengerActivity,
                    conversations, fragment.multiSelector, fragment, fragment)

            layoutManager.setCanScroll(true)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(SwipeItemDecoration())

            val touchHelper = fragment.swipeHelper.getSwipeTouchHelper(adapter!!)
            touchHelper.attachToRecyclerView(recyclerView)
        }

        fragment.messageListManager.tryOpeningFromArguments()
        checkEmptyViewDisplay()
    }

    fun checkEmptyViewDisplay() {
        if (recyclerView.adapter?.itemCount == 0 && empty.visibility == View.GONE) {
            empty.alpha = 0f
            empty.visibility = View.VISIBLE

            empty.animate().alpha(1f).setDuration(250).setListener(null)
        } else if (recyclerView.adapter?.itemCount != 0 && empty.visibility == View.VISIBLE) {
            empty.visibility = View.GONE
        }
    }
}