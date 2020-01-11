package xyz.klinker.messenger.shared.service.notification.conversation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import android.text.Html
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion
import xyz.klinker.messenger.shared.R
import xyz.klinker.messenger.shared.data.DataSource
import xyz.klinker.messenger.shared.data.FeatureFlags
import xyz.klinker.messenger.shared.data.MimeType
import xyz.klinker.messenger.shared.data.Settings
import xyz.klinker.messenger.shared.data.model.Message
import xyz.klinker.messenger.shared.data.pojo.NotificationAction
import xyz.klinker.messenger.shared.data.pojo.NotificationConversation
import xyz.klinker.messenger.shared.data.pojo.NotificationMessage
import xyz.klinker.messenger.shared.data.pojo.VibratePattern
import xyz.klinker.messenger.shared.service.ReplyService
import xyz.klinker.messenger.shared.service.notification.*
import xyz.klinker.messenger.shared.util.*

/**
 * Displays a notification for a single conversation.
 */
@Suppress("DEPRECATION")
@SuppressLint("NewApi")
class NotificationConversationProvider(private val service: Context, private val ringtoneProvider: NotificationRingtoneProvider, private val summaryProvider: NotificationSummaryProvider) {

    private val actionHelper = NotificationActionHelper(service)
    private val carHelper = NotificationCarHelper(service)
    private val wearableHelper = NotificationWearableHelper(service)

    fun giveConversationNotification(conversation: NotificationConversation, smartReplies: List<SmartReplySuggestion> = emptyList()): Notification {
        val publicVersion = preparePublicBuilder(conversation)
                .setDefaults(buildNotificationDefaults(conversation))
                .applyLightsSoundAndVibrate(conversation)
                .addPerson(conversation)
//                .bubble(conversation)

        val builder = prepareBuilder(conversation)
                .setDefaults(buildNotificationDefaults(conversation))
                .setLargeIcon(buildContactImage(conversation))
                .setPublicVersion(publicVersion.build())
                .applyLightsSoundAndVibrate(conversation)
                .applyStyle(conversation)

        val remoteInputBuilder = RemoteInput.Builder(ReplyService.EXTRA_REPLY)
                .setLabel(service.getString(R.string.reply_to, conversation.title))
                .setAllowFreeFormInput(true)

        if (WearableCheck.isAndroidWear(service)) {
            remoteInputBuilder.setChoices(service.resources.getStringArray(R.array.reply_choices))
        }

        val remoteInput = remoteInputBuilder.build()

        val wearableExtender = wearableHelper.buildExtender(conversation)

        if (!conversation.privateNotification) {
            val otp = try {
                OneTimePasswordParser.getOtp(conversation.messages[0].data)
            } catch (e: Exception) {
                null
            }

            if (otp != null) {
                actionHelper.addOtpAction(builder, otp, conversation.id)
                actionHelper.addDeleteAction(builder, wearableExtender, conversation)
            } else {
                var smartReplyIndex = 0
                Settings.notificationActions.forEach {
                    when (it) {
                        NotificationAction.REPLY -> actionHelper.addReplyAction(builder, wearableExtender, remoteInput, conversation)
                        NotificationAction.CALL -> actionHelper.addCallAction(builder, wearableExtender, conversation)
                        NotificationAction.ARCHIVE -> actionHelper.addArchiveAction(builder, wearableExtender, conversation)
                        NotificationAction.MUTE -> actionHelper.addMuteAction(builder, wearableExtender, conversation)
                        NotificationAction.READ -> actionHelper.addMarkReadAction(builder, wearableExtender, conversation)
                        NotificationAction.DELETE -> actionHelper.addDeleteAction(builder, wearableExtender, conversation)
                        NotificationAction.EMPTY -> { /* no-op */ }
                        NotificationAction.SMART_REPLY -> {
                            actionHelper.addSmartReplyAction(builder, wearableExtender, conversation, smartReplies, smartReplyIndex)
                            smartReplyIndex++
                        }
                    }
                }
            }

            // apply the extenders to the notification
            builder.extend(carHelper.buildExtender(conversation, remoteInput))
            builder.extend(wearableExtender)
        }

        actionHelper.addContentIntents(builder, conversation)

        val notification = builder.build()

        if (NotificationConstants.CONVERSATION_ID_OPEN != conversation.id) {
            NotificationManagerCompat.from(service).notify(conversation.id.toInt(), notification)
        }

        return notification
    }

