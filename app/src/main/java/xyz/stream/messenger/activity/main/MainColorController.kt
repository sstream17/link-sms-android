package xyz.stream.messenger.activity.main

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getColor
import com.google.android.material.bottomnavigation.BottomNavigationView
import xyz.stream.messenger.R
import xyz.stream.messenger.activity.MessengerActivity
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.pojo.BaseTheme
import xyz.stream.messenger.shared.util.ActivityUtils
import xyz.stream.messenger.shared.util.ColorConverter.DARKEN_AMOUNT
import xyz.stream.messenger.shared.util.ColorConverter.LIGHTEN_AMOUNT
import xyz.stream.messenger.shared.util.ColorConverter.darken
import xyz.stream.messenger.shared.util.ColorConverter.lighten
import xyz.stream.messenger.shared.util.ColorUtils
import xyz.stream.messenger.shared.util.ColorUtils.CONTRAST_MINIMUM
import xyz.stream.messenger.shared.util.ColorUtils.getContrastRatio
import xyz.stream.messenger.shared.util.TimeUtils

class MainColorController(private val activity: AppCompatActivity) {

    private val toolbar: Toolbar by lazy { activity.findViewById<View>(R.id.toolbar) as Toolbar }
    private val conversationListContainer: View by lazy { activity.findViewById<View>(R.id.conversation_list_container) }
    private val bottomNav: BottomNavigationView by lazy { activity.findViewById<View>(R.id.nav_view) as BottomNavigationView }

    fun colorActivity() {
        ColorUtils.checkBlackBackground(activity)
        ActivityUtils.setTaskDescription(activity)

        if (!Build.FINGERPRINT.contains("robolectric")) {
            TimeUtils.setupNightTheme(activity)
        }
    }

    fun configureGlobalColors() {
        if (Settings.isCurrentlyDarkTheme(activity)) {
            activity.window.navigationBarColor = Color.BLACK
        }

        toolbar.setBackgroundColor(Settings.mainColorSet.color)

        ColorUtils.adjustStatusBarColor(Settings.mainColorSet.color, Settings.mainColorSet.colorDark, activity)

        val selectedItemColor = possiblyOverrideColorSelection(activity, Settings.mainColorSet.color, Settings.mainColorSet.colorAccent)

        val states = arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(-android.R.attr.state_selected))
        val colors = intArrayOf(selectedItemColor, getColor(activity, R.color.secondaryText))
        val colorStateList = ColorStateList(states, colors)
        bottomNav.itemIconTintList = colorStateList
        bottomNav.itemTextColor = colorStateList

        if (Settings.baseTheme == BaseTheme.BLACK) {
            conversationListContainer.setBackgroundColor(Color.BLACK)
            bottomNav.setBackgroundColor(Color.BLACK)
        }
    }

    fun configureNavigationBarColor() {
        val isMessengerActivity = activity is MessengerActivity
        when {
            Settings.baseTheme == BaseTheme.BLACK -> ActivityUtils.setUpNavigationBarColor(activity, Color.BLACK, isMessengerActivity)
            Settings.isCurrentlyDarkTheme(activity) -> ActivityUtils.setUpNavigationBarColor(activity, -1223, isMessengerActivity) // random. the activity utils will handle the dark color
            else -> ActivityUtils.setUpNavigationBarColor(activity, Color.WHITE, isMessengerActivity)
        }
    }

    private fun possiblyOverrideColorSelection(
            context: Context,
            mainColor: Int,
            accentColor: Int): Int {
        val isBlackTheme = Settings.baseTheme == BaseTheme.BLACK
        val isDarkTheme = Settings.isCurrentlyDarkTheme(context)
        val isDarkColor = ColorUtils.isColorDark(mainColor)
        return when {
            isBlackTheme && isDarkColor -> selectColorMeetingContrast(context, Color.BLACK, mainColor, accentColor, Color.WHITE, LIGHTEN_AMOUNT, ::lighten)
            isDarkTheme && isDarkColor -> selectColorMeetingContrast(context, context.getColor(R.color.background), mainColor, accentColor, Color.WHITE, LIGHTEN_AMOUNT, ::lighten)
            !isDarkTheme && !isDarkColor -> selectColorMeetingContrast(context, Color.WHITE, mainColor, accentColor, Color.BLACK, DARKEN_AMOUNT, ::darken)
            else -> mainColor
        }
    }

    private fun selectColorMeetingContrast(
            context: Context,
            backgroundColor: Int,
            mainColor: Int,
            accentColor: Int,
            defaultColor: Int,
            modifyAmount: Int,
            modifierMethod: (Int, Int) -> Int): Int {
        val mainColor = when(mainColor) {
            Color.WHITE -> context.getColor(R.color.nearWhite)
            Color.BLACK -> context.getColor(R.color.nearBlack)
            else -> mainColor
        }

        val accentColor = when(accentColor) {
            Color.WHITE -> context.getColor(R.color.nearWhite)
            Color.BLACK -> context.getColor(R.color.nearBlack)
            else -> accentColor
        }

        var i = 0
        do {
            val modifiedMainColor = modifierMethod(mainColor, modifyAmount * i)
            val modifiedAccentColor = modifierMethod(accentColor, modifyAmount * i)
            val contrastMain = getContrastRatio(backgroundColor, modifiedMainColor)
            val contrastAccent = getContrastRatio(backgroundColor, modifiedAccentColor)

            if (contrastMain > CONTRAST_MINIMUM) {
                return modifiedMainColor
            } else if (contrastAccent > CONTRAST_MINIMUM) {
                return modifiedAccentColor
            }
        } while (i++ < 4)

        return defaultColor
    }
}