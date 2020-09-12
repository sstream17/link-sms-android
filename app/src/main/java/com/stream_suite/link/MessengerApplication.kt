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

package com.stream_suite.link

import android.app.Application
import android.content.Intent
import android.os.Build
import com.sensortower.events.EventHandler
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.api.implementation.AccountInvalidator
import com.stream_suite.link.api.implementation.firebase.AnalyticsHelper
import com.stream_suite.link.api.implementation.firebase.FirebaseApplication
import com.stream_suite.link.api.implementation.firebase.FirebaseMessageHandler
import com.stream_suite.link.api.implementation.retrofit.ApiErrorPersister
import com.stream_suite.link.shared.data.DataSource
import com.stream_suite.link.shared.data.Settings
import com.stream_suite.link.shared.data.model.RetryableRequest
import com.stream_suite.link.shared.service.FirebaseHandlerService
import com.stream_suite.link.shared.service.FirebaseResetService
import com.stream_suite.link.shared.service.QuickComposeNotificationService
import com.stream_suite.link.shared.service.notification.ShortcutUpdater
import com.stream_suite.link.shared.util.*
import com.stream_suite.link.shared.util.UpdateUtils

/**
 * Base application that will serve as any intro for any context in the rest of the app. Main
 * function is to enable night mode so that colors change depending on time of day.
 */
class MessengerApplication : FirebaseApplication(), ApiErrorPersister, AccountInvalidator, EventHandler.Provider, ShortcutUpdater {

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

    override fun refreshDynamicShortcuts(delay: Long) {
        if ("robolectric" != Build.FINGERPRINT && !Settings.firstStart) {
            val update = {
                val conversations = DataSource.getUnarchivedConversationsAsList(this)
                DynamicShortcutUtils(this@MessengerApplication).buildDynamicShortcuts(conversations)
            }

            if (delay == 0L) try {
                update()
                return
            } catch (e: Exception) {
            }

            Thread {
                try {
                    Thread.sleep(delay)
                    update()
                } catch (e: Exception) {
                    e.printStackTrace()
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

    override val eventHandler: EventHandler
        get() = object : EventHandler {
            override fun onAnalyticsEvent(type: String, message: String?) {
                AnalyticsHelper.logEvent(this@MessengerApplication, type)
            }
        }
}
