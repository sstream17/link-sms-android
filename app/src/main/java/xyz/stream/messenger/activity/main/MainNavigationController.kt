package xyz.stream.messenger.activity.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.fragment.message.MessageListFragment
import xyz.stream.messenger.shared.MessengerActivityExtras
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.service.ApiDownloadService
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.PhoneNumberUtils
import xyz.stream.messenger.shared.util.StringUtils
import xyz.stream.messenger.shared.util.listener.BackPressedListener

@Suppress("DEPRECATION")
class MainNavigationController(private val activity: MessengerActivity) : NavController(activity) {

    val conversationActionDelegate = MainNavigationConversationListActionDelegate(activity)
    val messageActionDelegate = MainNavigationMessageListActionDelegate(activity)

    val navigationView: NavigationView by lazy { activity.findViewById<View>(R.id.navigation_conversations) as NavigationView }
    val drawerLayout: DrawerLayout? by lazy { activity.findViewById<View>(R.id.drawer_layout) as DrawerLayout? }

    var conversationListFragment: ConversationListFragment? = null
    var otherFragment: Fragment? = null
    var inSettings = false
    var selectedNavigationItemId: Int = R.id.drawer_conversation

    fun isConversationListExpanded() = conversationListFragment != null && conversationListFragment!!.isExpanded
    fun isOtherFragmentConvoAndShowing() = otherFragment != null && otherFragment is ConversationListFragment && (otherFragment as ConversationListFragment).isExpanded
    fun getShownConversationList() = when {
        isOtherFragmentConvoAndShowing() -> otherFragment as ConversationListFragment
        else -> conversationListFragment
    }

    fun backPressed(): Boolean {
        val fragments = activity.supportFragmentManager.fragments.first().childFragmentManager.fragments

        fragments
                .filter { it is BackPressedListener && (it as BackPressedListener).onBackPressed() }
                .forEach { return true }

        when {
            conversationListFragment == null -> {
                val messageListFragment = findMessageListFragment()
                if (messageListFragment != null) {
                    try {
                        activity.supportFragmentManager.beginTransaction().remove(messageListFragment).commit()
                    } catch (e: Exception) {
                    }
                }

                conversationActionDelegate.displayConversations()
                activity.fab.show()
                drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                return true
            }
            else -> return false
        }
    }

    fun findMessageListFragment(): MessageListFragment? =
            activity.supportFragmentManager.findFragmentById(R.id.nav_host) as? MessageListFragment

    fun drawerItemClicked(id: Int): Boolean {
        conversationListFragment?.swipeHelper?.dismissSnackbars()

        when (id) {
            R.id.drawer_conversation -> return conversationActionDelegate.displayConversations()
            R.id.drawer_archived -> return conversationActionDelegate.displayArchived()
            R.id.drawer_private -> return conversationActionDelegate.displayPrivate()
            R.id.drawer_unread -> return conversationActionDelegate.displayUnread()
            R.id.drawer_schedule -> return conversationActionDelegate.displayScheduledMessages()
            R.id.drawer_mute_contacts -> return conversationActionDelegate.displayBlacklist()
            R.id.drawer_invite -> return conversationActionDelegate.displayInviteFriends()
            R.id.drawer_feature_settings -> return conversationActionDelegate.displayFeatureSettings()
            R.id.drawer_settings -> return conversationActionDelegate.displaySettings()
            R.id.drawer_account -> return conversationActionDelegate.displayMyAccount()
            R.id.drawer_help -> return conversationActionDelegate.displayHelpAndFeedback()
            R.id.drawer_about -> return conversationActionDelegate.displayAbout()
            R.id.drawer_edit_folders -> return conversationActionDelegate.displayEditFolders()
            R.id.menu_view_contact, R.id.drawer_view_contact -> return messageActionDelegate.viewContact()
            R.id.menu_view_media, R.id.drawer_view_media -> return messageActionDelegate.viewMedia()
            R.id.menu_delete_conversation, R.id.drawer_delete_conversation -> return messageActionDelegate.deleteConversation()
            R.id.menu_archive_conversation, R.id.drawer_archive_conversation -> return messageActionDelegate.archiveConversation()
            R.id.menu_conversation_information, R.id.drawer_conversation_information -> return messageActionDelegate.conversationInformation()
            R.id.menu_conversation_blacklist, R.id.drawer_conversation_blacklist -> return messageActionDelegate.conversationBlacklist()
            R.id.menu_conversation_blacklist_all, R.id.drawer_conversation_blacklist_all -> return messageActionDelegate.conversationBlacklistAll()
            R.id.menu_conversation_schedule, R.id.drawer_conversation_schedule -> return messageActionDelegate.conversationSchedule()
            R.id.menu_contact_settings, R.id.drawer_contact_settings -> return messageActionDelegate.contactSettings()
            R.id.menu_call_with_duo -> return messageActionDelegate.callWithDuo()
            R.id.menu_show_bubble -> return messageActionDelegate.showBubble()
            R.id.menu_call -> return if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                messageActionDelegate.callContact()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), MessengerActivityExtras.REQUEST_CALL_PERMISSION)
                    false
                } else {
                    messageActionDelegate.callContact()
                }
            }

            else -> {
                return true
            }
        }
    }

    fun optionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home, R.id.menu_search -> true
        else -> false
    }
}