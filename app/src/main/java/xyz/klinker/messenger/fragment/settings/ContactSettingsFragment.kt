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

package xyz.klinker.messenger.fragment.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.*
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import xyz.klinker.messenger.R
import xyz.klinker.messenger.activity.compose.ComposeActivity
import xyz.klinker.messenger.activity.compose.ComposeConstants
import xyz.klinker.messenger.shared.activity.AbstractSettingsActivity
import xyz.klinker.messenger.shared.data.ColorSet
import xyz.klinker.messenger.shared.data.DataSource
import xyz.klinker.messenger.shared.data.FeatureFlags
import xyz.klinker.messenger.shared.data.Settings
import xyz.klinker.messenger.shared.data.model.Conversation
import xyz.klinker.messenger.shared.util.*
import xyz.klinker.messenger.shared.util.listener.ColorSelectedListener
import xyz.klinker.messenger.view.ColorPreference

/**
 * Fragment for modifying contact preferences. This includes pinning, changing colors, changing
 * ringtone, and changing the group name.
 */
class ContactSettingsFragment : MaterialPreferenceFragment() {

    val conversation: Conversation by lazy { DataSource.getConversation(activity, arguments.getLong(ARG_CONVERSATION_ID))!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpDefaults()
        addPreferencesFromResource(R.xml.settings_contact)

        setUpToolbar()
        setUpPin()
        setUpMute()
        setUpPrivate()
        setUpGroupName()
        setUpEditRecipients()
        setUpFolder()
        setUpCleanupOldMessages()
        setUpRingtone()
        setUpNotificationChannels()
        setUpColors()

        if (Settings.useGlobalThemeColor) {
            // remove the color customizations since they don't apply to anything except group messages
            val customizationCategory = findPreference(getString(R.string.pref_contact_customization_group)) as PreferenceCategory
            preferenceScreen.removePreference(customizationCategory)
        }

        if (AndroidVersionUtil.isAndroidO) {
            // remove the LED customizations since the user won't be able to configure this here, they will
            // have to go through channels instead.
            // The ringtone pref has been converted to push them to notification channel settings
            val notificationCategory = findPreference(getString(R.string.pref_contact_notification_group)) as PreferenceCategory
            notificationCategory.removePreference(findPreference(getString(R.string.pref_contact_led_color)))
            notificationCategory.removePreference(findPreference(getString(R.string.pref_contact_ringtone)))
        }
    }

    private fun setUpDefaults() {
        PreferenceManager.getDefaultSharedPreferences(activity).edit()
                .putBoolean(getString(R.string.pref_contact_pin_conversation), conversation.pinned)
                .putBoolean(getString(R.string.pref_contact_private_conversation), conversation.private)
                .putString(getString(R.string.pref_contact_group_name), conversation.title)
                .putString(getString(R.string.pref_contact_ringtone),
                        if (conversation.ringtoneUri == null)
                            Settings.ringtone
                        else
                            conversation.ringtoneUri)
                .putInt(getString(R.string.pref_contact_primary_color), conversation.colors.color)
                .putInt(getString(R.string.pref_contact_primary_dark_color), conversation.colors.colorDark)
                .putInt(getString(R.string.pref_contact_primary_light_color), conversation.colors.colorLight)
                .putInt(getString(R.string.pref_contact_accent_color), conversation.colors.colorAccent)
                .putInt(getString(R.string.pref_contact_led_color), conversation.ledColor)
                .apply()
    }

    private fun setUpToolbar() {
        activity.title = conversation.title

        val toolbar = (activity as AbstractSettingsActivity).toolbar

        if (Settings.useGlobalThemeColor) {
            toolbar?.setBackgroundColor(Settings.mainColorSet.color)
            ActivityUtils.setStatusBarColor(activity, Settings.mainColorSet.colorDark)
        } else {
            toolbar?.setBackgroundColor(conversation.colors.color)
            ActivityUtils.setStatusBarColor(activity, conversation.colors.colorDark)
        }
    }

    private fun setUpPin() {
        val preference = findPreference(getString(R.string.pref_contact_pin_conversation)) as SwitchPreference
        preference.isChecked = conversation.pinned

        preference.setOnPreferenceChangeListener { _, o ->
            conversation.pinned = o as Boolean
            true
        }
    }

