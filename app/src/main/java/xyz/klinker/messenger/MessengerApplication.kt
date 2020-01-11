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

package xyz.klinker.messenger

import android.app.Application
import android.content.Intent
import android.os.Build
import xyz.klinker.messenger.api.implementation.Account
import xyz.klinker.messenger.api.implementation.AccountInvalidator
import xyz.klinker.messenger.api.implementation.firebase.FirebaseApplication
import xyz.klinker.messenger.api.implementation.firebase.FirebaseMessageHandler
import xyz.klinker.messenger.api.implementation.retrofit.ApiErrorPersister
import xyz.klinker.messenger.shared.data.DataSource
import xyz.klinker.messenger.shared.data.Settings
import xyz.klinker.messenger.shared.data.model.RetryableRequest
import xyz.klinker.messenger.shared.service.FirebaseHandlerService
import xyz.klinker.messenger.shared.service.FirebaseResetService
import xyz.klinker.messenger.shared.service.QuickComposeNotificationService
import xyz.klinker.messenger.shared.util.*
import xyz.klinker.messenger.shared.util.UpdateUtils

/**
 * Base application that will serve as any intro for any context in the rest of the app. Main
 * function is to enable night mode so that colors change depending on time of day.
 */
class MessengerApplication : FirebaseApplication(), ApiErrorPersister, AccountInvalidator {

    override fun onCreate() {
        super.onCreate()

        KotlinObjectInitializers.initializeObjects(this)
        FirstRunInitializer.applyDefaultSettings(this)
        UpdateUtils.rescheduleWork(this)

        enableSecurity()

        TimeUtils.setupNightTheme()
        NotificationUtils.createNotificationChannels(this)

        if (Settings.quickCompose) {
            QuickComposeNotificationService.start(this)
        }
    }

    fun refreshDynamicShortcuts() {
        if ("robolectric" != Build.FINGERPRINT && !Settings.firstStart) {
            Thread {
                try {
                    Thread.sleep(10 * TimeUtils.SECOND)
                    val source = DataSource

                    var conversations = source.getPinnedConversationsAsList(this)
                    if (conversations.isEmpty()) {
                        conversations = source.getUnarchivedConversationsAsList(this)
                    }

                    DynamicShortcutUtils(this@MessengerApplication).buildDynamicShortcuts(conversations)
                } catch (e: Exception) {
                }
            }.start()
        }
    }

    override fun getFirebaseMessageHandler(): FirebaseMessageHandler {
        return object : FirebaseMessageHandler {
            override fun handleMessage(application: Application, operation: String, data: String) {
                Thread { FirebaseHandlerService.process(application, operation, data) }.start()
            }

            override fun handleDelete(application: Application) {
                val handleMessage = Intent(application, FirebaseResetService::class.java)
                if (AndroidVersionUtil.isAndroidO) {
                    startForegroundService(handleMessage)
                } else {
                    startService(handleMessage)
                }
            }
        }
    }

    override fun onAddConversationError(conversationId: Long) {
        if (!Account.exists() || !Account.primary) {
            return
        }

        Thread {
            DataSource.insertRetryableRequest(this,
                    RetryableRequest(RetryableRequest.TYPE_ADD_CONVERSATION, conversationId, TimeUtils.now))
        }.start()
    }

    override fun onAddMessageError(messageId: Long) {
        if (!Account.exists() || !Account.primary) {
            return
        }

        Thread {
            DataSource.insertRetryableRequest(this,
                    RetryableRequest(RetryableRequest.TYPE_ADD_MESSAGE, messageId, TimeUtils.now))
        }.start()
    }

    override fun onAccountInvalidated(account: Account) {
        DataSource.invalidateAccountDetails()
    }

    companion object {

        /**
         * By default, java does not allow for strong security schemes due to export laws in other
         * countries. This gets around that. Might not be necessary on Android, but we'll put it here
         * anyways just in case.
         */
        private fun enableSecurity() {
            try {
                val field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted")
                field.isAccessible = true
                field.set(null, java.lang.Boolean.FALSE)
            } catch (e: Exception) {

            }

        }
    }
}
