package com.stream_suite.link.activity.notification

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.view.View

import com.stream_suite.link.R
import com.stream_suite.link.shared.service.ReplyService

class MarshmallowReplyActivity : AppCompatActivity() {

    private val animators = ReplyAnimators(this)
    private val dataProvider = ReplyDataProvider(this)
    private val wearableHandler = ReplyWearableHandler(this)
    private val layoutInitializer = ReplyLayoutInitializer(this, dataProvider, animators)
    private val sender = ReplySender(this, dataProvider, animators)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_reply)

        overridePendingTransition(0, 0)

        if (wearableHandler.reply() || dataProvider.conversation == null) {
            finish()
            return
        }

        dataProvider.queryMessageHistory()

        sender.setupViews()
        layoutInitializer.setupBackgroundComponents()
        layoutInitializer.showContactImage()

        animators.alphaIn(findViewById<View>(R.id.dim_background), 300, 0)
        findViewById<View>(android.R.id.content).post({ layoutInitializer.displayMessages() })

        sender.requestFocus()
        dataProvider.dismissNotification()
    }

    override fun onBackPressed() {
        sender.saveDraft()
        animators.slideOut()
        Handler().postDelayed({
            finish()
            overridePendingTransition(0, android.R.anim.fade_out)
        }, 300)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getIntent().putExtra(ReplyService.EXTRA_CONVERSATION_ID, intent.getLongExtra(ReplyService.EXTRA_CONVERSATION_ID, -1))
        recreate()
    }
}
