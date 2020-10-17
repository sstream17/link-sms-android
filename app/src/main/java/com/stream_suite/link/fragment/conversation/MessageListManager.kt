package com.stream_suite.link.fragment.conversation

import android.os.Handler
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.stream_suite.link.R
import com.stream_suite.link.adapter.view_holder.ConversationViewHolder
import com.stream_suite.link.fragment.message.MessageInstanceManager
import com.stream_suite.link.fragment.message.MessageListFragment
import com.stream_suite.link.shared.MessengerActivityExtras
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.util.ActivityUtils
import com.stream_suite.link.shared.util.AnimationUtils
import com.stream_suite.link.shared.util.ColorUtils
import com.stream_suite.link.shared.util.DensityUtil

class MessageListManager(private val fragment: ConversationListFragment) {

    companion object {
        internal val ARG_CONVERSATION_TO_OPEN_ID = "conversation_to_open"
        internal val ARG_MESSAGE_TO_OPEN_ID = "message_to_open"
    }

    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val clickHandler: Handler by lazy { Handler() }

    var expandedConversation: ConversationViewHolder? = null
    var messageListFragment: MessageListFragment? = null

    fun onConversationExpanded(viewHolder: ConversationViewHolder): Boolean {
        fragment.updateHelper.updateInfo = null

        val activity = activity
        if ((expandedConversation != null && expandedConversation!!.conversation!!.id == viewHolder.conversation!!.id)
                || activity == null) {
            return false
        }

        fragment.swipeHelper.dismissSnackbars()

        expandedConversation?.expanded = false
        expandedConversation = viewHolder
        AnimationUtils.expandActivityForConversation(activity)

        val args = fragment.arguments
        if (args != null && args.containsKey(ARG_MESSAGE_TO_OPEN_ID)) {
            messageListFragment = MessageInstanceManager.newInstance(viewHolder.conversation!!,
                    args.getLong(ARG_MESSAGE_TO_OPEN_ID))
            args.remove(ARG_MESSAGE_TO_OPEN_ID)
        } else {
            messageListFragment = MessageInstanceManager.newInstance(viewHolder.conversation!!)
        }

        if (messageListFragment != null) {
            try {
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.message_list_container, messageListFragment!!)
                        .commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!Settings.useGlobalThemeColor) {
            ActivityUtils.setTaskDescription(activity,
                    viewHolder.conversation!!.title!!, viewHolder.conversation!!.colors.color)
        }

        if (!DensityUtil.isSmallestWidth600(activity)) {
            fragment.recyclerManager.canScroll(false)
        }

        activity.intent?.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)
        fragment.arguments?.putLong(ARG_CONVERSATION_TO_OPEN_ID, -1L)

        return true
    }

    fun onConversationContracted() {
        expandedConversation = null
        AnimationUtils.contractActivityFromConversation(activity)

        if (messageListFragment == null) {
            return
        }

        val contractedId = messageListFragment!!.conversationId
        messageListFragment?.view?.animate()?.alpha(0f)?.setDuration(100)?.start()

        try {
            Handler().postDelayed({
                if (messageListFragment != null) {
                    try {
                        activity?.supportFragmentManager?.beginTransaction()
                                ?.remove(messageListFragment!!)
                                ?.commit()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                messageListFragment = null
            }, AnimationUtils.EXPAND_CONVERSATION_DURATION.toLong())
        } catch (e: Exception) {
        }

        val color = Settings.mainColorSet
        ColorUtils.adjustStatusBarColor(color.color, color.colorDark, activity)

        ColorUtils.changeRecyclerOverscrollColors(fragment.recyclerView, color.color)
        ActivityUtils.setTaskDescription(activity)

        fragment.updateHelper.broadcastUpdateInfo()
        fragment.updateHelper.broadcastTitleChange(contractedId)

        fragment.recyclerManager.canScroll(true)
    }

    fun tryOpeningFromArguments() {
        val args = fragment.arguments
        if (args != null) {
            val conversationToOpen = args.getLong(ARG_CONVERSATION_TO_OPEN_ID, -1L)
            args.putLong(ARG_CONVERSATION_TO_OPEN_ID, -1L)

            if (conversationToOpen != -1L) {
                clickConversationWithId(conversationToOpen)
            }
        } else {
            Log.v("Conversation List", "no conversations to open")
        }
    }

    private fun clickConversationWithId(id: Long) {
        val conversationPosition = fragment.adapter?.findPositionForConversationId(id) ?: return
        if (conversationPosition != -1) {
            fragment.recyclerManager.scrollToPosition(conversationPosition)
            clickConversationAtPosition(conversationPosition)
        }
    }

    private fun clickConversationAtPosition(position: Int) {
        clickHandler.removeCallbacksAndMessages(null)
        clickHandler.postDelayed({
            try {
                val itemView = fragment.recyclerManager.getViewAtPosition(position)
                itemView.isSoundEffectsEnabled = false
                itemView.performClick()
                itemView.isSoundEffectsEnabled = true
            } catch (e: Exception) {
                // not yet ready to click
                clickConversationAtPosition(position)
            }
        }, 100)
    }

}