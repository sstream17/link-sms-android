package xyz.stream.messenger.activity.main

import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.MessengerActivityExtras

class NotificationDismissalController(private val activity: MessengerActivity) {

    private val intent
        get() = activity.intent
}