    private fun prepareCommonBuilder(conversation: NotificationConversation) = NotificationCompat.Builder(service,
                getNotificationChannel(service, conversation.id))
            .setSmallIcon(if (!conversation.groupConversation) R.drawable.ic_stat_notify else R.drawable.ic_stat_notify_group)
            .setAutoCancel(true)
            .setColor(if (Settings.useGlobalThemeColor) Settings.mainColorSet.color else conversation.color)
            .setPriority(if (Settings.headsUp) Notification.PRIORITY_MAX else Notification.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setGroupSummary(false)
            .setGroup("${NotificationConstants.GROUP_KEY_MESSAGES}-${conversation.id}")
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .addPerson(conversation)

    private fun prepareBuilder(conversation: NotificationConversation) =
            prepareCommonBuilder(conversation)
                    .setContentTitle(conversation.title)
                    .setShowWhen(true)
                    .setTicker(service.getString(R.string.notification_ticker, conversation.title))
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setWhen(if (AndroidVersionUtil.isAndroidO) TimeUtils.now else conversation.timestamp)

    private fun preparePublicBuilder(conversation: NotificationConversation) =
            prepareCommonBuilder(conversation)
                    .setContentTitle(service.resources.getQuantityString(R.plurals.new_conversations, 1, 1))
                    .setContentText(service.resources.getQuantityString(R.plurals.new_messages, conversation.messages.size, conversation.messages.size))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    private fun buildContactImage(conversation: NotificationConversation): Bitmap? {
        var contactImage = ImageUtils.clipToCircle(ImageUtils.getBitmap(service, conversation.imageUri))

        try {
            val height = service.resources.getDimension(android.R.dimen.notification_large_icon_height)
            val width = service.resources.getDimension(android.R.dimen.notification_large_icon_width)
            contactImage = Bitmap.createScaledBitmap(contactImage!!, width.toInt(), height.toInt(), true)
        } catch (e: Exception) {
        }

        return contactImage
    }

    private fun buildNotificationDefaults(conversation: NotificationConversation): Int {
        if (WearableCheck.isAndroidWear(service)) {
            return Notification.DEFAULT_ALL
        }

        val vibratePattern = Settings.vibrate
        var defaults = 0
        if (vibratePattern === VibratePattern.DEFAULT) {
            defaults = Notification.DEFAULT_VIBRATE
        }

        if (conversation.ledColor == Color.WHITE) {
            defaults = defaults or Notification.DEFAULT_LIGHTS
        }

        return defaults
    }

    private fun buildMessagingStyle(conversation: NotificationConversation): NotificationCompat.MessagingStyle? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !Settings.historyInNotifications) {
            return null
        }

        val messagingStyle = NotificationCompat.MessagingStyle(service.getString(R.string.you))

        if (conversation.groupConversation) {
            messagingStyle.conversationTitle = conversation.title
            messagingStyle.isGroupConversation = true
        }

        val messages = conversation.realMessages
        val imageCache = mutableMapOf<String, Bitmap?>()

        for (i in messages.indices.reversed()) {
            val message = messages[i]

            val person: Person? = if (message.type == Message.TYPE_RECEIVED) {
                // we split it so that we only get the first name,
                // if there is more than one

                val image = if (AndroidVersionUtil.isAndroidP) {
                    val circle = when {
                        conversation.imageUri != null -> {
                            if (imageCache.containsKey(conversation.imageUri!!)) {
                                imageCache[conversation.imageUri!!]
                            } else {
                                val image = ImageUtils.getBitmap(service, conversation.imageUri)
                                val circleImage = ImageUtils.clipToCircle(image)

                                imageCache[conversation.imageUri!!] = circleImage

                                circleImage
                            }
                        }
                        message.from != null -> {
                            if (imageCache.containsKey(message.from!!)) {
                                imageCache[message.from!!]
                            } else {
                                val uri = ContactUtils.findImageUri(message.from!!, service, true)
                                val image = ImageUtils.getBitmap(service, uri)
                                val circleImage = ImageUtils.clipToCircle(image)

                                imageCache[message.from!!] = circleImage

                                circleImage
                            }
                        }
                        else -> null
                    }

                    circle
                } else {
                    null
                }

                Person.Builder()
                        .setName(if (message.from != null) message.from else conversation.title)
                        .setIcon(if (image != null) IconCompat.createWithBitmap(image) else null)
                        .build()
            } else {
                null
            }

            val messageText = when {
                MimeType.isAudio(message.mimeType!!) -> Html.fromHtml("<i>" + service.getString(R.string.audio_message) + "</i>")
                MimeType.isVideo(message.mimeType!!) -> Html.fromHtml("<i>" + service.getString(R.string.video_message) + "</i>")
                MimeType.isVcard(message.mimeType!!) -> Html.fromHtml("<i>" + service.getString(R.string.contact_card) + "</i>")
                MimeType.isStaticImage(message.mimeType) -> Html.fromHtml("<i>" + service.getString(R.string.picture_message) + "</i>")
                message.mimeType == MimeType.IMAGE_GIF -> Html.fromHtml("<i>" + service.getString(R.string.gif_message) + "</i>")
                MimeType.isExpandedMedia(message.mimeType) -> Html.fromHtml("<i>" + service.getString(R.string.media) + "</i>")
                else -> message.data
            }

            val m = NotificationCompat.MessagingStyle.Message(messageText, message.timestamp, person)
            if (MimeType.isStaticImage(message.mimeType)) {
                m.setData(message.mimeType, Uri.parse(message.data))
            }

            messagingStyle.addMessage(m)
        }

