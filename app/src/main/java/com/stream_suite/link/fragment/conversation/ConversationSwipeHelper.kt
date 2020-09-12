package com.stream_suite.link.fragment.conversation

import android.os.Handler
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.snackbar.Snackbar
import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.adapter.conversation.ConversationListAdapter
import com.stream_suite.link.fragment.ArchivedConversationListFragment
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.model.Conversation
import com.stream_suite.link.shared.util.SnackbarAnimationFix
import com.stream_suite.link.utils.swipe_to_dismiss.SwipeTouchHelper
import com.stream_suite.link.utils.swipe_to_dismiss.setup.SwipeSetupUnarchive

class ConversationSwipeHelper(private val fragment: ConversationListFragment) {

    private val activity: FragmentActivity?  = fragment.activity

    private val pendingDelete = mutableListOf<Conversation>()
    private var pendingArchive = mutableListOf<Conversation>()

    private var deleteSnackbar: Snackbar? = null
    private var archiveSnackbar: Snackbar? = null

    private val deleteSnackbarCallback = object : Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            super.onDismissed(snackbar, event)
            dismissDeleteSnackbar()
            clearPending()
        }
    }

    private val archiveSnackbarCallback = object : Snackbar.Callback() {
        override fun onDismissed(snackbar: Snackbar?, event: Int) {
            super.onDismissed(snackbar, event)
            dismissArchiveSnackbar()
            clearPending()
        }
    }

    fun getSwipeTouchHelper(adapter: ConversationListAdapter): ItemTouchHelper {
        return if (fragment is ArchivedConversationListFragment)
            SwipeTouchHelper(SwipeSetupUnarchive(adapter))
        else SwipeTouchHelper(adapter)
    }

    fun clearPending() {
        pendingDelete.clear()
        pendingArchive.clear()
    }

    fun onSwipeToDelete(conversation: Conversation) {
        pendingDelete.add(conversation)

        val plural = activity?.resources?.getQuantityString(R.plurals.conversations_deleted,
                pendingDelete.size, pendingDelete.size)

        archiveSnackbar?.dismiss()
        deleteSnackbar?.removeCallback(deleteSnackbarCallback)

        deleteSnackbar = Snackbar.make(fragment.recyclerView, plural ?: "", UNDO_DURATION)
                .setAction(R.string.undo) { fragment.recyclerManager.loadConversations() }
                .addCallback(deleteSnackbarCallback)
        SnackbarAnimationFix.apply(deleteSnackbar!!)
        if (activity is MessengerActivity) {
            activity.insetController.adjustSnackbar(deleteSnackbar!!).show()
        } else {
            deleteSnackbar?.show()
        }

        NotificationManagerCompat.from(activity!!).cancel(conversation.id.toInt())

        // for some reason, if this is done immediately then the final snackbar will not be
        // displayed
        Handler().postDelayed({ fragment.checkEmptyViewDisplay() }, 500)
    }

    fun onSwipeToArchive(conversation: Conversation) {
        pendingArchive.add(conversation)

        val plural = activity?.resources?.getQuantityString(
                if (fragment is ArchivedConversationListFragment) R.plurals.conversations_unarchived else R.plurals.conversations_archived,
                pendingArchive.size, pendingArchive.size)

        deleteSnackbar?.dismiss()
        archiveSnackbar?.removeCallback(archiveSnackbarCallback)

        archiveSnackbar = Snackbar.make(fragment.recyclerView, plural ?: "", UNDO_DURATION)
                .setAction(R.string.undo) { fragment.recyclerManager.loadConversations() }
                .addCallback(archiveSnackbarCallback)
        SnackbarAnimationFix.apply(archiveSnackbar!!)
        if (activity is MessengerActivity) {
            activity.insetController.adjustSnackbar(archiveSnackbar!!).show()
        } else {
            archiveSnackbar?.show()
        }

        NotificationManagerCompat.from(activity!!).cancel(conversation.id.toInt())

        // for some reason, if this is done immediately then the final snackbar will not be
        // displayed
        Handler().postDelayed({ fragment.checkEmptyViewDisplay() }, 500)
    }

    fun dismissSnackbars() {
        archiveSnackbar?.dismiss()
        deleteSnackbar?.dismiss()

        dismissArchiveSnackbar()
        dismissDeleteSnackbar()
    }

    private fun dismissDeleteSnackbar() {
        deleteSnackbar = null

        val list = mutableListOf<Conversation>()
        list.addAll(pendingDelete)
        pendingDelete.clear()

        Thread {
            list.forEach { performDeleteOperation(it) }
        }.start()
    }

    private fun dismissArchiveSnackbar() {
        archiveSnackbar = null

        val list = mutableListOf<Conversation>()
        list.addAll(pendingArchive)
        pendingArchive.clear()

        Thread {
            list.forEach { performArchiveOperation(it) }
        }.start()
    }

    private fun performDeleteOperation(conversation: Conversation) {
        if (activity != null) {
            DataSource.deleteConversation(activity, conversation)
        }
    }

    private fun performArchiveOperation(conversation: Conversation) {
        if (activity != null) {
            if (fragment is ArchivedConversationListFragment) {
                DataSource.unarchiveConversation(activity, conversation.id)
            } else {
                DataSource.archiveConversation(activity, conversation.id)
            }
        }
    }

    private companion object {
        val UNDO_DURATION = 6000
    }
}