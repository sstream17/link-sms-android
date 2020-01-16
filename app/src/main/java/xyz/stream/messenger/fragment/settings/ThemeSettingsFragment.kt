package xyz.stream.messenger.fragment.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate

import xyz.stream.messenger.R
import xyz.stream.messenger.api.implementation.Account
import xyz.stream.messenger.api.implementation.ApiUtils
import xyz.stream.messenger.shared.data.ColorSet
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.BaseTheme
import xyz.stream.messenger.shared.util.AndroidVersionUtil
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.TimeUtils
import xyz.stream.messenger.shared.util.listener.ColorSelectedListener
import xyz.stream.messenger.view.ColorPreference

class ThemeSettingsFragment : MaterialPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.settings_theme)

        initBaseTheme()
        initBubbleStyle()
        initFontSize()
        initApplyToolbarTheme()
        initConversationCategories()
        initMessageTimestamp()

        initUseGlobalTheme()
        setUpColors()
    }

    override fun onStop() {
        super.onStop()
        Settings.forceUpdate(activity)
    }

    private fun initBaseTheme() {
        findPreference(getString(R.string.pref_base_theme))
                .setOnPreferenceChangeListener { _, o ->
                    val newValue = o as String

                    TimeUtils.setupNightTheme(null, when (newValue) {
                        "day_night" -> BaseTheme.DAY_NIGHT
                        "light" -> BaseTheme.ALWAYS_LIGHT
                        "dark" -> BaseTheme.ALWAYS_DARK
                        "black" -> BaseTheme.BLACK
                        else -> BaseTheme.DAY_NIGHT
                    })

                    ApiUtils.updateBaseTheme(Account.accountId, newValue)
                    activity.recreate()
                    
                    true
                }
    }

    private fun initFontSize() {
        findPreference(getString(R.string.pref_font_size))
                .setOnPreferenceChangeListener { _, o ->
                    val size = o as String
                    ApiUtils.updateFontSize(Account.accountId, size)

                    true
                }
    }

    private fun initBubbleStyle() {
        findPreference(getString(R.string.pref_bubble_style))
                .setOnPreferenceChangeListener { _, o ->
                    val style = o as String
                    ApiUtils.updateBubbleStyle(Account.accountId, style)
                    true
                }
    }

    private fun initApplyToolbarTheme() {
        findPreference(getString(R.string.pref_apply_primary_color_toolbar))
                .setOnPreferenceChangeListener { _, o ->
                    val colorToolbar = o as Boolean
                    ApiUtils.updateApplyToolbarColor(Account.accountId, colorToolbar)

                    activity?.recreate()
                    true
                }
    }

    private fun initConversationCategories() {
        findPreference(getString(R.string.pref_conversation_categories))
                .setOnPreferenceChangeListener { _, o ->
                    val showCategories = o as Boolean
                    ApiUtils.updateConversationCategories(Account.accountId, showCategories)
                    true
                }
    }

    private fun initMessageTimestamp() {
        findPreference(getString(R.string.pref_message_timestamp))
                .setOnPreferenceChangeListener { _, o ->
                    val showTimestamp = o as Boolean
                    ApiUtils.updateMessageTimestamp(Account.accountId, showTimestamp)
                    true
                }
    }

    private fun initUseGlobalTheme() {
        findPreference(getString(R.string.pref_apply_theme_globally))
                .setOnPreferenceChangeListener { _, o ->
                    val global = o as Boolean
                    ApiUtils.updateUseGlobalTheme(Account.accountId, global)

                    true
                }
    }

    private fun setUpColors() {
        val preference = findPreference(getString(R.string.pref_global_primary_color)) as ColorPreference
        val darkColorPreference = findPreference(getString(R.string.pref_global_primary_dark_color)) as ColorPreference
        val accentColorPreference = findPreference(getString(R.string.pref_global_accent_color)) as ColorPreference

        preference.setOnPreferenceChangeListener { _, o ->
            ColorUtils.animateToolbarColor(activity, Settings.mainColorSet.color, o as Int)
            Settings.setValue(activity, getString(R.string.pref_global_primary_color), o)
            Settings.mainColorSet.color = o

            ApiUtils.updatePrimaryThemeColor(Account.accountId, o)
            true
        }

        preference.setColorSelectedListener(object : ColorSelectedListener {
            override fun onColorSelected(colors: ColorSet) {
                darkColorPreference.setColor(colors.colorDark)
                accentColorPreference.setColor(colors.colorAccent)
            }
        })

        darkColorPreference.setOnPreferenceChangeListener { _, o ->
            ColorUtils.animateStatusBarColor(activity, Settings.mainColorSet.colorDark, o as Int)
            Settings.setValue(activity, getString(R.string.pref_global_primary_dark_color), o)
            Settings.mainColorSet.colorDark = o

            ApiUtils.updatePrimaryDarkThemeColor(Account.accountId, o)
            true
        }

        accentColorPreference.setOnPreferenceChangeListener { _, o ->
            Settings.setValue(activity, getString(R.string.pref_global_accent_color), o as Int)
            Settings.mainColorSet.colorAccent = o

            ApiUtils.updateAccentThemeColor(Account.accountId, o)
            true
        }
    }
}
