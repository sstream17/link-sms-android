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

package xyz.stream.messenger.fragment.message

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sgottard.sofa.ContentFragment
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerTvActivity
import xyz.stream.messenger.activity.compose.ShareData
import xyz.stream.messenger.fragment.message.attach.AttachmentInitializer
import xyz.stream.messenger.fragment.message.attach.AttachmentListener
import xyz.stream.messenger.fragment.message.attach.AttachmentManager
import xyz.stream.messenger.fragment.message.load.MessageListLoader
import xyz.stream.messenger.fragment.message.load.ViewInitializerDeferred
import xyz.stream.messenger.fragment.message.load.ViewInitializerNonDeferred
import xyz.stream.messenger.fragment.message.send.MessageCounterCalculator
import xyz.stream.messenger.fragment.message.send.PermissionHelper
import xyz.stream.messenger.fragment.message.send.SendMessageManager
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.ScheduledMessage
import xyz.stream.messenger.shared.receiver.MessageListUpdatedReceiver
import xyz.stream.messenger.shared.service.notification.NotificationConstants
import xyz.stream.messenger.shared.shared_interfaces.IMessageListFragment
import xyz.stream.messenger.shared.util.*
import xyz.stream.messenger.utils.multi_select.MessageMultiSelectDelegate
import java.util.*

/**
 * Fragment for displaying messages for a certain conversation.
 */
class MessageListFragment : Fragment(), ContentFragment, IMessageListFragment {

    private val fragmentActivity: FragmentActivity? by lazy { activity }

    val argManager: MessageInstanceManager by lazy { MessageInstanceManager(this) }
    val attachManager: AttachmentManager by lazy { AttachmentManager(this) }
    val attachInitializer: AttachmentInitializer by lazy { AttachmentInitializer(this) }
    val attachListener: AttachmentListener by lazy { AttachmentListener(this) }
    val draftManager: DraftManager by lazy { DraftManager(this) }
    val counterCalculator: MessageCounterCalculator by lazy { MessageCounterCalculator(this) }
    val sendManager: SendMessageManager by lazy { SendMessageManager(this) }
    val messageLoader: MessageListLoader by lazy { MessageListLoader(this) }
    val notificationManager: MessageListNotificationManager by lazy { MessageListNotificationManager(this) }
    val smartReplyManager: SmartReplyManager by lazy { SmartReplyManager(this) }
    private val permissionHelper = PermissionHelper(this)
    val nonDeferredInitializer: ViewInitializerNonDeferred by lazy { ViewInitializerNonDeferred(this) }
    private val deferredInitializer: ViewInitializerDeferred by lazy { ViewInitializerDeferred(this) }
    val multiSelect: MessageMultiSelectDelegate by lazy { MessageMultiSelectDelegate(this) }
    val searchHelper: MessageSearchHelper by lazy { MessageSearchHelper(this) }

    var rootView: View? = null

    private var updatedReceiver: MessageListUpdatedReceiver? = null
    private var detailsChoiceDialog: AlertDialog? = null

    private var extraMarginTop = 0
    private var extraMarginLeft = 0

    private var scheduledMessage: ScheduledMessage = ScheduledMessage()
    private var imageData: ShareData? = null
    private var scheduledMessageCalendar: Calendar? = null

    override val conversationId: Long
        get() = argManager.conversationId

