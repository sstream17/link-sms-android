package xyz.stream.messenger.fragment.message

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import xyz.stream.messenger.activity.BubbleActivity
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.util.NotificationUtils


class MessageListNotificationManager(private val fragment: MessageListFragment) {
    
    private val activity: FragmentActivity? by lazy { fragment.activity }
    private val argManager
        get() = fragment.argManager

    var dismissNotification = false
    var dismissOnStartup = false

    fun dismissNotification() {
        if (fragment.activity is BubbleActivity) {
            return
        }

        try {
            if (dismissNotification && notificationActive()) {
                NotificationManagerCompat.from(activity!!)
                        .cancel(argManager.conversationId.toInt())

                ApiUtils.dismissNotification(Account.accountId,
                        Account.deviceId,
                        argManager.conversationId)

                NotificationUtils.cancelGroupedNotificationWithNoContent(activity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun dismissOnMessageSent() {
        if (fragment.activity is BubbleActivity) {
            return
        }

        if (notificationActive()) {
            NotificationManagerCompat.from(activity!!).cancel(argManager.conversationId.toInt())
            NotificationUtils.cancelGroupedNotificationWithNoContent(activity)
        }
    }

    fun onStart() {
        dismissNotification = true

        if (dismissOnStartup) {
            dismissNotification()
            dismissOnStartup = false
        }
    }

    private fun notificationActive() =
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
            else {
                val manager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                val notifications = manager?.activeNotifications

                val notificationId = argManager.conversationId.toInt()
                notifications?.any { it.id == notificationId } == true
            }
        } catch (e: Exception) {
            true
        }
}