        return messagingStyle
    }


    //
    //
    // Extension methods on the notification builder
    //
    //


    private fun NotificationCompat.Builder.applyLightsSoundAndVibrate(conversation: NotificationConversation): NotificationCompat.Builder {
        if (WearableCheck.isAndroidWear(service)) {
            return this
        }

        if (conversation.ledColor != Color.WHITE) {
            this.setLights(conversation.ledColor, 1000, 500)
        }

        val sound = ringtoneProvider.getRingtone(conversation.ringtoneUri)
        if (sound != null) {
            this.setSound(sound)
        }

        if (Settings.vibrate.pattern != null) {
            this.setVibrate(Settings.vibrate.pattern)
        } else if (Settings.vibrate === VibratePattern.OFF) {
            this.setVibrate(LongArray(0))
        }

        return this
    }

    private fun NotificationCompat.Builder.applyStyle(conversation: NotificationConversation): NotificationCompat.Builder {
        val messagingStyle: NotificationCompat.Style? = buildMessagingStyle(conversation)
        var pictureStyle: NotificationCompat.BigPictureStyle? = null
        var inboxStyle: NotificationCompat.InboxStyle? = null

        val text = StringBuilder()
        if (conversation.messages.size > 1 && conversation.messages[0].from != null) {
            inboxStyle = NotificationCompat.InboxStyle()

            for ((_, data, mimeType, _, from) in conversation.messages) {
                if (mimeType == MimeType.TEXT_PLAIN) {
                    val line = "<b>$from:</b>  $data"
                    text.append(line)
                    text.append("\n")
                    inboxStyle.addLine(Html.fromHtml(line))
                } else {
                    pictureStyle = NotificationCompat.BigPictureStyle()
                            .bigPicture(ImageUtils.getBitmap(service, data))
                }
            }
        } else {
            for (i in 0 until conversation.messages.size) {
                val (_, data, mimeType, _, from) = conversation.messages[i]

                if (mimeType == MimeType.TEXT_PLAIN) {
                    if (from != null) {
                        text.append("<b>")
                        text.append(from)
                        text.append(":</b>  ")
                        text.append(conversation.messages[i].data)
                        text.append("\n")
                    } else {
                        text.append(conversation.messages[i].data)
                        text.append("<br/>")
                    }
                } else if (MimeType.isStaticImage(mimeType)) {
                    pictureStyle = NotificationCompat.BigPictureStyle()
                            .bigPicture(ImageUtils.getBitmap(service, data))
                }
            }
        }

        var content = text.toString().trim { it <= ' ' }
        if (content.endsWith("<br/>")) {
            content = content.substring(0, content.length - 5)
        }

        if (!conversation.privateNotification) {
            val formattedText = when {
                !content.contains("<b>") && !content.contains("<br/>") -> content
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Html.fromHtml(content, 0)
                else -> Html.fromHtml(content)
            }

            this.setContentText(formattedText)

            when {
                pictureStyle != null && !AndroidVersionUtil.isAndroidP -> {
                    this.setStyle(pictureStyle)
                    this.setContentText(service.getString(R.string.picture_message))
                }
                messagingStyle != null -> this.setStyle(messagingStyle)
                inboxStyle != null -> this.setStyle(inboxStyle)
                else -> this.setStyle(NotificationCompat.BigTextStyle().bigText(formattedText))
            }
        }

        return this
    }

    private fun NotificationCompat.Builder.addPerson(conversation: NotificationConversation): NotificationCompat.Builder {
        // TODO: use a "person" object here, so that bubbles work?
        if (!conversation.groupConversation) {
            this.addPerson("tel:" + conversation.phoneNumbers!!)
        } else {
            for (number in conversation.phoneNumbers!!.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                this.addPerson("tel:$number")
            }
        }

        return this
    }

    private fun NotificationCompat.Builder.bubble(conversation: NotificationConversation): NotificationCompat.Builder {
        if (!AndroidVersionUtil.isAndroidQ) {
            return this
        }

        val icon = IconCompat.createWithAdaptiveBitmap(buildContactImage(conversation))
        val uri = Uri.parse("https://messenger.klinkerapps.com/" + conversation.id)
        val intent = PendingIntent.getActivity(
                service,
                conversation.id.toInt(),
                ActivityUtils.buildForComponent(ActivityUtils.BUBBLE_ACTIVITY)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(uri),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

//        this.bubbleMetadata = NotificationCompat.BubbleMetadata.Builder()
//                .setDesiredHeight(DensityUtil.toDp(service, 400))
//                .setIcon(icon)
//                .setIntent(intent)
//                .build()

        return this
    }

    companion object {
        internal fun getNotificationChannel(service: Context, conversationId: Long): String {
            if (!AndroidVersionUtil.isAndroidO) {
                return NotificationUtils.DEFAULT_CONVERSATION_CHANNEL_ID
            }

            val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return if (manager.getNotificationChannel(conversationId.toString() + "") != null) {
                conversationId.toString() + ""
            } else {
                NotificationUtils.DEFAULT_CONVERSATION_CHANNEL_ID
            }
        }
    }

}