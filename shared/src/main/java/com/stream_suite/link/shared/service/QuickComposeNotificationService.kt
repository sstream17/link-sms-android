package com.stream_suite.link.shared.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stream_suite.link.shared.R
import com.stream_suite.link.shared.data.ColorSet
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.util.ActivityUtils
import com.stream_suite.link.shared.util.ContactUtils
import com.stream_suite.link.shared.util.NotificationUtils
import com.stream_suite.link.shared.util.WearableCheck

class QuickComposeNotificationService : IntentService("QuickComposeNotificationService") {

    override fun onHandleIntent(intent: Intent?) {
        val foreground = NotificationCompat.Builder(this, NotificationUtils.SILENT_BACKGROUND_CHANNEL_ID)
                .setContentTitle(getString(R.string.write_new_message))
                .setSmallIcon(R.drawable.ic_stat_notify_group)
                .setLocalOnly(true)
                .setColor(ColorSet.DEFAULT(this).color)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
        startForeground(FOREGROUND_ID, foreground)

        if (!WearableCheck.isAndroidWear(this)) {
            val notification = NotificationCompat.Builder(this, NotificationUtils.QUICK_TEXT_CHANNEL_ID)
                    .setContentTitle(getString(R.string.write_new_message))
                    .setSmallIcon(R.drawable.ic_stat_notify_group)
                    .setLocalOnly(true)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setWhen(0)

            addContentIntent(notification)
            addActionsToNotification(notification)

            NotificationManagerCompat.from(this).notify(QUICK_TEXT_ID, notification.build())
        }

        stopForeground(true)
    }

    private fun addContentIntent(builder: NotificationCompat.Builder) {
        val compose = ActivityUtils.buildForComponent(ActivityUtils.QUICK_SHARE_ACTIVITY)
        val pendingCompose = PendingIntent.getActivity(this, QUICK_TEXT_ID,
                compose, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pendingCompose)
    }

    private fun addActionsToNotification(builder: NotificationCompat.Builder) {
        val numbers = getNumbersFromPrefs(this)

        for (i in numbers.indices) {
            val name = ContactUtils.findContactNames(numbers[i], this)

            val compose = ActivityUtils.buildForComponent(ActivityUtils.QUICK_SHARE_ACTIVITY)
            compose.data = Uri.parse("sms:${numbers[i]}")
            compose.action = Intent.ACTION_SENDTO

            val pendingCompose = PendingIntent.getActivity(this, QUICK_TEXT_ID + i,
                    compose, PendingIntent.FLAG_UPDATE_CURRENT)

            val action = NotificationCompat.Action(R.drawable.ic_reply_white, name, pendingCompose)
            builder.addAction(action)
        }
    }

    companion object {
        private const val FOREGROUND_ID = 1225
        private const val QUICK_TEXT_ID = 1226

        fun start(context: Context) {
            val intent = Intent(context, QuickComposeNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            NotificationManagerCompat.from(context).cancel(QUICK_TEXT_ID)
        }

        fun getNumbersFromPrefs(context: Context): List<String> {
            val numbers = Settings.getSharedPrefs(context).getString(context.getString(R.string.pref_quick_compose_favorites), null)
            return numbers?.split(",".toRegex())?.filter { it.isNotBlank() } ?: emptyList()
        }
    }
}