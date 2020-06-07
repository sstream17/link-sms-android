package xyz.stream.messenger.activity.main

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.adapter.options.OptionsMenuAdapter
import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.fragment.message.MessageListFragment
import xyz.stream.messenger.shared.MessengerActivityExtras
import xyz.stream.messenger.shared.util.ActivityUtils
import xyz.stream.messenger.shared.util.MiddleDividerItemDecoration
import xyz.stream.messenger.shared.util.options.OptionsMenuDataFactory
import xyz.stream.messenger.utils.FixedScrollLinearLayoutManager

@Suppress("DEPRECATION")
class MainNavigationController(private val activity: MessengerActivity) : NavController(activity) {

    val conversationActionDelegate = MainNavigationConversationListActionDelegate(activity)
    val messageActionDelegate = MainNavigationMessageListActionDelegate(activity)

    val navigationView: NavigationView by lazy { activity.findViewById<View>(R.id.navigation_conversations) as NavigationView }
    val drawerLayout: DrawerLayout? by lazy { activity.findViewById<View>(R.id.drawer_layout) as DrawerLayout? }

    var conversationListFragment: ConversationListFragment? = null
    var otherFragment: Fragment? = null
    var returnNavigationId: Int = R.id.navigation_inbox
    var inSettings = false
    var selectedNavigationItemId: Int = R.id.drawer_conversation

    private var optionsMenu: AlertDialog? = null

    fun isConversationListExpanded() = conversationListFragment != null && conversationListFragment!!.isExpanded
    fun isOtherFragmentConvoAndShowing() = otherFragment != null && otherFragment is ConversationListFragment && (otherFragment as ConversationListFragment).isExpanded
    fun getShownConversationList() = when {
        isOtherFragmentConvoAndShowing() -> otherFragment as ConversationListFragment
        else -> conversationListFragment
    }

    fun backPressed(): Boolean {
        val controller = findNavController(activity, R.id.nav_host)
        return when (controller.currentDestination?.id) {
            R.id.navigation_archived,
            R.id.navigation_folder,
            R.id.navigation_private,
            R.id.navigation_scheduled,
            R.id.navigation_unread -> {
                controller.setGraph(R.navigation.navigation_conversations)
                true
            }
            R.id.navigation_message_list -> {
                val fragments = activity.supportFragmentManager.fragments.first().childFragmentManager.fragments
                fragments
                        .filter { it is MessageListFragment && it.onBackPressed() }
                        .forEach { return true }

                if (returnNavigationId != 1 && returnNavigationId != R.id.navigation_inbox) {
                    controller.navigate(returnNavigationId)
                    returnNavigationId = -1
                } else {
                    controller.setGraph(R.navigation.navigation_conversations)
                }
                ActivityUtils.setStatusBarColor(activity, activity.getColor(R.color.statusBarBackground))
                true
            }
            else -> false
        }
    }

    fun findMessageListFragment(): MessageListFragment? =
            activity.supportFragmentManager.findFragmentById(R.id.nav_host) as? MessageListFragment

    fun drawerItemClicked(id: Int): Boolean {
        conversationListFragment?.swipeHelper?.dismissSnackbars()

        when (id) {
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

    fun optionsItemSelected(itemId: Int): Boolean {
        conversationListFragment?.swipeHelper?.dismissSnackbars()

        closeMenu()

        return when (itemId) {
            R.id.drawer_private -> conversationActionDelegate.displayPrivate()
            R.id.drawer_mute_contacts -> conversationActionDelegate.displayBlacklist()
            R.id.drawer_invite -> conversationActionDelegate.displayInviteFriends()
            R.id.drawer_feature_settings -> conversationActionDelegate.displayFeatureSettings()
            R.id.drawer_settings -> conversationActionDelegate.displaySettings()
            R.id.drawer_account -> conversationActionDelegate.displayMyAccount()
            R.id.drawer_help -> conversationActionDelegate.displayHelpAndFeedback()
            R.id.drawer_about -> conversationActionDelegate.displayAbout()
            R.id.drawer_edit_folders -> conversationActionDelegate.displayEditFolders()
            else -> false
        }
    }

    fun openMenu() {
        val layout = LayoutInflater.from(activity).inflate(R.layout.dialog_options_menu, null, false)
        val recyclerView = layout.findViewById<View>(R.id.recycler_view) as RecyclerView
        recyclerView.apply {
            setHasFixedSize(true)
            val noScrollLayoutManager = FixedScrollLinearLayoutManager(activity)
            noScrollLayoutManager.setCanScroll(false)
            layoutManager = noScrollLayoutManager
            adapter = OptionsMenuAdapter(OptionsMenuDataFactory.getOptions(), ::optionsItemSelected)
            addItemDecoration(MiddleDividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        }

        optionsMenu = AlertDialog.Builder(activity)
                .setView(layout)
                .create()
        optionsMenu!!.show()
    }

    fun closeMenu() {
        if (optionsMenu != null) {
            optionsMenu!!.cancel()
        }
    }
}