package xyz.stream.messenger.activity.main

import android.app.Activity
import android.content.Intent
import android.os.Handler
import androidx.fragment.app.Fragment
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.fragment.PrivateConversationListFragment
import xyz.stream.messenger.fragment.message.attach.AttachmentListener
import xyz.stream.messenger.activity.passcode.PasscodeVerificationActivity
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.util.TimeUtils

class MainResultHandler(private val activity: MessengerActivity) {

    fun handle(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PasscodeVerificationActivity.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                activity.navController.conversationActionDelegate.displayFragmentWithBackStack(PrivateConversationListFragment(), R.id.navigation_private)
                Settings.setValue(activity, activity.getString(R.string.pref_private_conversation_passcode_last_entry), TimeUtils.now)
            } else {
                // TODO: Possibly return to Inbox fragment
            }

            return
        }

        var fragment: Fragment? = activity.supportFragmentManager.findFragmentById(R.id.message_list_container)
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data)
        } else {
            if (requestCode == AttachmentListener.RESULT_CAPTURE_IMAGE_REQUEST) {
                Handler().postDelayed({
                    val messageList = activity.supportFragmentManager.findFragmentById(R.id.message_list_container)
                    messageList?.onActivityResult(requestCode, resultCode, data)
                }, 1000)
            }

            fragment = activity.supportFragmentManager.findFragmentById(R.id.conversation_list_container)
            fragment?.onActivityResult(requestCode, resultCode, data)
        }
    }
}