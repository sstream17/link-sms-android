package xyz.stream.messenger.activity.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.activity.SettingsActivity
import xyz.stream.messenger.activity.passcode.PasscodeVerificationActivity
import xyz.stream.messenger.fragment.*
import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.fragment.settings.AboutFragment
import xyz.stream.messenger.fragment.settings.HelpAndFeedbackFragment
import xyz.stream.messenger.fragment.settings.MyAccountFragment
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Folder
import xyz.stream.messenger.shared.util.AnimationUtils
import xyz.stream.messenger.shared.util.TimeUtils


class MainNavigationConversationListActionDelegate(private val activity: MessengerActivity) {

    private val navController
        get() = activity.navController
    private val intentHandler
        get() = activity.intentHandler

    fun displayConversations(): Boolean {
        return displayConversations(null)
    }

    fun displayConversations(savedInstanceState: Bundle?): Boolean {
        activity.fab.show()
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
            transaction.replace(R.id.recycler_view, navController.conversationListFragment!!)
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
        return displayFragmentWithBackStack(UnreadConversationListFragment())
    }

    internal fun displayFolder(folder: Folder): Boolean {
        return displayFragmentWithBackStack(FolderConversationListFragment.getInstance(folder))
    }

    internal fun displayScheduledMessages(): Boolean {
        return displayFragmentWithBackStack(ScheduledMessagesFragment())
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

    // TODO: Replace with Navigation architecture
    internal fun displayFragmentWithBackStack(fragment: Fragment): Boolean {
        activity.searchHelper.closeSearch()
        activity.fab.hide()
        activity.invalidateOptionsMenu()
        navController.inSettings = true

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