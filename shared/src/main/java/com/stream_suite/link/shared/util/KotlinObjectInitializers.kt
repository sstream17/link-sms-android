package com.stream_suite.link.shared.util

import android.content.Context
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.api.implementation.ApiUtils
import com.stream_suite.link.shared.R
import com.stream_suite.link.shared.data.FeatureFlags
import com.stream_suite.link.shared.data.MmsSettings
import com.stream_suite.link.shared.data.Settings

object KotlinObjectInitializers {

    fun initializeObjects(context: Context) {
        try {
            ApiUtils.environment = context.getString(R.string.environment)
        } catch (e: Exception) {
            ApiUtils.environment = "release"
        }

        Account.init(context)
        FeatureFlags.init(context)
        Settings.init(context)
        MmsSettings.init(context)
        DualSimUtils.init(context)
        EmojiInitializer.initializeEmojiCompat(context)
    }
}