    private fun setUpMute() {
        val preference = findPreference(getString(R.string.pref_contact_mute_conversation)) as SwitchPreference
        preference.isChecked = conversation.mute
        enableNotificationBasedOnMute(conversation.mute)

        preference.setOnPreferenceChangeListener { _, o ->
            conversation.mute = o as Boolean
            enableNotificationBasedOnMute(conversation.mute)

            true
        }
    }

    private fun enableNotificationBasedOnMute(mute: Boolean) {
        val ringtone = findPreference(getString(R.string.pref_contact_ringtone))

        if (mute) {
            if (ringtone != null) {
                ringtone.isEnabled = false
            }
        } else {
            if (ringtone != null) {
                ringtone.isEnabled = true
            }
        }
    }

    private fun setUpGroupName() {
        val preference = findPreference(getString(R.string.pref_contact_group_name)) as EditTextPreference

        if (!conversation.isGroup) {
            preference.title = getString(R.string.conversation_name)
        }

        preference.editText.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        preference.summary = conversation.title
        preference.setOnPreferenceChangeListener { preference1, o ->
            conversation.title = o as String
            preference1.summary = conversation.title

            ActivityUtils.setTaskDescription(activity, conversation.title!!, conversation.colors.color)
            activity.title = conversation.title

            true
        }
    }

    private fun setUpEditRecipients() {
        val preference = findPreference(getString(R.string.pref_contact_edit_recipients))

        if (!conversation.isGroup) {
            preferenceScreen.removePreference(preference)
            return
        }

        preference.setOnPreferenceClickListener {
            val editRecipients = Intent(activity, ComposeActivity::class.java)
            editRecipients.action = ComposeConstants.ACTION_EDIT_RECIPIENTS
            editRecipients.putExtra(ComposeConstants.EXTRA_EDIT_RECIPIENTS_TITLE, conversation.title)
            editRecipients.putExtra(ComposeConstants.EXTRA_EDIT_RECIPIENTS_NUMBERS, conversation.phoneNumbers)

            startActivity(editRecipients)
            true
        }
    }


    private fun setUpPrivate() {
        val preference = findPreference(getString(R.string.pref_contact_private_conversation)) as SwitchPreference
        val folderPreference = findPreference(getString(R.string.pref_contact_select_folder)) as Preference?
        preference.isChecked = conversation.private

        preference.setOnPreferenceChangeListener { _, o ->
            conversation.private = o as Boolean
            folderPreference?.isEnabled = !conversation.private

            true
        }
    }

    private fun setUpFolder() {
        val preference = findPreference(getString(R.string.pref_contact_select_folder))
        val noFolderText = resources.getString(R.string.no_folder)

        if (conversation.private) {
            preference.isEnabled = false
        }

        val handler = Handler()
        Thread {
            val folders =  DataSource.getFoldersAsList(activity)
            val folder = folders.firstOrNull { it.id == conversation.folderId }

            handler.post {
                if (folder == null) {
                    preference.summary = noFolderText
                } else {
                    preference.summary = folder.name
                }

                preference.setOnPreferenceClickListener {
                    val currentIndex = folders.map { it.id }.indexOf(conversation.folderId)
                    val names = folders.map { StringUtils.titleize(it.name!!) }.toMutableList()
                    names.add(0, noFolderText)

                    AlertDialog.Builder(activity)
                            .setSingleChoiceItems(names.toTypedArray(), currentIndex + 1) { dialog, clickedIndex ->
                                if (clickedIndex == 0) {
                                    conversation.folderId = -1
                                    preference.summary = noFolderText
                                } else {
                                    conversation.folderId = folders[clickedIndex - 1].id
                                    preference.summary = folders[clickedIndex - 1].name
                                }

                                Thread {
                                    if (clickedIndex == 0) {
                                        DataSource.removeConversationFromFolder(activity, conversation.id, true)
                                    } else {
                                        DataSource.addConversationToFolder(activity, conversation.id, conversation.folderId!!, true)
                                    }
                                }.start()

                                dialog.dismiss()
                            }.show()
                    true
                }
            }
        }.start()
    }

