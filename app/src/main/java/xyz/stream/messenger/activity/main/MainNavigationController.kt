package xyz.stream.messenger.activity.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import xyz.stream.messenger.shared.util.show

@Suppress("DEPRECATION")
class MainNavigationController(private val activity: MessengerActivity)
    : BottomNavigationView.OnNavigationItemSelectedListener {

    val conversationActionDelegate = MainNavigationConversationListActionDelegate(activity)
    val messageActionDelegate = MainNavigationMessageListActionDelegate(activity)

    val navigationView: BottomNavigationView by lazy { activity.findViewById<View>(R.id.nav_view) as BottomNavigationView }

    var conversationListFragment: ConversationListFragment? = null
    var otherFragment: Fragment? = null
    var inSettings = false
    var selectedNavigationItemId: Int = R.id.navigation_inbox

    fun isConversationListExpanded() = conversationListFragment != null && conversationListFragment!!.isExpanded
    fun isOtherFragmentConvoAndShowing() = otherFragment != null && otherFragment is ConversationListFragment && (otherFragment as ConversationListFragment).isExpanded
    fun getShownConversationList() = when {
        isOtherFragmentConvoAndShowing() -> otherFragment as ConversationListFragment
        else -> conversationListFragment
    }

    fun initDrawer() {
        navigationView.setOnNavigationItemSelectedListener(this)
        navigationView.postDelayed({
            try {
                if (Account.exists()) {
                    (activity.findViewById<View>(R.id.drawer_header_my_name) as TextView).text = Account.myName
                }

                (activity.findViewById<View>(R.id.drawer_header_my_phone_number) as TextView).text =
                        PhoneNumberUtils.format(PhoneNumberUtils.getMyPhoneNumber(activity))

                if (!ColorUtils.isColorDark(Settings.mainColorSet.colorDark)) {
                    (activity.findViewById<View>(R.id.drawer_header_my_name) as TextView)
                            .setTextColor(activity.resources.getColor(R.color.lightToolbarTextColor))
                    (activity.findViewById<View>(R.id.drawer_header_my_phone_number) as TextView)
                            .setTextColor(activity.resources.getColor(R.color.lightToolbarTextColor))
                }

                // change the text to
                if (!Account.exists()) {
                    navigationView.menu.findItem(R.id.drawer_account).setTitle(R.string.menu_device_texting)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            activity.snoozeController.initSnooze()
        }, 300)
    }

    fun initToolbarTitleClick() {
        activity.toolbar.setOnClickListener {
            val otherFrag = otherFragment
            val fragment = when {
                conversationListFragment != null -> conversationListFragment
                otherFrag is ConversationListFragment -> otherFrag
                else -> return@setOnClickListener
            }

            fragment?.recyclerView?.smoothScrollToPosition(0)
        }
    }

    fun backPressed(): Boolean {
        val fragments = activity.supportFragmentManager.fragments

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
                navigationView.show()
                activity.toolbar.alignTitleCenter()
                return true
            }
            inSettings -> {
                onNavigationItemSelected(R.id.navigation_inbox)
                return true
            }
            else -> return false
        }
    }

    fun findMessageListFragment(): MessageListFragment? =
            activity.supportFragmentManager.findFragmentById(R.id.message_list_container) as? MessageListFragment

    fun drawerItemClicked(id: Int): Boolean {
        conversationListFragment?.swipeHelper?.dismissSnackbars()

        when (id) {
            android.R.id.home -> {
                activity.onBackPressed()
                return false
            }
            R.id.navigation_inbox -> return conversationActionDelegate.displayConversations()
            R.id.navigation_unread -> return conversationActionDelegate.displayUnread()
            R.id.navigation_compose -> return conversationActionDelegate.displayCompose()
            R.id.navigation_scheduled -> return conversationActionDelegate.displayScheduledMessages()
            R.id.drawer_archived -> return conversationActionDelegate.displayArchived()
            R.id.drawer_private -> return conversationActionDelegate.displayPrivate()
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

            else -> return false
        }
    }

    fun onNavigationItemSelected(itemId: Int) {
        val item = navigationView.menu.findItem(itemId)
        if (item != null) {
            onNavigationItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectedNavigationItemId = item.itemId

        if (ApiDownloadService.IS_RUNNING) {
            return true
        }

        val isHomeDestination = drawerItemClicked(item.itemId)

        when (item.itemId) {
            // Set app name as title for main destination
            R.id.navigation_inbox -> activity.setTitle(R.string.app_title)
            // Ignore changing title for following destinations
            R.id.navigation_compose, R.id.drawer_settings, android.R.id.home -> {}
            // Set destination title as title
            else -> activity.title = StringUtils.titleize(item.title.toString())
        }

        return  isHomeDestination
    }
}