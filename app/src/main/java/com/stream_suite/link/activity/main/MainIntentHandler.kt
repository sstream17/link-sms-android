package com.stream_suite.link.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.api.implementation.ApiUtils
import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.shared.MessengerActivityExtras
import com.stream_suite.link.shared.service.jobs.SubscriptionExpirationCheckJob
import com.stream_suite.link.shared.util.DensityUtil
import com.stream_suite.link.shared.util.show

class MainIntentHandler(private val activity: MessengerActivity) {

    private val navController
        get() = activity.navController
    private val activityIntent
        get() = activity.intent

    fun newIntent(intent: Intent) {
        val handled = handleShortcutIntent(intent)
        val convoId = intent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)

        intent.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)

        if (!handled && convoId != -1L) {
            activityIntent.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, convoId)
            navController.conversationActionDelegate.displayConversations()
        }
    }

    fun handleShortcutIntent(intent: Intent): Boolean {
        if (intent.data != null && intent.dataString!!.contains("https://link.stream-suite.com/")) {
            try {
                if (navController.isConversationListExpanded()) {
                    activity.onBackPressed()
                }

                displayShortcutConversation(java.lang.Long.parseLong(intent.data!!.lastPathSegment!!))
                activityIntent.data = null

                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        activityIntent.data = null
        return false
    }

    fun displayPrivateFromNotification() {
        val openPrivate = activityIntent.getBooleanExtra(MessengerActivityExtras.EXTRA_OPEN_PRIVATE, false)
        val conversationId = activityIntent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)
        activityIntent.putExtra(MessengerActivityExtras.EXTRA_OPEN_PRIVATE, false)

        if (openPrivate) {
            navController.conversationActionDelegate.displayPrivate()
        }
    }

    fun displayAccount() {
        if (activityIntent.getBooleanExtra(MessengerActivityExtras.EXTRA_START_MY_ACCOUNT, false)) {
            NotificationManagerCompat.from(activity).cancel(SubscriptionExpirationCheckJob.NOTIFICATION_ID)
            navController.onNavigationItemSelected(R.id.drawer_account)

            activityIntent.removeExtra(MessengerActivityExtras.EXTRA_START_MY_ACCOUNT)
        }
    }

    fun displayConversation(savedInstanceState: Bundle?): Pair<Long, Long> {
        var convoId = activityIntent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)
        var messageId = activityIntent.getLongExtra(MessengerActivityExtras.EXTRA_MESSAGE_ID, -1L)

        activityIntent.putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)
        activityIntent.putExtra(MessengerActivityExtras.EXTRA_MESSAGE_ID, -1L)

        if (savedInstanceState?.containsKey(MessengerActivityExtras.EXTRA_CONVERSATION_ID) == true) {
            convoId = savedInstanceState.getLong(MessengerActivityExtras.EXTRA_CONVERSATION_ID)
            messageId = -1L

            savedInstanceState.remove(MessengerActivityExtras.EXTRA_CONVERSATION_ID)
        }

        return Pair(convoId, messageId)
    }

    fun saveInstanceState(outState: Bundle?): Bundle {
        var outState = outState

        if (outState == null) {
            outState = Bundle()
        }

        if (navController.selectedNavigationItemId != R.id.navigation_inbox) {
            outState.putInt(MessengerActivityExtras.EXTRA_NAVIGATION_ITEM_ID, navController.selectedNavigationItemId)
        } else if (navController.isConversationListExpanded()) {
            outState.putLong(MessengerActivityExtras.EXTRA_CONVERSATION_ID, navController.getShownConversationList()!!.expandedId)
        }

        return outState
    }

    fun restoreNavigationSelection(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && savedInstanceState.containsKey(MessengerActivityExtras.EXTRA_NAVIGATION_ITEM_ID)) {
            val navItemId = savedInstanceState.getInt(MessengerActivityExtras.EXTRA_NAVIGATION_ITEM_ID)
            navController.onNavigationItemSelected(navItemId)
        }
    }

    fun dismissIfFromNotification() {
        val fromNotification = activityIntent.getBooleanExtra(MessengerActivityExtras.EXTRA_FROM_NOTIFICATION, false)
        val convoId = activityIntent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)

        activityIntent.putExtra(MessengerActivityExtras.EXTRA_FROM_NOTIFICATION, false)

        if (fromNotification && convoId != -1L) {
            ApiUtils.dismissNotification(Account.accountId, Account.deviceId, convoId)
        }
    }

    private fun displayShortcutConversation(convo: Long) {
        navController.navigationView.show()
        activity.toolbar.alignTitleCenter()
        activity.invalidateOptionsMenu()
        navController.inSettings = false

        navController.conversationListFragment = ConversationListFragment.newInstance(convo)
        navController.otherFragment = null

        val transaction = activity.supportFragmentManager.beginTransaction()

        if (navController.conversationListFragment != null) {
            transaction.replace(R.id.conversation_list_container, navController.conversationListFragment!!)
        }

        if (!DensityUtil.isSmallestWidth600Landscape(activity)) {
            val messageList = activity.supportFragmentManager.findFragmentById(R.id.message_list_container)
            if (messageList != null) {
                transaction.remove(messageList)
            }
        }

        transaction.commit()
    }
}