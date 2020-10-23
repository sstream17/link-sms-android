package com.stream_suite.link.activity.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.activity.SettingsActivity
import com.stream_suite.link.activity.passcode.PasscodeVerificationActivity
import com.stream_suite.link.fragment.*
import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.fragment.settings.AboutFragment
import com.stream_suite.link.fragment.settings.HelpAndFeedbackFragment
import com.stream_suite.link.fragment.settings.MyAccountFragment
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.util.*


class MainNavigationConversationListActionDelegate(private val activity: MessengerActivity) {

    private val navController
        get() = activity.navController
    private val intentHandler
        get() = activity.intentHandler

    fun displayConversations(): Boolean {
        return displayConversations(null)
    }

    fun displayConversations(savedInstanceState: Bundle?): Boolean {
        navController.navigationView.menu.findItem(R.id.navigation_inbox).isChecked = true
        navController.navigationView.show()
        activity.toolbar.alignTitleCenter()
        activity.invalidateOptionsMenu()
        navController.inSettings = false

        val (convoId, messageId) = intentHandler.displayConversation(savedInstanceState)

        var updateConversationListSize = false
        if (messageId != -1L && convoId != -1L) {
            navController.conversationListFragment = ConversationListFragment.newInstance(convoId, messageId)
            updateConversationListSize = true
        } else if (convoId != -1L && convoId != 0L) {
            navController.conversationListFragment = ConversationListFragment.newInstance(convoId)
            updateConversationListSize = true
        } else {
            navController.conversationListFragment = ConversationListFragment.newInstance()
        }

        if (updateConversationListSize) {
            val content = activity.findViewById<View>(R.id.content)
            content.post {
                AnimationUtils.conversationListSize = content.height
                AnimationUtils.toolbarSize = activity.toolbar.height
            }
        }

        navController.otherFragment = null

        val transaction = activity.supportFragmentManager.beginTransaction()

        if (navController.conversationListFragment != null) {
            transaction.replace(R.id.conversation_list_container, navController.conversationListFragment!!)
        }

        if (!DensityUtil.isSmallestWidth600Landscape(activity)) {
            val messageList = activity.supportFragmentManager
                    .findFragmentById(R.id.message_list_container)

            if (messageList != null) {
                transaction.remove(messageList)
            }
        }

        try {
            transaction.commit()
        } catch (e: Exception) {
        }

        return true
    }

    internal fun displayArchived(): Boolean {
        return displayFragmentWithBackStack(ArchivedConversationListFragment())
    }

    internal fun displayPrivate(): Boolean {
        val showPrivateConversations: () -> Unit = {
            displayFragmentWithBackStack(PrivateConversationListFragment())
        }

        val lastEntry = Settings.privateConversationsLastPasscodeEntry
        return if (Settings.privateConversationsPasscode.isNullOrEmpty() || TimeUtils.now - lastEntry < TimeUtils.MINUTE) {
            showPrivateConversations()
            true
        } else {
            PasscodeVerificationActivity.show(activity, showPrivateConversations)
            false
        }
    }

    internal fun displayUnread(): Boolean {
        navController.navigationView.menu.findItem(R.id.navigation_unread).isChecked = true
        return displayFragmentWithBackStack(UnreadConversationListFragment(), false)
    }

    internal fun displayCompose(): Boolean {
        activity.composeMessage()
        return false
    }

    internal fun displayScheduledMessages(): Boolean {
        navController.navigationView.menu.findItem(R.id.navigation_scheduled).isChecked = true
        return displayFragmentWithBackStack(ScheduledMessagesFragment(), false)
    }

    internal fun displayBlacklist(): Boolean {
        return displayFragmentWithBackStack(BlacklistFragment.newInstance())
    }

    internal fun displayInviteFriends(): Boolean {
        return displayFragmentWithBackStack(InviteFriendsFragment())
    }

    internal fun displaySettings(): Boolean {
        SettingsActivity.startGlobalSettings(activity)
        return true
    }

    internal fun displayFeatureSettings(): Boolean {
        SettingsActivity.startFeatureSettings(activity)
        return true
    }

    internal fun displayMyAccount(): Boolean {
        return displayFragmentWithBackStack(MyAccountFragment())
    }

    internal fun displayHelpAndFeedback(): Boolean {
        return displayFragmentWithBackStack(HelpAndFeedbackFragment())
    }

    internal fun displayAbout(): Boolean {
        return displayFragmentWithBackStack(AboutFragment())
    }

    internal fun displayEditFolders(): Boolean {
        SettingsActivity.startFolderSettings(activity)
        return true
    }

    internal fun displayFragmentWithBackStack(
            fragment: Fragment,
            hideBottomNav: Boolean = true): Boolean {
        activity.searchHelper.closeSearch()
        if (hideBottomNav) {
            navController.navigationView.hide()
            activity.toolbar.alignTitleStart()
            navController.inSettings = true
        }

        activity.invalidateOptionsMenu()

        navController.otherFragment = fragment
        Handler().postDelayed({
            try {
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.conversation_list_container, fragment)
                        .commit()
            } catch (e: Exception) {
                activity.finish()
                activity.overridePendingTransition(0, 0)
                activity.startActivity(Intent(activity, MessengerActivity::class.java))
                activity.overridePendingTransition(0, 0)
            }
        }, 200)

        return true
    }
}