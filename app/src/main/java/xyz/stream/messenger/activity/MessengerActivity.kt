/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.stream.messenger.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.compose.ComposeActivity
import xyz.stream.messenger.activity.main.*
import xyz.stream.messenger.fragment.PrivateConversationListFragment
import xyz.stream.messenger.fragment.conversation.MessageListManager.Companion.ARG_CONVERSATION_TO_OPEN_ID
import xyz.stream.messenger.fragment.settings.MyAccountFragment
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.databinding.ActivityMainBinding
import xyz.stream.messenger.shared.service.notification.NotificationConstants
import xyz.stream.messenger.shared.util.*
import xyz.stream.messenger.shared.util.ActivityUtils.setStatusBarColor
import xyz.stream.messenger.shared.view.search.PersistentSearchView
import xyz.stream.messenger.shared.view.search.SearchLayout
import xyz.stream.messenger.shared.widget.MessengerAppWidgetProvider


/**
 * Main entry point to the app. This will serve for setting up the drawer view, finding
 * conversations and displaying things on the screen to get the user started.
 */
class MessengerActivity : AppCompatActivity() {

    val navController = MainNavigationController(this)
    val accountController = MainAccountController(this)
    val intentHandler = MainIntentHandler(this)
    val searchHelper = MainSearchHelper(this)
    val snoozeController = SnoozeController(this)
    val insetController = MainInsetController(this)
    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)
    private val colorController = MainColorController(this)
    private val startDelegate = MainOnStartDelegate(this)
    private val permissionHelper = MainPermissionHelper(this)
    private val resultHandler = MainResultHandler(this)

    val fab: FloatingActionButton by lazy { findViewById<View>(R.id.fab) as FloatingActionButton }
    val snackbarContainer: FrameLayout by lazy { findViewById<FrameLayout>(R.id.snackbar_container) }
    val searchBar: PersistentSearchView by lazy { findViewById<PersistentSearchView>(R.id.search_view) }
    lateinit var bottomNav: BottomNavigationView
    private val content: View by lazy { findViewById<View>(R.id.nav_host) }
    private val searchLayout: SearchLayout by lazy { findViewById<View>(R.id.search_bar_container) as SearchLayout }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UpdateUtils(this).checkForUpdate()

        binding.apply {
            val navController = Navigation.findNavController(this@MessengerActivity, R.id.nav_host)
            navController.setGraph(R.navigation.navigation_conversations, intent.extras)
            navView.setupWithNavController(navController)

            bottomNav = navView

            // Hide bottom nav on screens which don't require it
            lifecycleScope.launchWhenResumed {
                navController.addOnDestinationChangedListener { _, destination, args ->
                    searchLayout.invalidateScrollRanges()
                    when (destination.id) {
                        R.id.navigation_inbox, R.id.navigation_unread, R.id.navigation_archived, R.id.navigation_scheduled -> {
                            val convoId = args?.getLong(ARG_CONVERSATION_TO_OPEN_ID) ?: -1L
                            if (convoId == -1L || convoId == 0L) {
                                searchView.show()
                                navView.show()
                                fab.show()
                            }
                        }
                        else -> {
                            searchView.hide()
                            navView.hide()
                            fab.hide()
                        }
                    }
                }
            }
        }

        initToolbar()
        fab.setOnClickListener { startActivity(Intent(applicationContext, ComposeActivity::class.java)) }

        colorController.configureGlobalColors()
        colorController.configureNavigationBarColor()
        colorController.configureProfilePictureColor(null)
        intentHandler.dismissIfFromNotification()
        intentHandler.displayPrivateFromNotification()
        navController.conversationActionDelegate.displayConversations(savedInstanceState)
        accountController.startIntroOrLogin(savedInstanceState)
        permissionHelper.requestDefaultSmsApp()

        val content = findViewById<View>(R.id.nav_host)
        content.post {
            AnimationUtils.conversationListSize = content.height
        }

        val accountImage = findViewById<FrameLayout>(R.id.account_image_holder)
        accountImage.setOnClickListener {
            navController.openMenu()
        }
    }

    public override fun onStart() {
        super.onStart()

        PromotionUtils(this).checkPromotions {
            MyAccountFragment.openTrialUpgradePreference = true
        }

        UnreadBadger(this).clearCount()

        colorController.colorActivity()
        navController.initOptionsMenu()
        intentHandler.displayAccount()
        intentHandler.handleShortcutIntent(intent)
        accountController.listenForFullRefreshes()
        accountController.refreshAccountToken()
        searchHelper.setup()

        startDelegate.run()

        try {
            if (navController.otherFragment is PrivateConversationListFragment) {
                if (navController.isOtherFragmentConvoAndShowing()) {
                    navController.getShownConversationList()?.expandedItem?.itemView?.performClick()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentHandler.newIntent(intent)
    }

    public override fun onResume() {
        super.onResume()

        val messageListFragment = navController.findMessageListFragment()
        if (messageListFragment != null) {
            NotificationConstants.CONVERSATION_ID_OPEN = messageListFragment.conversationId
        }
    }

    public override fun onPause() {
        super.onPause()

        if (navController.conversationListFragment != null) {
            navController.conversationListFragment!!.swipeHelper.dismissSnackbars()
        }

        NotificationConstants.CONVERSATION_ID_OPEN = 0L
    }

    public override fun onStop() {
        super.onStop()

        MessengerAppWidgetProvider.refreshWidget(this)
        accountController.stopListeningForRefreshes()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(content.windowToken, 0)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        var outState = intentHandler.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        accountController.stopListeningForDownloads()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        try {
            if (searchHelper.closeSearch()) {
            } else if (!navController.backPressed()) {
                super.onBackPressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            Settings.forceUpdate(this)
            colorController.colorActivity()
            colorController.configureGlobalColors()

            resultHandler.handle(requestCode, resultCode, data)
            accountController.startLoad(requestCode)
            super.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun displayConversations() = navController.conversationActionDelegate.displayConversations()

    private fun initToolbar() {
        setStatusBarColor(this, getColor(R.color.statusBarBackground))
        val actionBar = supportActionBar

        if (actionBar != null && !resources.getBoolean(R.bool.pin_drawer)) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
}
