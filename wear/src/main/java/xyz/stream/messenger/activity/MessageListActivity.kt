package xyz.stream.messenger.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.wear.widget.WearableRecyclerView
import androidx.wear.widget.drawer.WearableActionDrawerView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.drawer.WearableDrawerLayout
import xyz.stream.messenger.R
import xyz.stream.messenger.adapter.WearableMessageListAdapter
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Conversation
import xyz.stream.messenger.shared.data.model.Message
import xyz.stream.messenger.shared.receiver.MessageListUpdatedReceiver
import xyz.stream.messenger.shared.shared_interfaces.IMessageListFragment
import xyz.stream.messenger.shared.util.*
import xyz.klinker.wear.reply.WearableReplyActivity

class MessageListActivity : AppCompatActivity(), IMessageListFragment {

    private val conversation: Conversation? by lazy { DataSource.getConversation(this, intent.getLongExtra(CONVERSATION_ID, -1L)) }

    private val drawerLayout: WearableDrawerLayout by lazy { findViewById<View>(R.id.drawer_layout) as WearableDrawerLayout }
    private val actionDrawer: WearableActionDrawerView by lazy { findViewById<View>(R.id.action_drawer) as WearableActionDrawerView }
    private val recyclerView: WearableRecyclerView by lazy { findViewById<View>(R.id.recycler_view) as WearableRecyclerView }

    private val manager: LinearLayoutManager by lazy { LinearLayoutManager(this) }
    private val adapter: WearableMessageListAdapter by lazy {
        if (Settings.useGlobalThemeColor) {
            WearableMessageListAdapter(this, manager, null, Settings.mainColorSet.color, Settings.mainColorSet.colorAccent, conversation!!.isGroup)
        } else {
            WearableMessageListAdapter(this, manager, null, conversation!!.colors.color, conversation!!.colors.colorAccent, conversation!!.isGroup)
        }
    }

    private var updatedReceiver: MessageListUpdatedReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_message_list)

        initRecycler()
        loadMessages()
        dismissNotification()

        updatedReceiver = MessageListUpdatedReceiver(this)
        registerReceiver(updatedReceiver,
                MessageListUpdatedReceiver.intentFilter)

        if (conversation == null) {
            finish()
            return
        }

        actionDrawer.setBackgroundColor(conversation!!.colors.color)
        actionDrawer.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_close -> {
                    finish()
                    true
                }
                R.id.menu_reply -> {
                    WearableReplyActivity.start(this@MessageListActivity)
                    true
                }
                else -> false
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(updatedReceiver)
        } catch (e: Exception) {
        }

        CursorUtil.closeSilent(adapter.messages)
    }

    private fun initRecycler() {
        manager.stackFromEnd = true

        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
    }

    override fun loadMessages() {
        // doens't really matter here. We are loading everything anyways
        loadMessages(true)
    }

    override fun loadMessages(addedNewMessage: Boolean) {
        Thread {
            val cursor = DataSource.getMessages(this, conversation!!.id)
            runOnUiThread {
                if (adapter.messages == null) {
                    adapter.setCursor(cursor)
                } else {
                    adapter.addMessage(cursor)
                }
            }
        }.start()
    }

    override val conversationId: Long
        get() = conversation!!.id

    override fun setShouldPullDrafts(pull: Boolean) {

    }

    override fun setDismissOnStartup() {

    }

    override fun setConversationUpdateInfo(text: String) {

    }

    private fun dismissNotification() {
        NotificationManagerCompat.from(this).cancel(conversation!!.id.toInt())

        try {
            ApiUtils.dismissNotification(Account.accountId,
                    Account.deviceId, conversation!!.id)
        } catch (e: Error) {
        }

        NotificationUtils.cancelGroupedNotificationWithNoContent(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = WearableReplyActivity.getResultText(data)
        if (result != null) {
            sendMessage(result)
        }
    }

    private fun sendMessage(text: String) {
        val m = Message()
        m.conversationId = conversationId
        m.type = Message.TYPE_SENDING
        m.data = text
        m.timestamp = TimeUtils.now
        m.mimeType = MimeType.TEXT_PLAIN
        m.read = true
        m.seen = true
        m.from = null
        m.color = null
        m.sentDeviceId = if (Account.exists()) java.lang.Long.parseLong(Account.deviceId!!) else -1L
        m.simPhoneNumber = if (conversation!!.simSubscriptionId != null)
            DualSimUtils.getPhoneNumberFromSimSubscription(conversation!!.simSubscriptionId!!)
        else
            null

        if (text.isNotEmpty()) {
            DataSource.insertMessage(this, m, m.conversationId)
            loadMessages()

            SendUtils(conversation!!.simSubscriptionId).send(this, m.data!!, conversation!!.phoneNumbers!!, null, MimeType.TEXT_PLAIN)
        }
    }

    companion object {

        private const val CONVERSATION_ID = "conversation_id"

        fun startActivity(context: Context, conversationId: Long) {
            val intent = Intent(context, MessageListActivity::class.java)
            intent.putExtra(CONVERSATION_ID, conversationId)

            context.startActivity(intent)
        }
    }

}
