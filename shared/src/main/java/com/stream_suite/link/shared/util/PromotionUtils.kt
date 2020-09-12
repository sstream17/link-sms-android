package com.stream_suite.link.shared.util

import android.app.Activity
import android.os.Handler
import com.sensortower.rating.RatingPrompt
import com.sensortower.rating.RatingPromptOptions
import com.sensortower.rating.RatingPromptSettings
import com.stream_suite.link.api.implementation.Account
import com.stream_suite.link.shared.data.Settings

class PromotionUtils(private val context: Activity) {

    fun checkPromotions(onTrialExpired: () -> Unit) {
        if (trialExpired()) {
            onTrialExpired()
        } else {
            askForRating()
        }
    }

    private fun trialExpired(): Boolean {
        return Account.exists() && Account.subscriptionType == Account.SubscriptionType.FREE_TRIAL && Account.getDaysLeftInTrial() <= 0
    }

    private fun askForRating() {
        if (Account.exists() && !Account.primary) {
            // only prompt for rating on the primary device
            return
        }

        val settings = RatingPromptSettings.getInstance(context)
        val sharedPrefs = Settings.getSharedPrefs(context)

        if (sharedPrefs.getBoolean("update_rating_method", true)) {
            sharedPrefs.edit().putBoolean("update_rating_method", false).commit()

            settings.putBoolean("rating-prompt-dont-show-again", false)
            settings.putLong("rating-prompt-should-show-at-timestamp", -1L)
        }

        Handler().postDelayed({
            RatingPrompt.show(context, RatingPromptOptions.Builder()
                    .useLegacy(RatingPromptOptions.Legacy.Builder("Pulse")
                            .useEmojis(false)
                            .accentColor(Settings.mainColorSet.color)
                            .build()
                    ).build())
        }, 500)
    }
    
}
