package com.stream_suite.link.shared.service

import android.app.Activity
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.R
import com.stream_suite.link.shared.data.*
import com.stream_suite.link.shared.data.model.Message
import com.stream_suite.link.shared.receiver.MessageListUpdatedReceiver
import com.stream_suite.link.shared.util.*
import java.util.*

/**
 * Check whether or not there are messages in the internal database, that are not in Pulse's
 * database. This is useful for if a user goes away from Pulse for awhile, then wants to return to
 * it.
 */
class NewMessagesCheckService : IntentService("NewMessageCheckService") {

    override fun onHandleIntent(intent: Intent?) {
        val foreground = if (intent != null && intent.getBooleanExtra(EXTRA_FOREGROUND_NOTIFICATION, false)) {
            val notification = NotificationCompat.Builder(this,
                    NotificationUtils.SILENT_BACKGROUND_CHANNEL_ID)
                    .setContentTitle(getString(R.string.receiving_a_message))
                    .setSmallIcon(R.drawable.ic_download)
                    .setProgress(0, 0, true)
                    .setLocalOnly(true)
                    .setColor(ColorSet.DEFAULT(this).color)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build()
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
            true
        } else false

        try {
            handle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (foreground) {
            stopForeground(true)
        }
    }

    private fun handle() {
        if (ApiDownloadService.IS_RUNNING || (Account.exists() && !Account.primary)) {
            return
        }

        val sharedPreferences = Settings.getSharedPrefs(this)
        val lastRun = sharedPreferences.getLong("new_message_check_last_run", 0L)
        val fiveSecondsBefore = TimeUtils.now - TimeUtils.SECOND * 5

        val appSignature: String
        if (!Settings.signature!!.isEmpty()) {
            appSignature = "\n" + Settings.signature!!
        } else {
            appSignature = ""
        }

        if (Settings.stripUnicode) {
            return
        }

        // grab the latest 60 messages from Pulse's database
        // grab the latest 20 messages from the the internal SMS/MMS database
        // iterate over the internal messages and see if they are in the list from Pulse's database (search by text is fine)
        // if they are:
        //      continue, no problems here
        // if they aren't:
        //      insert them into the correct conversation and give the conversation update broadcast
        //      should I worry about updating the conversation list here?

        val pulseMessages = DataSource.getNumberOfMessages(this, 60)
        val internalMessages = SmsMmsUtils.getLatestSmsMessages(this, 20)

        val messagesToInsert = ArrayList<Message>()
        val addressesForMessages = ArrayList<String>()
        if (internalMessages != null && internalMessages.moveToFirst()) {
            do {
                var messageBody = internalMessages.getString(internalMessages.getColumnIndex(Telephony.Sms.BODY)).trim { it <= ' ' }
                val messageType = SmsMmsUtils.getSmsMessageType(internalMessages)
                val messageTimestamp = internalMessages.getLong(internalMessages.getColumnIndex(Telephony.Sms.DATE))

                if (messageType != Message.TYPE_RECEIVED) {
                    // sent message don't show a signature in the app, but they would be written to the internal database with one
                    // received messages don't need to worry about this, and shouldn't. If you send yourself a message, then it would come
                    // in with a signature, if this was applied to received messages, then that received message would get duplicated
                    messageBody = messageBody.replace(appSignature, "")
                }

                // the message timestamp should be more than the last time this service ran, but more than 5 seconds old,
                // and it shouldn't already be in the database
                if (messageTimestamp in (lastRun + 1) until fiveSecondsBefore) {
                    if (!alreadyInDatabase(pulseMessages, messageBody, messageType)) {
                        val message = Message()

                        message.type = messageType
                        message.data = messageBody
                        message.timestamp = messageTimestamp
                        message.mimeType = MimeType.TEXT_PLAIN
                        message.read = true
                        message.seen = true
                        if (messageType != Message.TYPE_RECEIVED) {
                            message.sentDeviceId = if (Account.exists()) java.lang.Long.parseLong(Account.deviceId!!) else -1L
                        } else {
                            message.sentDeviceId = -1L
                        }

                        messagesToInsert.add(message)
                        addressesForMessages.add(PhoneNumberUtils.clearFormatting(
                                internalMessages.getString(internalMessages.getColumnIndex(Telephony.Sms.ADDRESS))))
                    } else {
                        val message = messageStatusNeedsUpdatedToSent(pulseMessages, messageBody, messageType)
                        if (message != null) {
                            DataSource.updateMessageType(this, message.id, Message.TYPE_SENT)
                        }
                    }
                }
            } while (internalMessages.moveToNext())

            internalMessages.closeSilent()
        }

        val conversationsToRefresh = ArrayList<Long>()
        for (i in messagesToInsert.indices) {
            val message = messagesToInsert[i]
            val conversationId = DataSource.insertMessage(message,
                    PhoneNumberUtils.clearFormatting(addressesForMessages[i]), this)

            if (!conversationsToRefresh.contains(conversationId)) {
                conversationsToRefresh.add(conversationId)
            }
        }

        for (conversationId in conversationsToRefresh) {
            MessageListUpdatedReceiver.sendBroadcast(this, conversationId)
        }

        if (conversationsToRefresh.size > 0) {
            //sendBroadcast(new Intent(REFRESH_WHOLE_CONVERSATION_LIST));
        }

        NewMessagesCheckService.writeLastRun(this)
    }

    private fun alreadyInDatabase(messages: List<Message>, bodyToSearch: String, newMessageType: Int): Boolean {
        return messages.any { message -> message.mimeType == MimeType.TEXT_PLAIN &&
                typesAreEqual(newMessageType, message.type) &&
                bodyToSearch.trim { it <= ' ' }.contains(message.data!!.trim { it <= ' ' }) }
    }

    private fun messageStatusNeedsUpdatedToSent(messages: List<Message>, bodyToSearch: String, newMessageType: Int): Message? {
        if (newMessageType != Message.TYPE_SENT) {
            return null
        }

        return messages.firstOrNull { message -> message.mimeType == MimeType.TEXT_PLAIN &&
                message.type == Message.TYPE_SENDING &&
                message.data!!.trim { it <= ' ' } == bodyToSearch.trim { it <= ' ' } }
    }

    companion object {

        private const val FOREGROUND_NOTIFICATION_ID = 44562
        private const val EXTRA_FOREGROUND_NOTIFICATION = "extra_foreground_notification"
        const val REFRESH_WHOLE_CONVERSATION_LIST = "com.stream_suite.link.REFRESH_WHOLE_CONVERSATION_LIST"

        fun startService(activity: Activity) {
            try {
                activity.startService(Intent(activity, NewMessagesCheckService::class.java))
            } catch (e: IllegalStateException) {
            }
        }

        fun startService(context: Context) {
            val intent = Intent(context, NewMessagesCheckService::class.java)

            try {
                if (AndroidVersionUtil.isAndroidO) {
                    intent.putExtra(EXTRA_FOREGROUND_NOTIFICATION, true)
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: IllegalStateException) {
            }
        }

        fun writeLastRun(context: Context, time: Long = TimeUtils.now) {
            try {
                Settings.getSharedPrefs(context).edit()
                        .putLong("new_message_check_last_run", time)
                        .apply()
            } catch (e: Exception) {
                // in robolectric, i don't want it to crash
            }
        }

        fun typesAreEqual(newMessageType: Int, oldMessageType: Int): Boolean {
            return when (newMessageType) {
                Message.TYPE_ERROR -> oldMessageType == Message.TYPE_ERROR
                Message.TYPE_RECEIVED -> oldMessageType == Message.TYPE_RECEIVED
                else -> oldMessageType != Message.TYPE_RECEIVED
            }
        }
    }
}
