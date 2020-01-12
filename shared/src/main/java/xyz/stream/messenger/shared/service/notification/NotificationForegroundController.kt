package xyz.stream.messenger.shared.service.notification

import android.content.Intent
import androidx.core.app.NotificationCompat
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.ColorSet
import xyz.stream.messenger.shared.util.AndroidVersionUtil
import xyz.stream.messenger.shared.util.NotificationUtils

class NotificationForegroundController(private val service: NotificationService) {

    private var gaveDismissableForegroundNotification = false
    private var gaveForegroundNotification = false

    fun show(intent: Intent?) {
        if (intent != null && intent.getBooleanExtra(NotificationConstants.EXTRA_FOREGROUND, false) && AndroidVersionUtil.isAndroidO) {
            gaveForegroundNotification = true
            gaveDismissableForegroundNotification = true
            val notification = NotificationCompat.Builder(service,
                    NotificationUtils.SILENT_BACKGROUND_CHANNEL_ID)
                    .setContentTitle(service.getString(R.string.repeat_interval))
                    .setSmallIcon(R.drawable.ic_stat_notify_group)
                    .setLocalOnly(true)
                    .setColor(ColorSet.DEFAULT(service).color)
                    .setOngoing(false)
                    .build()

            service.startForeground(NotificationConstants.FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    fun hide() {
        if (gaveForegroundNotification) {
            service.stopForeground(true)
        }
    }
}