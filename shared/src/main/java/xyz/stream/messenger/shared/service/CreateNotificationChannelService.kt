package xyz.stream.messenger.shared.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat

import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.ColorSet
import xyz.stream.messenger.shared.util.AndroidVersionUtil
import xyz.stream.messenger.shared.util.NotificationUtils

class CreateNotificationChannelService : IntentService("CreateNotificationChannelService") {

    override fun onHandleIntent(intent: Intent?) {
        val notification = NotificationCompat.Builder(this,
                NotificationUtils.SILENT_BACKGROUND_CHANNEL_ID)
                .setContentTitle(getString(R.string.creating_channels_text))
                .setSmallIcon(R.drawable.ic_stat_notify_group)
                .setLocalOnly(true)
                .setColor(ColorSet.DEFAULT(this).color)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
        startForeground(FOREGROUND_ID, notification)

        NotificationUtils.createNotificationChannels(this)

        stopForeground(true)
    }

    companion object {
        private val FOREGROUND_ID = 1224
        private val NOTIFICATION_CHANNEL_VERSION = 5
        private val PREF_KEY = "needs_to_create_notification_channels_$NOTIFICATION_CHANNEL_VERSION"

        fun shouldRun(context: Context): Boolean {
            if (!AndroidVersionUtil.isAndroidO) {
                return false
            }

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val shouldRun = prefs.getBoolean(PREF_KEY, true)

            if (shouldRun) prefs.edit().putBoolean(PREF_KEY, false).apply()
            return shouldRun
        }
    }
}
