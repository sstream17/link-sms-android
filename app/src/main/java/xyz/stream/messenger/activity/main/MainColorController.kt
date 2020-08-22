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

        val selectedItemColor = possiblyOverrideColorSelection(activity, Settings.mainColorSet.color)

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

    private fun possiblyOverrideColorSelection(context: Context, color: Int): Int {
        val isDarkTheme = Settings.isCurrentlyDarkTheme(context)
        val isDarkColor = ColorUtils.isColorDark(color)
        return when {
            isDarkTheme && isDarkColor -> {
                if (color == Color.BLACK) Color.WHITE else lighten(color, LIGHTEN_AMOUNT)
            }
            !isDarkTheme && !isDarkColor -> {
                if (color == Color.WHITE) Color.BLACK else darken(color, DARKEN_AMOUNT)
            }
            else -> color
        }
    }
}