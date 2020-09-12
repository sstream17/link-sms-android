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

package com.stream_suite.link.shared.receiver

import android.content.Context
import android.content.Intent
import com.stream_suite.link.api.implementation.Account

/**
 * Receiver which gets a notification when an MMS message has finished sending. It will mark the
 * message as sent in the database by default. We also need to add functionality for marking it
 * as sent in our own database.
 */
class MmsSentReceiver : com.klinker.android.send_message.MmsSentReceiver() {

    override fun updateInInternalDatabase(context: Context, intent: Intent, resultCode: Int) {
        Thread { super.updateInInternalDatabase(context, intent, resultCode) }.start()
    }

    override fun onMessageStatusUpdated(context: Context, intent: Intent, receiverResultCode: Int) {
        if (Account.exists() && !Account.primary) {
            return
        }

        SmsSentReceiver.markLatestAsRead(context)
    }

}
