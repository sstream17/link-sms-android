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

package xyz.stream.messenger.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.activity.compose.ComposeActivity
import xyz.stream.messenger.adapter.ScheduledMessagesAdapter
import xyz.stream.messenger.fragment.bottom_sheet.EditScheduledMessageFragment
import xyz.stream.messenger.shared.MessengerActivityExtras
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.MimeType
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.ScheduledMessage
import xyz.stream.messenger.shared.service.jobs.ScheduledMessageJob
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.PhoneNumberUtils
import xyz.stream.messenger.shared.util.SmsMmsUtils
import xyz.stream.messenger.shared.util.listener.BackPressedListener
import xyz.stream.messenger.shared.util.listener.ScheduledMessageClickListener

@Suppress("DEPRECATION")
/**
 * Fragment for displaying scheduled messages.
 */
class ScheduledMessagesFragment : Fragment(), ScheduledMessageClickListener {

    private val fragmentActivity: FragmentActivity? by lazy { activity }

    private var conversationMatcher: String? = null

    val list: RecyclerView by lazy { view!!.findViewById<View>(R.id.list) as RecyclerView }
    val fab: FloatingActionButton by lazy { view!!.findViewById<View>(R.id.fab) as FloatingActionButton }
    private val progress: ProgressBar? by lazy { view?.findViewById<View>(R.id.progress) as ProgressBar? }
    private val emptyView: View by lazy { view!!.findViewById<View>(R.id.empty_view) }

    private val scheduledMessageSent = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            loadMessages()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_schedule_messages, parent, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.layoutManager = LinearLayoutManager(fragmentActivity)
        fab.setOnClickListener {
            val intent = Intent(context, ComposeActivity::class.java)
            intent.putExtra(MessengerActivityExtras.EXTRA_SHOULD_SCHEDULE_MESSAGE, true)
            startActivity(intent)
        }

        val messengerActivity = fragmentActivity
        if (messengerActivity is MessengerActivity) {
            messengerActivity.insetController.modifyScheduledMessageElements(this)
        }

        emptyView.setBackgroundColor(Settings.mainColorSet.colorLight)
        fab.backgroundTintList = ColorStateList.valueOf(Settings.mainColorSet.colorAccent)
        ColorUtils.changeRecyclerOverscrollColors(list, Settings.mainColorSet.color)

        loadMessages()

        val arguments = arguments
        if (arguments?.getString(ARG_TITLE) != null && arguments.getString(ARG_PHONE_NUMBERS) != null) {
            val message = ScheduledMessage()
            message.to = arguments.getString(ARG_PHONE_NUMBERS)
            message.title = arguments.getString(ARG_TITLE)
            message.data = arguments.getString(ARG_DATA)
            message.mimeType = MimeType.TEXT_PLAIN
        }

        if (arguments?.containsKey(ARG_CONVERSATION_MATCHER) == true) {
            conversationMatcher = arguments.getString(ARG_CONVERSATION_MATCHER)

            // NOTE: I am not disabling the FAB because it doesn't work. It does in fact allow creating a scheduled message,
            // but it doesn't have the right colors, the position covers messages, and it creates the message for any conversation,
            // not just the current conversation.
            fab.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        fragmentActivity?.registerReceiver(scheduledMessageSent,
                IntentFilter(ScheduledMessageJob.BROADCAST_SCHEDULED_SENT))
    }

    override fun onStop() {
        super.onStop()

        try {
            fragmentActivity?.unregisterReceiver(scheduledMessageSent)
        } catch (e: Exception) {
        }

        ScheduledMessageJob.scheduleNextRun(fragmentActivity!!)
    }

    fun loadMessages() {
        Thread {
            try {
                if (fragmentActivity != null) {
                    val messages = DataSource.getScheduledMessagesAsList(fragmentActivity!!).filter {
                        if (conversationMatcher == null) true else conversationMatcher == SmsMmsUtils.createIdMatcher(PhoneNumberUtils.clearFormattingAndStripStandardReplacements(it.to!!)).default
                    }
                    fragmentActivity!!.runOnUiThread { setMessages(messages) }
                }
            } catch (e: Exception) {
            }
        }.start()
    }

    private fun setMessages(messages: List<ScheduledMessage>) {
        progress?.visibility = View.GONE
        list.adapter = ScheduledMessagesAdapter(messages, this)

        if (list.adapter!!.itemCount == 0 && conversationMatcher == null) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    override fun onClick(message: ScheduledMessage) {
        if (message.mimeType != MimeType.TEXT_PLAIN && fragmentActivity != null) {
            AlertDialog.Builder(fragmentActivity!!)
                    .setMessage(R.string.remove_scheduled_message)
                    .setPositiveButton(R.string.api_yes) { _, _ ->
                        DataSource.deleteScheduledMessage(fragmentActivity!!, message.id)
                        loadMessages()
                    }.setNegativeButton(R.string.api_no) { _, _ -> }
                    .show()
        } else {
            val fragment = EditScheduledMessageFragment()
            fragment.setMessage(message)
            fragment.setFragment(this)
            fragment.show(fragmentActivity?.supportFragmentManager!!, "")
        }
    }

    companion object {

        private const val ARG_TITLE = "title"
        private const val ARG_PHONE_NUMBERS = "phone_numbers"
        private const val ARG_DATA = "data"
        private const val ARG_CONVERSATION_MATCHER = "conversation_phone_matcher"

        fun newInstance(): ScheduledMessagesFragment {
            return ScheduledMessagesFragment()
        }

        fun newInstance(conversationMatcher: String): ScheduledMessagesFragment {
            val args = Bundle()
            args.putString(ARG_CONVERSATION_MATCHER, conversationMatcher)

            val fragment = ScheduledMessagesFragment()
            fragment.arguments = args

            return fragment
        }

        fun newInstance(title: String, phoneNumbers: String, text: String): ScheduledMessagesFragment {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_PHONE_NUMBERS, phoneNumbers)
            args.putString(ARG_DATA, text)

            val fragment = ScheduledMessagesFragment()
            fragment.arguments = args

            return fragment
        }
    }
}
