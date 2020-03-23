package xyz.stream.messenger.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.main.MainColorController
import xyz.stream.messenger.fragment.message.MessageInstanceManager
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.util.ActivityUtils

open class NoLimitMessageListActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_full_conversation)

        var conversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1)
        if (conversationId == -1L) {
            conversationId = try {
                intent.data!!.lastPathSegment!!.toLong()
            } catch (e: Exception) {
                -1L
            }
        }

        val conversation = DataSource.getConversation(this, conversationId)
        if (conversation == null) {
            finish()
            return
        }

        var view = findViewById<View>(android.R.id.content).rootView
        findNavController(view).navigate(R.id.action_global_message_list, MessageInstanceManager.newInstance(conversation, -1, false).arguments)

        ActivityUtils.setStatusBarColor(this, conversation.colors.colorDark, conversation.colors.color)
        ActivityUtils.setTaskDescription(this, conversation.title!!, conversation.colors.color)
        MainColorController(this).configureNavigationBarColor()
    }

    companion object {
        private val EXTRA_CONVERSATION_ID = "conversation_id"

        fun start(context: Context?, conversationId: Long) {
            val intent = Intent(context, NoLimitMessageListActivity::class.java)
            intent.putExtra(EXTRA_CONVERSATION_ID, conversationId)

            context?.startActivity(intent)
        }
    }
}
