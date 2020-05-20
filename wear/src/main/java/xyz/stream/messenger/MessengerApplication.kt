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

package xyz.stream.messenger

import android.app.Application
import android.content.Intent
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.AccountInvalidator

import xyz.stream.messenger.api.implementation.firebase.FirebaseApplication
import xyz.stream.messenger.api.implementation.firebase.FirebaseMessageHandler
import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.service.FirebaseHandlerService
import xyz.stream.messenger.shared.service.FirebaseResetService
import xyz.stream.messenger.shared.util.*

/**
 * Base application that will serve as any intro for any context in the rest of the app. Main
 * function is to enable night mode so that colors change depending on time of day.
 */
class MessengerApplication : xyz.stream.messenger.api.implementation.firebase.FirebaseApplication(), AccountInvalidator {

    override fun onCreate() {
        super.onCreate()

        KotlinObjectInitializers.initializeObjects(this)
        UpdateUtils.rescheduleWork(this)

        enableSecurity()

        TimeUtils.setupNightTheme()
        NotificationUtils.createNotificationChannels(this)
    }

    override fun getFirebaseMessageHandler(): xyz.stream.messenger.api.implementation.firebase.FirebaseMessageHandler {
        return object : xyz.stream.messenger.api.implementation.firebase.FirebaseMessageHandler {
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