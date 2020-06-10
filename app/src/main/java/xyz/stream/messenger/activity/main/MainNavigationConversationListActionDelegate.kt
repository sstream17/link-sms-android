package xyz.stream.messenger.activity.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
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

        if (messageId != -1L && convoId != -1L) {
            navController.conversationListFragment = ConversationListFragment.newInstance(convoId, messageId)
        } else if (convoId != -1L && convoId != 0L) {
            navController.conversationListFragment = ConversationListFragment.newInstance(convoId)
        } else {
            navController.conversationListFragment = ConversationListFragment.newInstance()
        }

        navController.otherFragment = null

        if (navController.conversationListFragment != null) {
            findNavController(activity, R.id.nav_host).setGraph(R.navigation.navigation_conversations, navController.conversationListFragment!!.arguments)
        }

        return true
    }

    internal fun displayPrivate(): Boolean {
        val showPrivateConversations: () -> Unit = {
            displayFragmentWithBackStack(PrivateConversationListFragment(), R.id.navigation_private)
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

    internal fun displayFolder(folder: Folder): Boolean {
        return displayFragmentWithBackStack(FolderConversationListFragment.getInstance(folder), R.id.navigation_folder)
    }

    internal fun displayBlacklist(): Boolean {
        return displayFragmentWithBackStack(BlacklistFragment.newInstance(), R.id.navigation_blacklist)
    }

    internal fun displayInviteFriends(): Boolean {
        return displayFragmentWithBackStack(InviteFriendsFragment(), R.id.navigation_invite)
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
        return displayFragmentWithBackStack(MyAccountFragment(), R.id.navigation_account)
    }

    internal fun displayHelpAndFeedback(): Boolean {
        return displayFragmentWithBackStack(HelpAndFeedbackFragment(), R.id.navigation_help_feedback)
    }

    internal fun displayAbout(): Boolean {
        return displayFragmentWithBackStack(AboutFragment(), R.id.navigation_about)
    }

    internal fun displayEditFolders(): Boolean {
        SettingsActivity.startFolderSettings(activity)
        return true
    }

    internal fun displayFragmentWithBackStack(fragment: Fragment, id: Int): Boolean {
        activity.searchHelper.closeSearch()
        activity.fab.hide()
        activity.invalidateOptionsMenu()
        navController.inSettings = true

        navController.otherFragment = fragment
        findNavController(activity, R.id.nav_host).navigate(id)

        return true
    }
}