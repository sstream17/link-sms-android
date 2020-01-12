package xyz.stream.messenger.shared.util

import android.app.Activity
import android.app.job.JobScheduler
import android.content.Context
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.WorkManager
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.api.implementation.firebase.ScheduledTokenRefreshService
import xyz.stream.messenger.shared.R
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.SwipeOption
import xyz.stream.messenger.shared.service.ContactResyncService
import xyz.stream.messenger.shared.service.jobs.*

class UpdateUtils(private val context: Activity) {

    private val appVersion: Int
        get() = try {
            val packageInfo = context.packageManager
                    .getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }

    fun checkForUpdate(): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val storedAppVersion = sharedPreferences.getInt("app_version", 0)
        ContactResyncService.runIfApplicable(context, sharedPreferences, storedAppVersion)

        if (sharedPreferences.getBoolean("swipe_revamp", true)) {
            sharedPreferences.edit().putBoolean("swipe_revamp", false).commit()
            if (Settings.legacySwipeDelete) {
                Settings.setValue(context, context.getString(R.string.pref_right_to_left_swipe), SwipeOption.DELETE.rep)
                ApiUtils.updateRightToLeftSwipeAction(Account.accountId, SwipeOption.DELETE.rep)
            }
        }

        val currentAppVersion = appVersion

        return if (storedAppVersion != currentAppVersion) {
            Log.v(TAG, "new app version")
            sharedPreferences.edit().putInt("app_version", currentAppVersion).apply()
            true
        } else {
            false
        }
    }

    companion object {

        private const val TAG = "UpdateUtil"

        fun rescheduleWork(context: Context) {
            WorkManager.getInstance().cancelAllWork()

            CleanupOldMessagesWork.scheduleNextRun(context)
            FreeTrialNotifierWork.scheduleNextRun(context)
            ScheduledMessageJob.scheduleNextRun(context)
            ContactSyncWork.scheduleNextRun(context)
            SubscriptionExpirationCheckJob.scheduleNextRun(context)
            SignoutJob.scheduleNextRun(context)
            xyz.stream.messenger.api.implementation.firebase.ScheduledTokenRefreshService.scheduleNextRun(context)
            SyncRetryableRequestsWork.scheduleNextRun(context)
            RepostQuickComposeNotificationWork.scheduleNextRun(context)
        }

    }
}
