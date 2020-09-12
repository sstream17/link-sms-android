package com.stream_suite.link.fragment.message.send

import android.os.Build
import android.view.View
import android.widget.TextView
import com.stream_suite.link.R
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.fragment.message.MessageListFragment
import com.stream_suite.link.shared.util.MessageCountHelper

class MessageCounterCalculator(private val fragment: MessageListFragment) {

    private val messageEntry: TextView by lazy { fragment.rootView!!.findViewById<View>(R.id.message_entry) as TextView }
    private val counter: TextView by lazy { fragment.rootView!!.findViewById<View>(R.id.text_counter) as TextView }

    fun updateCounterText() {
        val text = messageEntry.text.toString()
        counter.text = when {
            ignoreCounterText() || text.isEmpty() -> null
            fragment.attachManager.currentlyAttached.isNotEmpty() -> null
            fragment.argManager.isGroup -> null
            else -> {
                MessageCountHelper.getMessageCounterText(text)
            }
        }
    }

    private fun ignoreCounterText(): Boolean {
        // they seem to have issues, where some dialog pops up, asking which SIM to send from
        // happens when the user is running LineageOS
        return !Account.primary && (Build.MODEL == "Nexus 9" || Build.MANUFACTURER.toLowerCase() == "oneplus" ||
                Build.MANUFACTURER.toLowerCase() == "sony" || Build.MANUFACTURER.toLowerCase() == "xiaomi" ||
                Build.MANUFACTURER.toLowerCase() == "samsung" || Build.MANUFACTURER.toLowerCase() == "lge" ||
                Build.MODEL.toLowerCase().contains("kindle") || Build.MODEL.toLowerCase().contains("YT-X703F"))
    }
}