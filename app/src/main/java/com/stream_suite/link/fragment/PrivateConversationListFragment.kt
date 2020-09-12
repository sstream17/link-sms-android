package com.stream_suite.link.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.navigation.NavigationView

import com.stream_suite.link.R
import com.stream_suite.link.activity.MessengerActivity
import com.stream_suite.link.adapter.view_holder.ConversationViewHolder
import com.stream_suite.link.fragment.conversation.ConversationListFragment
import com.stream_suite.link.shared.data.Settings

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
                        .setNegativeButton(R.string.menu_feature_settings) { _, _ ->
                            if (fragmentActivity is MessengerActivity) {
                                fragmentActivity.clickNavigationItem(R.id.drawer_feature_settings)
                            }
                        }.show()

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
            val navView = activity?.findViewById<View>(R.id.navigation_view) as NavigationView?
            navView?.menu?.findItem(R.id.navigation_inbox)?.isChecked = true

            activity?.title = getString(R.string.app_title)
            (activity as MessengerActivity).displayConversations()
        }

        return true
    }

    override fun onConversationContracted(viewHolder: ConversationViewHolder) {
        super.onConversationContracted(viewHolder)

        val navView = activity?.findViewById<View>(R.id.navigation_view) as NavigationView?
        navView?.menu?.findItem(R.id.drawer_private)?.isChecked = true
    }
}
