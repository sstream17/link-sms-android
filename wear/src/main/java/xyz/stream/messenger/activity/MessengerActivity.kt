package xyz.stream.messenger.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import xyz.stream.messenger.R
import xyz.stream.messenger.adapter.WearableConversationListAdapter
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.shared.MessengerActivityExtras
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.receiver.ConversationListUpdatedReceiver
import xyz.stream.messenger.shared.shared_interfaces.IConversationListFragment
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.PermissionsUtils
import xyz.stream.messenger.shared.util.TimeUtils

class MessengerActivity : AppCompatActivity(), IConversationListFragment {


    private val recyclerView: WearableRecyclerView by lazy { findViewById<View>(R.id.recycler_view) as WearableRecyclerView }
    private var updatedReceiver: ConversationListUpdatedReceiver? = null

    override val adapter: WearableConversationListAdapter by lazy { WearableConversationListAdapter(emptyList()) }

    override val isFragmentAdded: Boolean
        get() = true

    override val expandedId: Long
        get() = -1

    override fun checkEmptyViewDisplay() { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_messenger)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadConversations()

        if (Account.accountId == null) {
            startActivityForResult(Intent(this, InitialLoadWearActivity::class.java), REQUEST_LOGIN)
            finish()
            return
        }

        displayConversation()

        updatedReceiver = ConversationListUpdatedReceiver(this)
        registerReceiver(updatedReceiver, ConversationListUpdatedReceiver.intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (updatedReceiver != null) {
            unregisterReceiver(updatedReceiver)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val convoId = intent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)
        if (convoId != -1L) {
            getIntent().putExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, convoId)
            displayConversation()
        }
    }

    override fun onStart() {
        super.onStart()

        requestPermissions()
        ColorUtils.checkBlackBackground(this)
        TimeUtils.setupNightTheme(this)
    }

    override fun onActivityResult(requestCode: Int, responseCode: Int, data: Intent?) {
        if (requestCode == REQUEST_LOGIN) {
            overridePendingTransition(0,0)
            startActivity(Intent(this, MessengerActivity::class.java))

            overridePendingTransition(0,0)
            finish()
        }
    }

    private fun requestPermissions() {
        if (PermissionsUtils.checkRequestMainPermissions(this)) {
            PermissionsUtils.startMainPermissionRequest(this)
        }
    }

    private fun loadConversations() {
        Thread {
            val conversations = DataSource.getUnarchivedConversationsAsList(this)
            runOnUiThread {
                adapter.conversations = conversations.toMutableList()
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    private fun displayConversation() {
        val convoId = intent.getLongExtra(MessengerActivityExtras.EXTRA_CONVERSATION_ID, -1L)

        if (convoId != -1L) {
            ConversationListUpdatedReceiver.sendBroadcast(this, convoId, null, true)
            MessageListActivity.startActivity(this, convoId)
        }
    }

    companion object {
        private val REQUEST_LOGIN = 1
    }
}
