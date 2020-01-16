package xyz.stream.messenger.fragment.conversation

import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import xyz.stream.messenger.MessengerApplication
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.adapter.conversation.ConversationListAdapter
import xyz.stream.messenger.fragment.ArchivedConversationListFragment
import xyz.stream.messenger.fragment.FolderConversationListFragment
import xyz.stream.messenger.fragment.PrivateConversationListFragment
import xyz.stream.messenger.fragment.UnreadConversationListFragment
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Conversation
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.TimeUtils
import xyz.stream.messenger.utils.FixedScrollLinearLayoutManager
import xyz.stream.messenger.utils.swipe_to_dismiss.SwipeItemDecoration

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