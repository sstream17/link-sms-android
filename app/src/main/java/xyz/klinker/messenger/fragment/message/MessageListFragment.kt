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

package xyz.klinker.messenger.fragment.message

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sgottard.sofa.ContentFragment
import xyz.klinker.giphy.Giphy
import xyz.klinker.messenger.BuildConfig

import xyz.klinker.messenger.R
import xyz.klinker.messenger.activity.MessengerActivity
import xyz.klinker.messenger.activity.MessengerTvActivity
import xyz.klinker.messenger.activity.compose.ShareData
import xyz.klinker.messenger.activity.main.MainSearchHelper
import xyz.klinker.messenger.api.implementation.Account
import xyz.klinker.messenger.fragment.message.attach.AttachmentInitializer
import xyz.klinker.messenger.fragment.message.attach.AttachmentListener
import xyz.klinker.messenger.fragment.message.attach.AttachmentManager
import xyz.klinker.messenger.fragment.message.send.MessageCounterCalculator
import xyz.klinker.messenger.fragment.message.load.MessageListLoader
import xyz.klinker.messenger.fragment.message.send.PermissionHelper
import xyz.klinker.messenger.fragment.message.send.SendMessageManager
import xyz.klinker.messenger.fragment.message.load.ViewInitializerDeferred
import xyz.klinker.messenger.fragment.message.load.ViewInitializerNonDeferred
import xyz.klinker.messenger.shared.data.DataSource
import xyz.klinker.messenger.shared.data.MimeType
import xyz.klinker.messenger.shared.data.MmsSettings
import xyz.klinker.messenger.shared.data.model.ScheduledMessage
import xyz.klinker.messenger.shared.receiver.MessageListUpdatedReceiver
import xyz.klinker.messenger.shared.service.notification.NotificationConstants
import xyz.klinker.messenger.shared.shared_interfaces.IMessageListFragment
import xyz.klinker.messenger.shared.util.AnimationUtils
import xyz.klinker.messenger.shared.util.CursorUtil
import xyz.klinker.messenger.shared.util.TimeUtils
import xyz.klinker.messenger.utils.multi_select.MessageMultiSelectDelegate
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

    private var imageData: ShareData? = null
    private var messageInProcess: ScheduledMessage? = null

    // samsung messed up the date picker in some languages on Lollipop 5.0 and 5.1. Ugh.
    // fixes this issue: http://stackoverflow.com/a/34853067
    private val contextToFixDatePickerCrash: ContextWrapper
        get() = object : ContextWrapper(activity!!) {
            private var wrappedResources: Resources? = null
            override fun getResources(): Resources {
                val r = super.getResources()
                if (wrappedResources == null) {
                    wrappedResources = object : Resources(r.assets, r.displayMetrics, r.configuration) {
                        @Throws(Resources.NotFoundException::class)
                        override fun getString(id: Int, vararg formatArgs: Any): String {
                            return try {
                                super.getString(id, *formatArgs)
                            } catch (ifce: IllegalFormatConversionException) {
                                Log.e("DatePickerDialogFix", "IllegalFormatConversionException Fixed!", ifce)
                                var template = super.getString(id)
                                template = template.replace(("%" + ifce.conversion).toRegex(), "%s")
                                String.format(configuration.locale, template, *formatArgs)
                            }

                        }
                    }
                }

                return wrappedResources!!
            }
        }

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

        draftManager.createDrafts()
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

        if (searchHelper.closeSearch()) {
            return true
        }

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

    fun startSchedulingMessage() {
        val message = ScheduledMessage()
        displayScheduleDialog(message)
    }

    private fun dismissDetailsChoiceDialog() {
        if (detailsChoiceDialog != null && detailsChoiceDialog!!.isShowing) {
            detailsChoiceDialog!!.dismiss()
            detailsChoiceDialog = null
        }
    }

    private fun displayScheduleDialog(message: ScheduledMessage){
        val layout = LayoutInflater.from(fragmentActivity).inflate(R.layout.dialog_scheduled_message, null, false)
        val repeat = layout.findViewById<Spinner>(R.id.repeat_interval)

        repeat.adapter = ArrayAdapter.createFromResource(fragmentActivity!!, R.array.scheduled_message_repeat, android.R.layout.simple_spinner_dropdown_item)

        val builder = AlertDialog.Builder(fragmentActivity!!)
                .setView(layout)
                .setCancelable(true)

        builder.show()
    }

    private fun displayDateDialog(message: ScheduledMessage) {
        var context: Context? = contextToFixDatePickerCrash

        if (context == null) {
            context = fragmentActivity
        }

        if (context == null) {
            return
        }

        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, year, month, day ->
            message.timestamp = GregorianCalendar(year, month, day)
                    .timeInMillis
            displayTimeDialog(message)
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    private fun displayTimeDialog(message: ScheduledMessage) {
        if (fragmentActivity == null) {
            return
        }

        val calendar = Calendar.getInstance()
        TimePickerDialog(fragmentActivity, { _, hourOfDay, minute ->
            message.timestamp = message.timestamp + 1000 * 60 * 60 * hourOfDay
            message.timestamp = message.timestamp + 1000 * 60 * minute

            if (message.timestamp > TimeUtils.now) {
                displayMessageDialog(message)
            } else {
                Toast.makeText(fragmentActivity, R.string.scheduled_message_in_future,
                        Toast.LENGTH_SHORT).show()
                displayDateDialog(message)
            }
        },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(fragmentActivity))
                .show()
    }

    private fun displayMessageDialog(message: ScheduledMessage) {

        val layout = LayoutInflater.from(fragmentActivity).inflate(R.layout.dialog_scheduled_message_content, null, false)
        val editText = layout.findViewById<EditText>(R.id.edit_text)
        val repeat = layout.findViewById<Spinner>(R.id.repeat_interval)
        val image = layout.findViewById<ImageView>(R.id.image)

        repeat.adapter = ArrayAdapter.createFromResource(fragmentActivity!!, R.array.scheduled_message_repeat, android.R.layout.simple_spinner_dropdown_item)

        if (imageData != null) {
            image.visibility = View.VISIBLE
            if (imageData!!.mimeType == MimeType.IMAGE_GIF) {
                Glide.with(fragmentActivity!!)
                        .asGif()
                        .load(imageData!!.data)
                        .into(image)
            } else {
                Glide.with(fragmentActivity!!)
                        .load(imageData!!.data)
                        .apply(RequestOptions().centerCrop())
                        .into(image)
            }
        }

        if (message.data != null && message.data!!.isNotEmpty()) {
            editText.setText(message.data)
            editText.setSelection(message.data!!.length)
        }

        editText.post {
            editText.requestFocus()
            (fragmentActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }

        val builder = AlertDialog.Builder(fragmentActivity!!)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton(R.string.save) { _, _ ->
                    if (editText.text.isNotEmpty() || image != null) {
                        message.repeat = repeat.selectedItemPosition

                        val messages = mutableListOf<ScheduledMessage>()

                        if (editText.text.isNotEmpty()) {
                            messages.add(ScheduledMessage().apply {
                                this.id = DataSource.generateId()
                                this.repeat = message.repeat
                                this.timestamp = message.timestamp
                                this.title = message.title
                                this.to = message.to
                                this.data = editText.text.toString()
                                this.mimeType = MimeType.TEXT_PLAIN
                            })
                        }

                        if (imageData != null) {
                            messages.add(ScheduledMessage().apply {
                                this.id = DataSource.generateId()
                                this.repeat = message.repeat
                                this.timestamp = message.timestamp
                                this.title = message.title
                                this.to = message.to
                                this.data = imageData!!.data
                                this.mimeType = imageData!!.mimeType
                            })
                        }

                        saveMessages(messages)
                        imageData = null

                        (fragmentActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                                ?.hideSoftInputFromWindow(editText.windowToken, 0)
                    } else {
                        displayMessageDialog(message)
                    }
                }.setNegativeButton(android.R.string.cancel) { _, _ ->
                    imageData = null
                    (fragmentActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                            ?.hideSoftInputFromWindow(editText.windowToken, 0)
                }

        if (!Account.exists() || Account.primary) {
            if (imageData == null) {
                builder.setNeutralButton(R.string.attach_image) { _, _ ->
                    if (editText.text.isNotEmpty()) {
                        message.data = editText.text.toString()
                    } else {
                        message.data = null
                    }

                    (fragmentActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                            ?.hideSoftInputFromWindow(editText.windowToken, 0)

                    AlertDialog.Builder(fragmentActivity!!)
                            .setItems(R.array.scheduled_message_attachment_options) { _, position ->
                                messageInProcess = message

                                when (position) {
                                    0 -> {
                                        val intent = Intent()
                                        intent.type = "image/*"
                                        intent.action = Intent.ACTION_GET_CONTENT
                                        fragmentActivity?.startActivityForResult(Intent.createChooser(intent, "Select Picture"), AttachmentListener.RESULT_GALLERY_PICKER_REQUEST)
                                    }
                                    1 -> {
                                        Giphy.Builder(fragmentActivity, BuildConfig.GIPHY_API_KEY)
                                                .maxFileSize(MmsSettings.maxImageSize)
                                                .start()
                                    }
                                }
                            }.show()
                }
            } else {
                builder.setNeutralButton(R.string.remove_image_short) { _, _ ->
                    if (editText.text.isNotEmpty()) {
                        message.data = editText.text.toString()
                    } else {
                        message.data = null
                    }

                    displayMessageDialog(message)
                }
            }
        }

        builder.show()
    }

    private fun saveMessages(messages: List<ScheduledMessage>) {
        Thread {
            messages.forEach { DataSource.insertScheduledMessage(fragmentActivity!!, it) }
        }.start()
    }

}
