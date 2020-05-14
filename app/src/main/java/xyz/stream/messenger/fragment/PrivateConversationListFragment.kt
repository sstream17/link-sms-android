package xyz.stream.messenger.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.navigation.NavigationView

import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.adapter.view_holder.ConversationViewHolder
import xyz.stream.messenger.fragment.conversation.ConversationListFragment
import xyz.stream.messenger.shared.data.Settings

class PrivateConversationListFragment : ConversationListFragment() {

    override fun onCreateView(inflater: LayoutInflater, viewGroup: ViewGroup?, bundle: Bundle?): View? {
        val view = super.onCreateView(inflater, viewGroup, bundle)

        val fragmentActivity = activity
        if (fragmentActivity != null && Settings.privateConversationsPasscode.isNullOrBlank()) {
            val prefs = Settings.getSharedPrefs(fragmentActivity)
            if (prefs.getBoolean("private_conversation_security_disclainer", true)) {
                AlertDialog.Builder(fragmentActivity)
                        .setMessage(R.string.enable_passcode_disclaimer)
                        .setPositiveButton(android.R.string.ok, { _, _ -> })
                        .show()

                prefs.edit().putBoolean("private_conversation_security_disclainer", false).commit()
            }
        }


        return view
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun noConversationsText() = getString(R.string.no_private_messages_description)

    // always consume the back event and send us to the conversation list
    override fun onBackPressed(): Boolean {
        if (!super.onBackPressed()) {
            activity?.title = getString(R.string.app_title)
            (activity as MessengerActivity).displayConversations()
        }

        return true
    }
}