    val isDragging: Boolean
        get() = deferredInitializer.dragDismissFrameLayout.isDragging
    val isRecyclerScrolling: Boolean
        get() = messageLoader.messageList.scrollState != RecyclerView.SCROLL_STATE_IDLE

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, bundle: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_message_list, parent, false)

        if (!isAdded) {
            return rootView!!
        }

        nonDeferredInitializer.init(bundle)

        AnimationUtils.animateConversationPeripheralIn(rootView!!.findViewById(R.id.app_bar_layout))
        AnimationUtils.animateConversationPeripheralIn(rootView!!.findViewById(R.id.send_bar))

        val deferredTime = if (activity is MessengerTvActivity) 0L
        else (AnimationUtils.EXPAND_CONVERSATION_DURATION + 25).toLong()

        Handler().postDelayed({
            if (!isAdded) {
                return@postDelayed
            }

            deferredInitializer.init()
            messageLoader.loadMessages()

            notificationManager.dismissNotification = true
            notificationManager.dismissNotification()
        }, deferredTime)

        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updatedReceiver = MessageListUpdatedReceiver(this)
        fragmentActivity?.registerReceiver(updatedReceiver,
                MessageListUpdatedReceiver.intentFilter)

        if (extraMarginLeft != 0 || extraMarginTop != 0) {
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.marginStart = extraMarginLeft
            view.invalidate()
        }
    }

    override fun onStart() {
        super.onStart()
        notificationManager.onStart()

        Handler().postDelayed({
            if (fragmentActivity != null) Thread { DataSource.readConversation(fragmentActivity!!, conversationId) }.start()
        }, (AnimationUtils.EXPAND_CONVERSATION_DURATION + 50).toLong())
    }

    override fun onResume() {
        super.onResume()
        NotificationConstants.CONVERSATION_ID_OPEN = conversationId
    }

    override fun onPause() {
        super.onPause()
        NotificationConstants.CONVERSATION_ID_OPEN = 0L
    }

    override fun onStop() {
        super.onStop()
        notificationManager.dismissNotification = false

        Handler().postDelayed({
            if (fragmentActivity != null) Thread { DataSource.readConversation(fragmentActivity!!, conversationId) }.start()
        }, (AnimationUtils.EXPAND_CONVERSATION_DURATION + 50).toLong())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        draftManager.createDrafts()
        sendManager.sendOnFragmentDestroyed()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (updatedReceiver != null) {
            fragmentActivity?.unregisterReceiver(updatedReceiver)
            updatedReceiver = null
        }

        draftManager.createDrafts(scheduledMessage = scheduledMessage)
        multiSelect.clearActionMode()
    }

    override fun onDetach() {
        super.onDetach()
        CursorUtil.closeSilent(messageLoader.adapter?.messages)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (!permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        attachListener.onActivityResult(requestCode, resultCode, data)
    }

    override fun getFocusRootView() = try {
            messageLoader.messageList.getChildAt(messageLoader.messageList.childCount - 1)
        } catch (e: NullPointerException) { null }

    override fun setExtraMargin(marginTop: Int, marginLeft: Int) {
        this.extraMarginTop = marginTop
        this.extraMarginLeft = marginLeft
    }

    override fun isScrolling() = false
    override fun setConversationUpdateInfo(text: String) { messageLoader.informationUpdater.setConversationUpdateInfo(text) }
    override fun setDismissOnStartup() { notificationManager.dismissOnStartup = true }
    override fun setShouldPullDrafts(pull: Boolean) { draftManager.pullDrafts = pull }
    override fun loadMessages() { messageLoader.loadMessages(false) }
    override fun loadMessages(addedNewMessage: Boolean) { messageLoader.loadMessages(addedNewMessage) }
    fun resendMessage(originalMessageId: Long, text: String) { sendManager.resendMessage(originalMessageId, text) }
    fun setDetailsChoiceDialog(dialog: AlertDialog) { this.detailsChoiceDialog = dialog }

    fun onBackPressed(): Boolean {
        dismissDetailsChoiceDialog()

        /*if (searchHelper.closeSearch()) {
            return true
        }*/

        if (attachManager.backPressed()) {
            return true
        }

        sendManager.sendDelayedMessage()
        if (updatedReceiver != null) {
            fragmentActivity?.unregisterReceiver(updatedReceiver)
            updatedReceiver = null
        }

        return false
    }

    fun dismissKeyboard() {
        try {
            val imm = fragmentActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(fragmentActivity?.findViewById<View>(android.R.id.content)?.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun startSchedulingMessage(scheduleImmediately: Boolean = false) {
        scheduledMessage = ScheduledMessage()
        scheduledMessage.timestamp = TimeUtils.now
        displayScheduleDialog(scheduledMessage, scheduleImmediately = scheduleImmediately)
    }

    private fun dismissDetailsChoiceDialog() {
        if (detailsChoiceDialog != null && detailsChoiceDialog!!.isShowing) {
            detailsChoiceDialog!!.dismiss()
            detailsChoiceDialog = null
        }
    }

    private fun updateTimeInputs(newDate: Calendar, date: TextView, time: TextView){
        date.text = DateFormat.format("MMM dd, yyyy", newDate)
        val timeFormat = if (DateFormat.is24HourFormat(fragmentActivity)) "HH:mm" else "hh:mm a"
        time.text = if (newDate.timeInMillis < TimeUtils.now + 10 * TimeUtils.SECOND) {
            "Now"
        } else {
            DateFormat.format(timeFormat, newDate)
        }
    }

    private fun displayScheduleDialog(message: ScheduledMessage, isEdit: Boolean = false, scheduleImmediately: Boolean = false){
        val layout = LayoutInflater.from(fragmentActivity).inflate(R.layout.dialog_scheduled_message, null, false)
        val date = layout.findViewById<TextView>(R.id.schedule_date)
        val time = layout.findViewById<TextView>(R.id.schedule_time)
        val repeat = layout.findViewById<Spinner>(R.id.repeat_interval)

        scheduledMessageCalendar = Calendar.getInstance()
        scheduledMessageCalendar?.timeInMillis = message.timestamp
        updateTimeInputs(scheduledMessageCalendar!!, date, time)

        date.setOnClickListener {
            ScheduledMessageUtils.displayDateDialog(fragmentActivity!!, scheduledMessageCalendar!!, date, time)
        }

        time.setOnClickListener {
            ScheduledMessageUtils.displayTimeDialog(fragmentActivity!!, scheduledMessageCalendar!!, date, time)
        }

        repeat.adapter = ArrayAdapter.createFromResource(fragmentActivity!!, R.array.scheduled_message_repeat, android.R.layout.simple_spinner_dropdown_item)
        repeat.setSelection(message.repeat)

        val color = if (Settings.useGlobalThemeColor) {
            Settings.mainColorSet.colorAccent
        } else {
            argManager.colorAccent
        }

        val okText = if (scheduleImmediately) {
            R.string.schedule
        }
        else {
            android.R.string.ok
        }

        val cancelText = if (isEdit) {
            R.string.delete
        }
        else {
            android.R.string.cancel
        }

        val builder = MaterialAlertDialogBuilder(fragmentActivity!!)
                .setView(layout)
                .setCancelable(false)
                .setNegativeButton(cancelText) { _, _ ->
                    sendManager.disableMessageScheduling()
                    hideScheduledTime()
                    imageData = null
                }
                .setPositiveButton(okText) {_, _ ->
                    if (scheduleImmediately) {
                        sendManager.sendScheduledMessage(message)
                    }
                    else {
                        message.repeat = repeat.selectedItemPosition
                        message.timestamp = scheduledMessageCalendar?.timeInMillis ?: TimeUtils.now
                        if (message.timestamp > TimeUtils.now) {
                            sendManager.enableMessageScheduling(message)
                            showScheduledTime(message, isEdit)
                        }
                    }
                }

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(color)
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(color)
    }

    fun showScheduledTime(message: ScheduledMessage, isEdit: Boolean = false) {

        if (!isEdit) {
            Handler().postDelayed({

                Thread { try {
                    val info = fragmentActivity?.findViewById<LinearLayout>(R.id.scheduled_message_info)

                    if (info != null) {
                        activity?.runOnUiThread {
                            val params = info.layoutParams
                            val animator = ValueAnimator.ofInt(0, DensityUtil.toDp(activity, 40))

                            info.requestLayout()
                            info.visibility = View.VISIBLE


                            animator.addUpdateListener { valueAnimator ->
                                val value = valueAnimator.animatedValue as Int
                                params.height = value
                                info.layoutParams = params
                            }

                            animator.duration = 200
                            animator.start()
                        }
                    }
                } catch (e: Throwable) {} }.start()
            }, 500)
        }

        val dateTime = fragmentActivity?.findViewById<TextView>(R.id.scheduled_date_time)
        val time = if (DateFormat.is24HourFormat(fragmentActivity)) {
            DateFormat.format("MM/dd/yy HH:mm", message.timestamp)
        } else {
            DateFormat.format("MM/dd/yy hh:mm a", message.timestamp)
        }

        var repeat = when (message.repeat) {
            ScheduledMessage.REPEAT_DAILY -> getString(R.string.scheduled_repeat_daily)
            ScheduledMessage.REPEAT_WEEKLY -> getString(R.string.scheduled_repeat_weekly)
            ScheduledMessage.REPEAT_MONTHLY -> getString(R.string.scheduled_repeat_monthly)
            ScheduledMessage.REPEAT_YEARLY -> getString(R.string.scheduled_repeat_yearly)
            else -> ""
        }

        if (!repeat.isBlank()) {
            repeat = " ($repeat)"
        }

        val text = getString(R.string.scheduled_time_formatted, time, repeat)
        dateTime?.text = text

        val editButton = fragmentActivity?.findViewById<LinearLayout>(R.id.scheduled_message_info_button)
        editButton?.setOnClickListener { displayScheduleDialog(message, isEdit = true) }
    }

    fun hideScheduledTime() {
        Handler().postDelayed({

            Thread { try {
                val info = fragmentActivity?.findViewById<LinearLayout>(R.id.scheduled_message_info)

                if (info != null) {
                    activity?.runOnUiThread {
                        val params = info.layoutParams
                        val animator = ValueAnimator.ofInt(info.height, 0)

                        animator.addUpdateListener { valueAnimator ->
                            val value = valueAnimator.animatedValue as Int
                            params.height = value
                            info.layoutParams = params
                        }

                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                info.visibility = View.GONE
                            }
                        })

                        animator.duration = 200
                        animator.start()
                    }
                }
            } catch (e: Throwable) {} }.start()
        }, 500)
    }
}
