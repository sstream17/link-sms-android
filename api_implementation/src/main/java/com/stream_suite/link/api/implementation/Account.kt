package com.stream_suite.link.api.implementation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64

import java.util.Date

import javax.crypto.spec.SecretKeySpec

@SuppressLint("ApplySharedPref")
object Account {

    @JvmStatic
    val QUICK_SIGN_UP_SYSTEM = false

    enum class SubscriptionType constructor(var typeCode: Int) {
        TRIAL(1), SUBSCRIBER(2), LIFETIME(3), FREE_TRIAL(4), FINISHED_FREE_TRIAL_WITH_NO_ACCOUNT_SETUP(5);

        companion object {
            fun findByTypeCode(code: Int): com.stream_suite.link.api.implementation.Account.SubscriptionType? {
                return values().firstOrNull { it.typeCode == code }
            }
        }
    }

    var encryptor: com.stream_suite.link.encryption.EncryptionUtils? = null
        private set

    var primary: Boolean = false
    var trialStartTime: Long = 0
    var subscriptionType: com.stream_suite.link.api.implementation.Account.SubscriptionType? = null
    var subscriptionExpiration: Long = 0
    var myName: String? = null
    var myPhoneNumber: String? = null
    var deviceId: String? = null
    var accountId: String? = null
    var salt: String? = null
    var passhash: String? = null
    var key: String? = null

    var hasPurchased: Boolean = false

