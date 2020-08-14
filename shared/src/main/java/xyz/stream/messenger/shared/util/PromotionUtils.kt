package xyz.stream.messenger.shared.util

import android.app.Activity
import android.content.SharedPreferences
import android.os.Handler
import com.sensortower.rating.RatingPrompt
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.shared.data.Settings

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

    // The "rate-it" library will manage whether or not it has been shown or whether it needs to be shown.
    // If the user has already rated the app, Google Play will manage this case.

    private fun askForRating() {
        Handler().postDelayed({
            RatingPrompt.show(context)
        }, 500)
    }
    
}
