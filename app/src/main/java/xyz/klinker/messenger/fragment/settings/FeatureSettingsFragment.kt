package xyz.klinker.messenger.fragment.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import xyz.klinker.messenger.R
import xyz.klinker.messenger.activity.SettingsActivity
import xyz.klinker.messenger.api.implementation.Account
import xyz.klinker.messenger.api.implementation.ApiUtils
import xyz.klinker.messenger.activity.passcode.PasscodeSetupActivity
import xyz.klinker.messenger.activity.passcode.PasscodeVerificationActivity
import xyz.klinker.messenger.shared.data.Settings
import xyz.klinker.messenger.shared.service.QuickComposeNotificationService
import xyz.klinker.messenger.shared.util.RedirectToMyAccount

class FeatureSettingsFragment : MaterialPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.settings_features)
        initSecurePrivateConversations()
        initSmartReplies()
        initSmartReplyTimeout()
        initInternalBrowser()
        initQuickCompose()
        initDelayedSending()
        initCleanupOldMessages()
        initSignature()
        initMessageBackup()
        initAutoReplyConfiguration()
    }

    override fun onStop() {
        super.onStop()
        Settings.forceUpdate(activity)
    }

    private fun initSecurePrivateConversations() {
        val preference = findPreference(getString(R.string.pref_secure_private_conversations))

        preference.setOnPreferenceClickListener {
            val showPasscodeSetup: () -> Unit = {
                startActivity(Intent(activity, PasscodeSetupActivity::class.java))
            }

            if (Settings.privateConversationsPasscode.isNullOrBlank()) {
                showPasscodeSetup()
            } else {
                PasscodeVerificationActivity.show(activity as FragmentActivity, showPasscodeSetup)
            }

            true
        }
    }

    private fun initSmartReplies() {
        val preference = findPreference(getString(R.string.pref_smart_reply))
        preference.setOnPreferenceChangeListener { _, o ->
            val useSmartReplies = o as Boolean
            ApiUtils.updateSmartReplies(Account.accountId, useSmartReplies)
            true
        }
    }

    private fun initSmartReplyTimeout() {
        val preference = findPreference(getString(R.string.pref_smart_reply_timeout))
        preference.setOnPreferenceChangeListener { _, o ->
            val useSmartReplyTimeout = o as Boolean
            ApiUtils.updateSmartReplyTimeout(Account.accountId, useSmartReplyTimeout)
            true
        }
    }

    private fun initInternalBrowser() {
        val preference = findPreference(getString(R.string.pref_internal_browser))
        preference.setOnPreferenceChangeListener { _, o ->
            val useBrowser = o as Boolean
            ApiUtils.updateInternalBrowser(Account.accountId, useBrowser)
            true
        }
    }

    private fun initQuickCompose() {
        val preference = findPreference(getString(R.string.pref_quick_compose))
        preference.setOnPreferenceChangeListener { _, o ->
            val quickCompose = o as Boolean
            ApiUtils.updateQuickCompose(Account.accountId, quickCompose)

            if (quickCompose) {
                QuickComposeNotificationService.start(activity)
            } else {
                QuickComposeNotificationService.stop(activity)
            }

            true
        }
    }

    private fun initDelayedSending() {
        val preference = findPreference(getString(R.string.pref_delayed_sending))
        preference.setOnPreferenceChangeListener { _, o ->
            val delayedSending = o as String
            ApiUtils.updateDelayedSending(
                    Account.accountId, delayedSending)
            true
        }
    }

    private fun initCleanupOldMessages() {
        val preference = findPreference(getString(R.string.pref_cleanup_messages))
        preference.setOnPreferenceChangeListener { _, o ->
            val cleanup = o as String
            ApiUtils.updateCleanupOldMessages(
                    Account.accountId, cleanup)
            true
        }
    }

    private fun initSignature() {
        findPreference(getString(R.string.pref_signature)).setOnPreferenceClickListener {
            val layout = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_text, null, false)
            val editText = layout.findViewById<View>(R.id.edit_text) as EditText
            editText.setHint(R.string.signature)
            editText.setText(Settings.signature)
            editText.setSelection(editText.text.length)

            AlertDialog.Builder(activity)
                    .setView(layout)
                    .setPositiveButton(R.string.save) { _, _ ->
                        val signature = editText.text.toString()
                        Settings.setValue(activity,
                                activity.getString(R.string.pref_signature), signature)
                        if (editText.text.isNotEmpty()) {
                            ApiUtils.updateSignature(Account.accountId,
                                    signature)
                        } else {
                            ApiUtils.updateSignature(Account.accountId, "")
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

            false
        }
    }

    private fun initMessageBackup() {
        findPreference(getString(R.string.pref_message_backup)).setOnPreferenceClickListener {
            if (Account.exists()) {
                AlertDialog.Builder(activity)
                        .setMessage(R.string.message_backup_summary_have_account)
                        .show()
            } else {
                AlertDialog.Builder(activity)
                        .setMessage(R.string.message_backup_summary)
                        .setPositiveButton(R.string.try_it) { _, _ ->
                            startActivity(Intent(activity, RedirectToMyAccount::class.java))
                        }.show()
            }

            false
        }
    }

    private fun initAutoReplyConfiguration() {
        val preference = findPreference(getString(R.string.pref_auto_reply))
        preference.setOnPreferenceClickListener {
            SettingsActivity.startAutoReplySettings(activity)
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PasscodeVerificationActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startActivity(Intent(activity, PasscodeSetupActivity::class.java))
        }
    }
}
