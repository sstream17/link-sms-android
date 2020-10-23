package com.stream_suite.link.fragment.bottom_sheet

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.stream_suite.link.R
import com.stream_suite.link.fragment.ScheduledMessagesFragment
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.model.ScheduledMessage
import com.stream_suite.link.shared.service.jobs.ScheduledMessageJob
import com.stream_suite.link.shared.util.ScheduledMessageUtils
import com.stream_suite.link.shared.util.TimeUtils
import java.util.*

@Suppress("DEPRECATION")
class EditScheduledMessageFragment : TabletOptimizedBottomSheetDialogFragment() {

    private var fragment: ScheduledMessagesFragment? = null
    private var scheduledMessage: ScheduledMessage? = null
    private var scheduledMessageCalendar: Calendar? = null

    private lateinit var sendDate: TextView
    private lateinit var sendTime: TextView
    private lateinit var messageText: EditText
    private lateinit var repeat: Spinner

    override fun createLayout(inflater: LayoutInflater): View {
        val contentView = inflater.inflate(R.layout.bottom_sheet_edit_scheduled_message, null, false)

        sendDate = contentView.findViewById<TextView>(R.id.send_date)
        sendTime = contentView.findViewById<TextView>(R.id.send_time)
        messageText = contentView.findViewById<EditText>(R.id.message)
        repeat = contentView.findViewById<Spinner>(R.id.repeat_interval)
        val name = contentView.findViewById<TextView>(R.id.contact_name)
        val delete = contentView.findViewById<Button>(R.id.delete)
        val save = contentView.findViewById<Button>(R.id.save)
        val send = contentView.findViewById<Button>(R.id.send)

        if (scheduledMessage != null) {
            messageText.setText(scheduledMessage!!.data)
            sendDate.text = DateFormat.format("MM/dd/yy", scheduledMessage!!.timestamp)
            val timeFormat = if (DateFormat.is24HourFormat(activity!!)) "HH:mm" else "hh:mm a"
            sendTime.text = DateFormat.format(timeFormat, scheduledMessage!!.timestamp)
            name.text = scheduledMessage!!.title
            messageText.setSelection(messageText.text.length)

            repeat.adapter = ArrayAdapter.createFromResource(activity!!, R.array.scheduled_message_repeat, android.R.layout.simple_spinner_dropdown_item)
            repeat.setSelection(scheduledMessage!!.repeat)
        }

        save.setOnClickListener { save() }
        delete.setOnClickListener { delete() }
        send.setOnClickListener { send() }

        scheduledMessageCalendar = Calendar.getInstance()
        scheduledMessageCalendar?.timeInMillis = scheduledMessage!!.timestamp
        sendDate.setOnClickListener { ScheduledMessageUtils.displayDateDialog(activity!!, scheduledMessageCalendar!!, sendDate, sendTime) }
        sendTime.setOnClickListener { ScheduledMessageUtils.displayTimeDialog(activity!!, scheduledMessageCalendar!!, sendDate, sendTime) }

        return contentView
    }

    fun setMessage(message: ScheduledMessage) {
        this.scheduledMessage = message
    }

    fun setFragment(fragment: ScheduledMessagesFragment) {
        this.fragment = fragment
    }

    private fun save() {
        val activity = activity ?: return

        if (scheduledMessage == null) {
            return
        }

        scheduledMessage!!.timestamp = scheduledMessageCalendar!!.timeInMillis
        scheduledMessage!!.data = messageText.text.toString()
        scheduledMessage!!.repeat = repeat.selectedItemPosition
        DataSource.updateScheduledMessage(activity, scheduledMessage!!)

        dismiss()
        fragment?.loadMessages()
    }

    private fun delete() {
        val activity = activity ?: return

        DataSource.deleteScheduledMessage(activity, scheduledMessage!!.id)

        dismiss()
        fragment?.loadMessages()
    }

    private fun send() {
        scheduledMessage!!.timestamp = TimeUtils.now
        save()

        ScheduledMessageJob.scheduleNextRun(fragment!!.activity!!)
    }
}