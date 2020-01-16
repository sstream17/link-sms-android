package xyz.stream.messenger.shared.util

import android.content.Context
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.FeatureFlags
import xyz.stream.messenger.shared.data.MmsSettings
import xyz.stream.messenger.shared.data.Settings

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