    fun init(context: Context) {
        val sharedPrefs = com.stream_suite.link.api.implementation.Account.getSharedPrefs(context)

        // account info
        com.stream_suite.link.api.implementation.Account.primary = sharedPrefs.getBoolean(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_primary), false)
        com.stream_suite.link.api.implementation.Account.subscriptionType = com.stream_suite.link.api.implementation.Account.SubscriptionType.Companion.findByTypeCode(sharedPrefs.getInt(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_subscription_type), 1))
        com.stream_suite.link.api.implementation.Account.subscriptionExpiration = sharedPrefs.getLong(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_subscription_expiration), -1)
        com.stream_suite.link.api.implementation.Account.trialStartTime = sharedPrefs.getLong(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_trial_start), -1)
        com.stream_suite.link.api.implementation.Account.myName = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_my_name), null)
        com.stream_suite.link.api.implementation.Account.myPhoneNumber = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_my_phone_number), null)
        com.stream_suite.link.api.implementation.Account.deviceId = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_device_id), null)
        com.stream_suite.link.api.implementation.Account.accountId = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_account_id), null)
        com.stream_suite.link.api.implementation.Account.salt = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_salt), null)
        com.stream_suite.link.api.implementation.Account.passhash = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_passhash), null)
        com.stream_suite.link.api.implementation.Account.key = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_key), null)

        com.stream_suite.link.api.implementation.Account.hasPurchased = sharedPrefs.getBoolean(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_has_purchased), false)

        if (com.stream_suite.link.api.implementation.Account.key == null && com.stream_suite.link.api.implementation.Account.passhash != null && com.stream_suite.link.api.implementation.Account.accountId != null && com.stream_suite.link.api.implementation.Account.salt != null) {
            // we have all the requirements to recompute the key,
            // not sure why this wouldn't have worked in the first place..
            com.stream_suite.link.api.implementation.Account.recomputeKey(context)
            com.stream_suite.link.api.implementation.Account.key = sharedPrefs.getString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_key), null)

            val secretKey = SecretKeySpec(Base64.decode(com.stream_suite.link.api.implementation.Account.key, Base64.DEFAULT), "AES")
            com.stream_suite.link.api.implementation.Account.encryptor = com.stream_suite.link.encryption.EncryptionUtils(secretKey)
        } else if (com.stream_suite.link.api.implementation.Account.key == null && com.stream_suite.link.api.implementation.Account.accountId != null) {
            // we cannot compute the key, uh oh. lets just start up the login activity and grab them...
            // This will do little good if they are on the api utils and trying to send a message or
            // something, or receiving a message. But they will have to re-login sometime I guess
            context.startActivity(Intent(context, com.stream_suite.link.api.implementation.LoginActivity::class.java))
        } else if (com.stream_suite.link.api.implementation.Account.key != null) {
            val secretKey = SecretKeySpec(Base64.decode(com.stream_suite.link.api.implementation.Account.key, Base64.DEFAULT), "AES")
            com.stream_suite.link.api.implementation.Account.encryptor = com.stream_suite.link.encryption.EncryptionUtils(secretKey)
        }

        val application = context.applicationContext
        if (application is com.stream_suite.link.api.implementation.AccountInvalidator) {
            application.onAccountInvalidated(com.stream_suite.link.api.implementation.Account)
        }
    }

    fun getSharedPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    }

    fun forceUpdate(context: Context): com.stream_suite.link.api.implementation.Account {
        com.stream_suite.link.api.implementation.Account.init(context)
        return com.stream_suite.link.api.implementation.Account
    }

    fun clearAccount(context: Context) {
        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .remove(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_account_id))
                .remove(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_salt))
                .remove(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_passhash))
                .remove(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_key))
                .remove(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_subscription_type))
                .remove(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_subscription_expiration))
                .commit()

        com.stream_suite.link.api.implementation.Account.init(context)
    }

    fun updateSubscription(context: Context, type: com.stream_suite.link.api.implementation.Account.SubscriptionType, expiration: Date?) {
        com.stream_suite.link.api.implementation.Account.updateSubscription(context, type, expiration?.time, true)
    }

    fun updateSubscription(context: Context, type: com.stream_suite.link.api.implementation.Account.SubscriptionType?, expiration: Long?, sendToApi: Boolean) {
        com.stream_suite.link.api.implementation.Account.subscriptionType = type
        com.stream_suite.link.api.implementation.Account.subscriptionExpiration = expiration!!

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putInt(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_subscription_type), type?.typeCode ?: 0)
                .putLong(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_subscription_expiration), expiration)
                .commit()

        if (sendToApi) {
            com.stream_suite.link.api.implementation.ApiUtils.updateSubscription(com.stream_suite.link.api.implementation.Account.accountId, type?.typeCode, expiration)
        }
    }

    fun setName(context: Context, name: String?) {
        com.stream_suite.link.api.implementation.Account.myName = name

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_my_name), name)
                .commit()
    }

    fun setPhoneNumber(context: Context, phoneNumber: String?) {
        com.stream_suite.link.api.implementation.Account.myPhoneNumber = phoneNumber

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_my_name), phoneNumber)
                .commit()
    }

    fun setPrimary(context: Context, primary: Boolean) {
        com.stream_suite.link.api.implementation.Account.primary = primary

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putBoolean(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_primary), primary)
                .commit()
    }

    fun setDeviceId(context: Context, deviceId: String?) {
        com.stream_suite.link.api.implementation.Account.deviceId = deviceId

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_device_id), deviceId)
                .commit()
    }

    fun setHasPurchased(context: Context, hasPurchased: Boolean) {
        com.stream_suite.link.api.implementation.Account.hasPurchased = hasPurchased

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putBoolean(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_has_purchased), hasPurchased)
                .commit()
    }

    fun recomputeKey(context: Context) {
        val keyUtils = com.stream_suite.link.encryption.KeyUtils()
        val key = keyUtils.createKey(com.stream_suite.link.api.implementation.Account.passhash, com.stream_suite.link.api.implementation.Account.accountId, com.stream_suite.link.api.implementation.Account.salt)

        val encodedKey = Base64.encodeToString(key.encoded, Base64.DEFAULT)

        com.stream_suite.link.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(com.stream_suite.link.api.implementation.R.string.api_pref_key), encodedKey)
                .commit()
    }

    fun exists(): Boolean {
        return com.stream_suite.link.api.implementation.Account.accountId != null && !com.stream_suite.link.api.implementation.Account.accountId!!.isEmpty() && com.stream_suite.link.api.implementation.Account.deviceId != null && com.stream_suite.link.api.implementation.Account.salt != null && com.stream_suite.link.api.implementation.Account.passhash != null
                && com.stream_suite.link.api.implementation.Account.key != null
    }

    private const val TRIAL_LENGTH = 7 // days
    fun getDaysLeftInTrial(): Int {
        return if (com.stream_suite.link.api.implementation.Account.subscriptionType == com.stream_suite.link.api.implementation.Account.SubscriptionType.FREE_TRIAL) {
            val now = Date().time
            val timeInTrial = now - com.stream_suite.link.api.implementation.Account.trialStartTime
            val trialLength = 1000 * 60 * 60 * 24 * com.stream_suite.link.api.implementation.Account.TRIAL_LENGTH
            if (timeInTrial > trialLength) {
                0
            } else {
                val timeLeftInTrial = trialLength - timeInTrial
                val timeInDays = (timeLeftInTrial / (1000 * 60 * 60 * 24)) + 1
                timeInDays.toInt()
            }
        } else {
            0
        }
    }
}