    private fun setUpCleanupOldMessages() {
        val preference = findPreference(getString(R.string.pref_cleanup_messages_now))

        preference.setOnPreferenceChangeListener { _, o ->
            val cleanup = o as String
            val timeout = when (cleanup) {
                "never" -> -1L
                "one_week" ->TimeUtils.DAY * 7
                "two_weeks" -> TimeUtils.DAY * 17
                "one_month" -> TimeUtils.DAY * 30
                "three_months" -> TimeUtils.DAY * 90
                "six_months" -> TimeUtils.YEAR / 2
                "one_year" -> TimeUtils.YEAR
                else -> -1L
            }

            if (timeout != -1L) {
                DataSource.cleanupOldMessagesInConversation(activity, conversation.id, TimeUtils.now - timeout)
            }
            true
        }
    }

    private fun setUpRingtone() {
        val preference = findPreference(getString(R.string.pref_contact_ringtone)) as RingtonePreference

        preference.setOnPreferenceChangeListener { _, o ->
            val newTone = o as String
            conversation.ringtoneUri = if (newTone.isBlank()) "silent" else newTone
            Log.v("conversation_ringtone", "new ringtone: $o")

            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpNotificationChannels() {
        val channelPref = findPreference(getString(R.string.pref_contact_notification_channel))
        val restoreDefaultsPref = findPreference(getString(R.string.pref_contact_notification_channel_restore_default))

        if (AndroidVersionUtil.isAndroidO) {
            channelPref.setOnPreferenceClickListener {
                NotificationUtils.createNotificationChannel(activity, conversation)

                val intent = Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                intent.putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, conversation.id.toString() + "")
                intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, activity.packageName)
                startActivity(intent)

                true
            }

            restoreDefaultsPref.setOnPreferenceClickListener {
                NotificationUtils.deleteChannel(activity, conversation.id)
                Toast.makeText(activity, R.string.restore_default_channel_settings_popup, Toast.LENGTH_SHORT).show()
                true
            }
        } else {
            val notificationCategory = findPreference(getString(R.string.pref_contact_notification_group)) as PreferenceCategory
            notificationCategory.removePreference(channelPref)
            notificationCategory.removePreference(restoreDefaultsPref)
        }
    }

    private fun setUpColors() {
        val preference = findPreference(getString(R.string.pref_contact_primary_color)) as ColorPreference
        val darkColorPreference = findPreference(getString(R.string.pref_contact_primary_dark_color)) as ColorPreference
        val accentColorPreference = findPreference(getString(R.string.pref_contact_accent_color)) as ColorPreference
        val ledColorPreference = findPreference(getString(R.string.pref_contact_led_color)) as ColorPreference

        preference.setOnPreferenceChangeListener { _, o ->
            ColorUtils.animateToolbarColor(activity, conversation.colors.color, o as Int)
            conversation.colors.color = o
            true
        }

        preference.setColorSelectedListener(object : ColorSelectedListener {
            override fun onColorSelected(colors: ColorSet) {
                darkColorPreference.setColor(colors.colorDark)
                accentColorPreference.setColor(colors.colorAccent)
            }
        })

        darkColorPreference.setOnPreferenceChangeListener { _, o ->
            ColorUtils.animateStatusBarColor(activity, conversation.colors.colorDark, o as Int)
            conversation.colors.colorDark = o
            true
        }

        accentColorPreference.setOnPreferenceChangeListener { _, o ->
            conversation.colors.colorAccent = o as Int
            true
        }

        ledColorPreference.setOnPreferenceChangeListener { _, newValue ->
            conversation.ledColor = newValue as Int
            true
        }
    }

    fun saveSettings() {
        val source = DataSource
        source.updateConversationSettings(activity, conversation)

        val contactList = if (conversation.isGroup) {
            source.getContactsByNames(activity, conversation.title)
        } else {
            source.getContacts(activity, conversation.phoneNumbers)
        }

        if (contactList.size == 1) {
            // it is an individual conversation and we have the contact in our database! Yay.
            contactList[0].colors = conversation.colors
            source.updateContact(activity, contactList[0])
        }

        PreferenceManager.getDefaultSharedPreferences(activity).edit()
                .putString(getString(R.string.pref_cleanup_messages_now), "never").commit()
    }

    companion object {
        private val ARG_CONVERSATION_ID = "conversation_id"

        fun newInstance(conversationId: Long): ContactSettingsFragment {
            val fragment = ContactSettingsFragment()
            val args = Bundle()
            args.putLong(ARG_CONVERSATION_ID, conversationId)
            fragment.arguments = args

            return fragment
        }
    }
}
