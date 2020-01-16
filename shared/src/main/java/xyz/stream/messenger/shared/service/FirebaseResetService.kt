package xyz.stream.messenger.shared.service

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationCompat

import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.ColorSet
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.util.AndroidVersionUtil
import xyz.stream.messenger.shared.util.NotificationUtils

class FirebaseResetService : IntentService("FirebaseResetService") {

    override fun onHandleIntent(intent: Intent?) {
        if (Account.primary) {
            return
        }

        if (AndroidVersionUtil.isAndroidO) {
            val notification = NotificationCompat.Builder(this,
                    NotificationUtils.SILENT_BACKGROUND_CHANNEL_ID)
                    .setContentTitle(getString(R.string.media_parse_text))
                    .setSmallIcon(R.drawable.ic_stat_notify_group)
                    .setProgress(0, 0, true)
                    .setLocalOnly(true)
                    .setColor(ColorSet.DEFAULT(this).color)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .build()
            startForeground(FOREGROUND_ID, notification)
        }

        // going to re-download everything I guess..
        DataSource.clearTables(this)
        ApiDownloadService.start(this)

        if (AndroidVersionUtil.isAndroidO) {
            stopForeground(true)
        }
    }

    companion object {
        private val FOREGROUND_ID = 1223
    }
}
