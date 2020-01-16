package xyz.stream.messenger.api.implementation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64

import java.util.Date

import javax.crypto.spec.SecretKeySpec

import xyz.stream.messenger.encryption.EncryptionUtils
import xyz.stream.messenger.encryption.KeyUtils

@SuppressLint("ApplySharedPref")
object Account {

    @JvmStatic
    val QUICK_SIGN_UP_SYSTEM = false

    enum class SubscriptionType constructor(var typeCode: Int) {
        TRIAL(1), SUBSCRIBER(2), LIFETIME(3), FREE_TRIAL(4), FINISHED_FREE_TRIAL_WITH_NO_ACCOUNT_SETUP(5);

        companion object {
            fun findByTypeCode(code: Int): xyz.stream.messenger.api.implementation.Account.SubscriptionType? {
                return values().firstOrNull { it.typeCode == code }
            }
        }
    }

    var encryptor: xyz.stream.messenger.encryption.EncryptionUtils? = null
        private set

    var primary: Boolean = false
    var trialStartTime: Long = 0
    var subscriptionType: xyz.stream.messenger.api.implementation.Account.SubscriptionType? = null
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
        val sharedPrefs = xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context)

        // account info
        xyz.stream.messenger.api.implementation.Account.primary = sharedPrefs.getBoolean(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_primary), false)
        xyz.stream.messenger.api.implementation.Account.subscriptionType = xyz.stream.messenger.api.implementation.Account.SubscriptionType.Companion.findByTypeCode(sharedPrefs.getInt(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_subscription_type), 1))
        xyz.stream.messenger.api.implementation.Account.subscriptionExpiration = sharedPrefs.getLong(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_subscription_expiration), -1)
        xyz.stream.messenger.api.implementation.Account.trialStartTime = sharedPrefs.getLong(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_trial_start), -1)
        xyz.stream.messenger.api.implementation.Account.myName = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_my_name), null)
        xyz.stream.messenger.api.implementation.Account.myPhoneNumber = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_my_phone_number), null)
        xyz.stream.messenger.api.implementation.Account.deviceId = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_device_id), null)
        xyz.stream.messenger.api.implementation.Account.accountId = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_account_id), null)
        xyz.stream.messenger.api.implementation.Account.salt = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_salt), null)
        xyz.stream.messenger.api.implementation.Account.passhash = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_passhash), null)
        xyz.stream.messenger.api.implementation.Account.key = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_key), null)

        xyz.stream.messenger.api.implementation.Account.hasPurchased = sharedPrefs.getBoolean(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_has_purchased), false)

        if (xyz.stream.messenger.api.implementation.Account.key == null && xyz.stream.messenger.api.implementation.Account.passhash != null && xyz.stream.messenger.api.implementation.Account.accountId != null && xyz.stream.messenger.api.implementation.Account.salt != null) {
            // we have all the requirements to recompute the key,
            // not sure why this wouldn't have worked in the first place..
            xyz.stream.messenger.api.implementation.Account.recomputeKey(context)
            xyz.stream.messenger.api.implementation.Account.key = sharedPrefs.getString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_key), null)

            val secretKey = SecretKeySpec(Base64.decode(xyz.stream.messenger.api.implementation.Account.key, Base64.DEFAULT), "AES")
            xyz.stream.messenger.api.implementation.Account.encryptor = xyz.stream.messenger.encryption.EncryptionUtils(secretKey)
        } else if (xyz.stream.messenger.api.implementation.Account.key == null && xyz.stream.messenger.api.implementation.Account.accountId != null) {
            // we cannot compute the key, uh oh. lets just start up the login activity and grab them...
            // This will do little good if they are on the api utils and trying to send a message or
            // something, or receiving a message. But they will have to re-login sometime I guess
            context.startActivity(Intent(context, xyz.stream.messenger.api.implementation.LoginActivity::class.java))
        } else if (xyz.stream.messenger.api.implementation.Account.key != null) {
            val secretKey = SecretKeySpec(Base64.decode(xyz.stream.messenger.api.implementation.Account.key, Base64.DEFAULT), "AES")
            xyz.stream.messenger.api.implementation.Account.encryptor = xyz.stream.messenger.encryption.EncryptionUtils(secretKey)
        }

        val application = context.applicationContext
        if (application is xyz.stream.messenger.api.implementation.AccountInvalidator) {
            application.onAccountInvalidated(xyz.stream.messenger.api.implementation.Account)
        }
    }

    fun getSharedPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    }

    fun forceUpdate(context: Context): xyz.stream.messenger.api.implementation.Account {
        xyz.stream.messenger.api.implementation.Account.init(context)
        return xyz.stream.messenger.api.implementation.Account
    }

    fun clearAccount(context: Context) {
        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .remove(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_account_id))
                .remove(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_salt))
                .remove(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_passhash))
                .remove(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_key))
                .remove(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_subscription_type))
                .remove(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_subscription_expiration))
                .commit()

        xyz.stream.messenger.api.implementation.Account.init(context)
    }

    fun updateSubscription(context: Context, type: xyz.stream.messenger.api.implementation.Account.SubscriptionType, expiration: Date?) {
        xyz.stream.messenger.api.implementation.Account.updateSubscription(context, type, expiration?.time, true)
    }

    fun updateSubscription(context: Context, type: xyz.stream.messenger.api.implementation.Account.SubscriptionType?, expiration: Long?, sendToApi: Boolean) {
        xyz.stream.messenger.api.implementation.Account.subscriptionType = type
        xyz.stream.messenger.api.implementation.Account.subscriptionExpiration = expiration!!

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putInt(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_subscription_type), type?.typeCode ?: 0)
                .putLong(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_subscription_expiration), expiration)
                .commit()

        if (sendToApi) {
            xyz.stream.messenger.api.implementation.ApiUtils.updateSubscription(xyz.stream.messenger.api.implementation.Account.accountId, type?.typeCode, expiration)
        }
    }

    fun setName(context: Context, name: String?) {
        xyz.stream.messenger.api.implementation.Account.myName = name

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_my_name), name)
                .commit()
    }

    fun setPhoneNumber(context: Context, phoneNumber: String?) {
        xyz.stream.messenger.api.implementation.Account.myPhoneNumber = phoneNumber

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_my_name), phoneNumber)
                .commit()
    }

    fun setPrimary(context: Context, primary: Boolean) {
        xyz.stream.messenger.api.implementation.Account.primary = primary

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putBoolean(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_primary), primary)
                .commit()
    }

    fun setDeviceId(context: Context, deviceId: String?) {
        xyz.stream.messenger.api.implementation.Account.deviceId = deviceId

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_device_id), deviceId)
                .commit()
    }

    fun setHasPurchased(context: Context, hasPurchased: Boolean) {
        xyz.stream.messenger.api.implementation.Account.hasPurchased = hasPurchased

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putBoolean(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_has_purchased), hasPurchased)
                .commit()
    }

    fun recomputeKey(context: Context) {
        val keyUtils = xyz.stream.messenger.encryption.KeyUtils()
        val key = keyUtils.createKey(xyz.stream.messenger.api.implementation.Account.passhash, xyz.stream.messenger.api.implementation.Account.accountId, xyz.stream.messenger.api.implementation.Account.salt)

        val encodedKey = Base64.encodeToString(key.encoded, Base64.DEFAULT)

        xyz.stream.messenger.api.implementation.Account.getSharedPrefs(context).edit()
                .putString(context.getString(xyz.stream.messenger.api.implementation.R.string.api_pref_key), encodedKey)
                .commit()
    }

    fun exists(): Boolean {
        return xyz.stream.messenger.api.implementation.Account.accountId != null && !xyz.stream.messenger.api.implementation.Account.accountId!!.isEmpty() && xyz.stream.messenger.api.implementation.Account.deviceId != null && xyz.stream.messenger.api.implementation.Account.salt != null && xyz.stream.messenger.api.implementation.Account.passhash != null
                && xyz.stream.messenger.api.implementation.Account.key != null
    }

    private const val TRIAL_LENGTH = 7 // days
    fun getDaysLeftInTrial(): Int {
        return if (xyz.stream.messenger.api.implementation.Account.subscriptionType == xyz.stream.messenger.api.implementation.Account.SubscriptionType.FREE_TRIAL) {
            val now = Date().time
            val timeInTrial = now - xyz.stream.messenger.api.implementation.Account.trialStartTime
            val trialLength = 1000 * 60 * 60 * 24 * xyz.stream.messenger.api.implementation.Account.TRIAL_LENGTH
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
