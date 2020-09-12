package com.stream_suite.link.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stream_suite.link.R
import com.stream_suite.link.activity.main.MainColorController
import com.stream_suite.link.fragment.message.MessageInstanceManager
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.util.ActivityUtils
import java.lang.Exception

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

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.message_list_container, MessageInstanceManager.newInstance(conversation, -1, false))
                .commit()

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
